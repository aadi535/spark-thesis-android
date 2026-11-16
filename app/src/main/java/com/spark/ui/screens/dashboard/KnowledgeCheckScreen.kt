package com.spark.ui.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spark.data.local.AccountSessionManager
import com.spark.data.network.ReviewResult
import com.spark.data.network.SparkApiService
import com.spark.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun KnowledgeCheckCard() {
    val owner = AccountSessionManager.accountKey
    val session = AccountSessionManager.generation
    val api: SparkApiService = org.koin.compose.koinInject()
    val state = remember(owner, session) { KnowledgeCheckState(api) { session == AccountSessionManager.generation } }
    var open by remember(owner, session) { mutableStateOf(false) }
    val guest = owner == null || owner == AccountSessionManager.GUEST
    Column(Modifier.fillMaxWidth().premiumSurface(24).padding(18.dp)) {
        Text("Knowledge Check", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(if (guest) "Sign in to review what you learned." else "Five questions about ideas you chose to explore.",
            color = TextSecondary, fontSize = 12.sp)
        TextButton(onClick = { open = true }, enabled = !guest) { Text("Review what you learned", color = TextSecondary) }
    }
    if (open) {
        Dialog(onDismissRequest = { open = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            KnowledgeCheckScreen(state, onClose = { open = false })
        }
    }
}

@Composable
fun KnowledgeCheckScreen(state: KnowledgeCheckState, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) { if (state.history == null) state.loadHistory() }
    BackHandler { onClose() }
    Surface(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), color = CardBackground,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Knowledge Check", color = TextPrimary, fontSize = 20.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onClose) { Text("Close", color = TextSecondary) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.loading) {
                    CircularProgressIndicator(color = SparkCyan)
                    Text("Please wait…", color = TextSecondary)
                }
                state.error?.let {
                    Text(it, color = TextSecondary)
                    if (state.history == null) TextButton(onClick = { scope.launch { state.loadHistory() } }, enabled = !state.loading) {
                        Text("Try again", color = SparkCyan)
                    }
                }
                val result = state.result
                val started = state.started
                when {
                    result != null -> {
                        ReviewResultContent(result)
                        TextButton(onClick = state::showHistory, enabled = !state.loading) { Text("Back to results", color = SparkCyan) }
                    }
                    started != null -> {
                        val question = started.questions[state.questionIndex]
                        Text("Question ${state.questionIndex + 1} of ${started.questions.size}", color = SparkCyan)
                        Text(question.topic, color = TextSecondary, fontSize = 12.sp)
                        Text(question.question, color = TextPrimary, fontSize = 18.sp)
                        question.options.forEachIndexed { index, option ->
                            Row(Modifier.fillMaxWidth().clickable(enabled = !state.loading) { state.select(index) }
                                .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.answers[state.questionIndex] == index,
                                    onClick = { state.select(index) }, enabled = !state.loading)
                                Text(option, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                        }
                        val last = state.questionIndex == started.questions.lastIndex
                        Button(onClick = { if (last) scope.launch { state.submit() } else state.next() },
                            enabled = !state.loading && state.answers[state.questionIndex] != null) {
                            Text(if (last) "Submit answers" else "Next")
                        }
                    }
                    else -> state.history?.let { history ->
                        val remaining = (5 - history.eligibleCount).coerceAtLeast(0)
                        Text(if (remaining > 0) "Explore $remaining more different ideas to begin. Newly explored ideas must finish syncing."
                            else "Check your understanding of five explored ideas. Scores do not change your XP.", color = TextSecondary)
                        Button(onClick = { scope.launch { state.start() } }, enabled = !state.loading && remaining == 0) {
                            Text("Start Knowledge Check")
                        }
                        TextButton(onClick = { scope.launch { state.loadHistory() } }, enabled = !state.loading) {
                            Text("Refresh learning history", color = SparkCyan)
                        }
                        Text("Previous results", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        if (history.results.isEmpty()) Text("No completed checks yet.", color = TextSecondary)
                        history.results.forEach { saved ->
                            TextButton(onClick = { state.viewResult(saved) }, enabled = !state.loading) {
                                Text("${reviewDate(saved.completedAt)} · ${saved.correctCount}/${saved.totalCount}", color = SparkCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewResultContent(result: ReviewResult) {
    val percent = if (result.totalCount > 0) result.correctCount * 100 / result.totalCount else 0
    Text("${result.correctCount}/${result.totalCount} · $percent%", color = SparkCyan, fontSize = 26.sp)
    Text(reviewDate(result.completedAt), color = TextSecondary, fontSize = 12.sp)
    if (result.correctCount == result.totalCount) Text("All answers correct!", color = TextPrimary)
    result.questions.forEach { question ->
        val correct = question.selectedOptionIndex == question.correctOptionIndex
        Text(if (correct) "Correct · ${question.topic}" else "Review · ${question.topic}", color = SparkCyan, fontSize = 12.sp)
        Text(question.question, color = TextPrimary)
        if (!correct) Text("Your answer: ${question.options[question.selectedOptionIndex]}", color = TextSecondary)
        Text("Correct answer: ${question.options[question.correctOptionIndex]}", color = TextPrimary)
        Text(question.explanation, color = TextSecondary)
        HorizontalDivider(color = GlassEdge)
    }
}

private fun reviewDate(timestamp: String): String = try {
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(timestamp))
} catch (e: Exception) { "Saved result" }
