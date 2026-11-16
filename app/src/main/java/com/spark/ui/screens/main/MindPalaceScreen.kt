package com.spark.ui.screens.main

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.spark.ui.screens.dashboard.ChatMessage
import com.spark.ui.screens.dashboard.GeneratedSpark
import com.spark.ui.screens.dashboard.GeneratedSparkFullScreen
import com.spark.ui.theme.*
import com.spark.ui.theme.ThemeManager
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MindPalaceScreen() {
    val aiClient: com.spark.data.network.SparkAIClient = org.koin.compose.koinInject()
    val context = LocalContext.current
    var selectedJournalSpark by remember { mutableStateOf<GeneratedSpark?>(null) }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var showDnaModal by remember { mutableStateOf(false) }
    var showExportPdfModal by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedJournalTopic by remember { mutableStateOf("ALL") }
    var selectedDateFilter by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "WEEK", "OLDER"
    var journalSortOrder by remember { mutableStateOf("NEWEST") } // "NEWEST", "OLDEST"

    // Topics list
    val allDistinctTopics = remember(MindPalaceData.journalHistory.size) {
        val topicsFromJournal = MindPalaceData.journalHistory.map { it.category.uppercase().trim() }.distinct()
        val standard = listOf("TECH", "SCIENCE", "PSYCHOLOGY", "HISTORY", "BIOLOGY", "PHILOSOPHY", "MATH", "ARTS")
        (listOf("ALL") + (topicsFromJournal + standard).distinct()).filter { topic ->
            topic == "ALL" || MindPalaceData.journalHistory.any { it.category.equals(topic, ignoreCase = true) }
        }.let { if (it.size <= 1) listOf("ALL", "TECH", "SCIENCE", "PSYCHOLOGY", "HISTORY") else it }
    }

    // Filtered & sorted journal entries
    val filteredJournalEntries = remember(
        selectedJournalTopic,
        selectedDateFilter,
        journalSortOrder,
        MindPalaceData.journalHistory.size
    ) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneWeekMillis = 7 * oneDayMillis

        var list = if (selectedJournalTopic == "ALL") {
            MindPalaceData.journalHistory.toList()
        } else {
            MindPalaceData.journalHistory.filter {
                it.category.equals(selectedJournalTopic, ignoreCase = true)
            }
        }

        // Filter by date
        list = when (selectedDateFilter) {
            "TODAY" -> list.filter {
                it.dateStr.contains("Today", ignoreCase = true) ||
                    (it.timestamp > 0 && (now - it.timestamp) <= oneDayMillis)
            }
            "WEEK" -> list.filter {
                it.dateStr.contains("Today", ignoreCase = true) ||
                    (it.timestamp > 0 && (now - it.timestamp) <= oneWeekMillis)
            }
            "OLDER" -> list.filter {
                !it.dateStr.contains("Today", ignoreCase = true) &&
                    (it.timestamp > 0 && (now - it.timestamp) > oneWeekMillis)
            }
            else -> list
        }

        // Sort order
        if (journalSortOrder == "OLDEST") {
            list.sortedBy { it.timestamp }
        } else {
            list.sortedByDescending { it.timestamp }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp)
        ) {
            item {
                HeaderSection()
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                CognitiveSynthesisCard(MindPalaceData.stats)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                NeuralNetworkCard(
                    nodes = MindPalaceData.neuralNodes,
                    connections = MindPalaceData.neuralConnections,
                    onOpenSpark = { spark ->
                        selectedJournalSpark = spark
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                IntellectualDnaCard(
                    stats = MindPalaceData.stats,
                    radarData = MindPalaceData.radarData,
                    onClick = { showDnaModal = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                LearningJournalHeader(
                    totalCount = filteredJournalEntries.size,
                    selectedTopic = selectedJournalTopic,
                    availableTopics = allDistinctTopics,
                    onSelectTopic = { selectedJournalTopic = it },
                    selectedDateFilter = selectedDateFilter,
                    onSelectDateFilter = { selectedDateFilter = it },
                    sortOrder = journalSortOrder,
                    onToggleSortOrder = {
                        journalSortOrder = if (journalSortOrder == "NEWEST") "OLDEST" else "NEWEST"
                    },
                    onExportPdfClick = { showExportPdfModal = true }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filteredJournalEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CardBackground)
                            .border(1.dp, GlassEdge, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FeatureGlyph("🔍", size = 28.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No sparks match this filter",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting 'All Topics' or 'All Time' above.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                items(filteredJournalEntries) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = {
                            val formattedSteps = if (entry.steps.isNotEmpty()) {
                                entry.steps.joinToString("\n- ", prefix = "- ")
                            } else {
                                "- Explore foundational principles\n- Reflect on key insights\n- Connect concept to daily reality"
                            }

                            val content = if (entry.fullContent.isNotBlank()) {
                                "${entry.fullContent}\n\nSteps:\n$formattedSteps\n\nFun Fact: ${if (entry.funFact.isNotBlank()) entry.funFact else entry.quote}"
                            } else {
                                "“${entry.quote}”\n\nThis past learning quest was completed and added to your encrypted neural signature.\n\nSteps:\n$formattedSteps\n\nFun Fact: Learning builds permanent physical neural pathways in your brain!"
                            }

                            selectedJournalSpark = GeneratedSpark(
                                title = entry.title,
                                content = content,
                                emoji = entry.emoji.ifBlank { "🧠" },
                                type = entry.category
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // FULL SCREEN PAST LEARNING SPARK OVERLAY
        AnimatedVisibility(
            visible = selectedJournalSpark != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            selectedJournalSpark?.let { spark ->
                GeneratedSparkFullScreen(
                    spark = spark,
                    chatMessages = chatMessages,
                    readingTimer = 0, // Past learning is already unlocked!
                    onSendMessage = { text ->
                        val userMsg = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            text = text,
                            isUser = true
                        )
                        chatMessages = chatMessages + userMsg
                        coroutineScope.launch {
                            val accountSession = com.spark.data.local.AccountSessionManager.generation
                            val aiAnswer = aiClient.sendChatMessage(
                                sparkTitle = spark.title,
                                sparkContent = spark.content,
                                history = chatMessages,
                                newQuestion = text
                            )
                            if (accountSession != com.spark.data.local.AccountSessionManager.generation) return@launch
                            chatMessages = chatMessages + ChatMessage(
                                id = java.util.UUID.randomUUID().toString(),
                                text = aiAnswer,
                                isUser = false
                            )
                        }
                    },
                    onMinimize = {
                        selectedJournalSpark = null
                        chatMessages = emptyList()
                    },
                    onComplete = {
                        selectedJournalSpark = null
                        chatMessages = emptyList()
                    }
                )
            }
        }

        if (showDnaModal) {
            IntellectualDnaDetailModal(
                stats = MindPalaceData.stats,
                radarData = MindPalaceData.radarData,
                onDismiss = { showDnaModal = false }
            )
        }

        if (showExportPdfModal) {
            ExportPdfModal(
                journalEntries = MindPalaceData.journalHistory,
                stats = MindPalaceData.stats,
                userName = GlobalUserState.userName,
                onDismiss = { showExportPdfModal = false }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    ScreenHeading("Mind Palace", "A mind in motion.", "Your discoveries. Beautifully connected.")
}

@Composable
fun CognitiveSynthesisCard(stats: MindPalaceStats) {
    Column(Modifier.fillMaxWidth().reveal(80).premiumSurface().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Your collection", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(stats.intellectDnaRank, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Total XP", stats.totalXp, "")
            StatItem("Neurons", "${stats.activeNeurons}", "")
            StatItem("Sync ratio", "${stats.syncRatio}%", "")
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, emoji: String) {
    Column {
        Text(value, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
        Spacer(Modifier.height(6.dp))
        Text(title, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun IntellectualDnaCard(
    stats: MindPalaceStats,
    radarData: List<RadarDataPoint>,
    onClick: () -> Unit = {}
) {
    Text(
        text = "Your intellectual DNA",
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassEdge, RoundedCornerShape(20.dp))
            .softPress { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.size(140.dp)) {
                    RadarChart(data = radarData)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MIND SEQUENCE",
                        color = SparkCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stats.mindSequenceId,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your encrypted neural signature, uniquely synthesized from your intellectual trajectory.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (ThemeManager.isLightMode) SurfaceVariant else AppBackground)
                    .border(0.8.dp, SurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "ANALYSIS CORE V1.2",
                        color = SparkCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stats.synthesisPeakMessage,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RadarChart(data: List<RadarDataPoint>) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) * 0.7f
        val angleStep = (2 * Math.PI / data.size).toFloat()

        val rings = 4
        for (i in 1..rings) {
            val ringRadius = radius * (i.toFloat() / rings)
            val ringPath = Path()
            for (j in data.indices) {
                val angle = j * angleStep - Math.PI / 2
                val x = center.x + ringRadius * cos(angle).toFloat()
                val y = center.y + ringRadius * sin(angle).toFloat()
                if (j == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = SurfaceVariant,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        for (j in data.indices) {
            val angle = j * angleStep - Math.PI / 2
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            drawLine(
                color = SurfaceVariant,
                start = center,
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val dataPath = Path()
        val dataPoints = mutableListOf<Offset>()

        for (j in data.indices) {
            val valueRatio = (data[j].value / 100f) * progress.value
            val angle = j * angleStep - Math.PI / 2
            val x = center.x + radius * valueRatio * cos(angle).toFloat()
            val y = center.y + radius * valueRatio * sin(angle).toFloat()
            val point = Offset(x, y)
            dataPoints.add(point)

            if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(
            path = dataPath,
            color = SparkCyan.copy(alpha = 0.18f),
            style = Fill
        )
        drawPath(
            path = dataPath,
            color = SparkCyan.copy(alpha = 0.18f),
            style = Stroke(width = 2.dp.toPx())
        )

        dataPoints.forEach { point ->
            drawCircle(
                color = SparkCyan,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun LearningJournalHeader(
    totalCount: Int,
    selectedTopic: String,
    availableTopics: List<String>,
    onSelectTopic: (String) -> Unit,
    selectedDateFilter: String,
    onSelectDateFilter: (String) -> Unit,
    sortOrder: String,
    onToggleSortOrder: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    val topicScrollState = rememberScrollState()
    val dateScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // TOP ROW: Title + Count + Export PDF Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEARNING JOURNAL",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SparkCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$totalCount",
                        color = SparkCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Export PDF Button (Active & Clickable!)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                    .border(1.dp, SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                    .softPress { onExportPdfClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export PDF",
                        tint = SparkCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export PDF",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TOPIC FILTER ROW (Horizontal scrollable chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(topicScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            availableTopics.forEach { topic ->
                val isSelected = selectedTopic.equals(topic, ignoreCase = true)
                val topicColor = if (topic == "ALL") SparkCyan else MindPalaceData.getTopicColor(topic)
                val count = if (topic == "ALL") {
                    MindPalaceData.journalHistory.size
                } else {
                    MindPalaceData.journalHistory.count { it.category.equals(topic, ignoreCase = true) }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) topicColor.copy(alpha = 0.2f)
                            else CardBackground
                        )
                        .border(
                            1.dp,
                            if (isSelected) topicColor else SurfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .softPress { onSelectTopic(topic) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(topicColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (topic == "ALL") "All Topics" else topic.replaceFirstChar { it.uppercase() },
                            color = if (isSelected) (if (ThemeManager.isLightMode) Color.Black else Color.White) else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$count",
                                color = if (isSelected) topicColor else TextSecondary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // DATE FILTER & SORT ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dateScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateFilters = listOf(
                "ALL" to "All Time",
                "TODAY" to "Today",
                "WEEK" to "This Week",
                "OLDER" to "Older"
            )

            dateFilters.forEach { (filterKey, filterLabel) ->
                val isSelected = selectedDateFilter == filterKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SurfaceVariant else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) SparkCyan.copy(alpha = 0.18f) else SurfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .softPress { onSelectDateFilter(filterKey) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = filterLabel,
                        color = if (isSelected) SparkCyan else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Sort Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBackground)
                    .border(1.dp, GlassEdge, RoundedCornerShape(8.dp))
                    .softPress { onToggleSortOrder() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (sortOrder == "NEWEST") "↓ Newest" else "↑ Oldest",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry, onClick: () -> Unit = {}) {
    val topicColor = MindPalaceData.getTopicColor(entry.category)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .softPress { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Top Row: Topic Badge + Date + XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(topicColor.copy(alpha = 0.18f))
                            .border(0.8.dp, topicColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.category,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.dateStr,
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${entry.xpGained} XP",
                        color = WondersGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Revisit →", color = SparkCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title / Question
            Text(
                text = entry.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Hook quote if present
            if (entry.quote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(2.dp)
                            .background(topicColor.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "“${entry.quote}”",
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ExportPdfModal(
    journalEntries: List<JournalEntry>,
    stats: MindPalaceStats,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    // Unique topics present in journal
    val availableTopics = remember(journalEntries.size) {
        journalEntries.map { it.category.uppercase().trim() }.distinct().sorted()
    }

    // Selected topics for export
    var selectAll by remember { mutableStateOf(true) }
    var selectedTopics by remember { mutableStateOf(availableTopics.toSet()) }

    val matchingEntriesCount = remember(selectedTopics, selectAll) {
        if (selectAll) journalEntries.size
        else journalEntries.count { selectedTopics.contains(it.category.uppercase().trim()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FeatureGlyph("📄", size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Mind Palace Dossier",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select the knowledge domains to compile into your PDF dossier.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select All toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBackground)
                        .clickable {
                            selectAll = !selectAll
                            selectedTopics = if (selectAll) availableTopics.toSet() else emptySet()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Topics & Domains",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Checkbox(
                        checked = selectAll,
                        onCheckedChange = {
                            selectAll = it
                            selectedTopics = if (it) availableTopics.toSet() else emptySet()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SparkCyan,
                            checkmarkColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Individual Topic items
                availableTopics.forEach { topic ->
                    val isChecked = selectAll || selectedTopics.contains(topic)
                    val topicCount = journalEntries.count { it.category.equals(topic, ignoreCase = true) }
                    val topicColor = MindPalaceData.getTopicColor(topic)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackground)
                            .clickable {
                                val newSet = selectedTopics.toMutableSet()
                                if (isChecked) {
                                    newSet.remove(topic)
                                    selectAll = false
                                } else {
                                    newSet.add(topic)
                                    if (newSet.size == availableTopics.size) selectAll = true
                                }
                                selectedTopics = newSet
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(topicColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = topic.replaceFirstChar { it.uppercase() },
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "($topicCount)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val newSet = selectedTopics.toMutableSet()
                                if (checked) {
                                    newSet.add(topic)
                                    if (newSet.size == availableTopics.size) selectAll = true
                                } else {
                                    newSet.remove(topic)
                                    selectAll = false
                                }
                                selectedTopics = newSet
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SparkCyan,
                                checkmarkColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassEdge, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total to Export:", color = TextSecondary, fontSize = 11.sp)
                            Text("$matchingEntriesCount Sparks", color = SparkCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Format:", color = TextSecondary, fontSize = 11.sp)
                            Text("Structured A4 PDF with Spark Logo", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (matchingEntriesCount == 0) {
                        android.widget.Toast.makeText(context, "Please select at least one topic to export", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isExporting = true
                    val topicsToExport = if (selectAll) setOf("ALL") else selectedTopics
                    val file = SparkPdfExporter.exportDossier(
                        context = context,
                        selectedTopics = topicsToExport,
                        journalEntries = journalEntries,
                        stats = stats,
                        userName = userName
                    )
                    isExporting = false
                    if (file != null) {
                        SparkPdfExporter.sharePdf(context, file)
                        onDismiss()
                    }
                },
                enabled = !isExporting && matchingEntriesCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Generate PDF",
                        tint = AppBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExporting) "Generating..." else "Generate & Export PDF",
                        color = AppBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    )
}

@Composable
fun NeuralNetworkCard(
    nodes: List<NeuralNode>,
    connections: List<NeuralConnection>,
    onOpenSpark: (GeneratedSpark) -> Unit = {}
) {
    var dragRotX by remember { mutableStateOf(0f) }
    var dragRotY by remember { mutableStateOf(0f) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var showDetailModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "autoRotate")
    val autoRotY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoRotate"
    )

    val currentRotX = dragRotX
    val currentRotY = dragRotY + autoRotY

    val selectedNode = nodes.find { it.id == selectedNodeId }
    val selectedJournalEntry = selectedNode?.let { node ->
        MindPalaceData.journalHistory.find { it.id == node.id || it.title.equals(node.title, ignoreCase = true) }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassEdge, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Your constellation",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Drag to orbit • Tap node for question details",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${nodes.size}",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "connections",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (nodes.isEmpty()) 220.dp else 290.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .appBackdrop()
            ) {
                data class ProjectedNode(
                    val node: NeuralNode,
                    val px: Float,
                    val py: Float,
                    val pz: Float,
                    val scale: Float
                )

                var projectedNodes by remember { mutableStateOf(listOf<ProjectedNode>()) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dragRotX -= dragAmount.y * 0.005f
                                dragRotY += dragAmount.x * 0.005f
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val tapped = projectedNodes.sortedByDescending { it.pz }.find { pn ->
                                    val dx = pn.px - offset.x
                                    val dy = pn.py - offset.y
                                    (dx * dx + dy * dy) < (34.dp.toPx() * pn.scale * 34.dp.toPx() * pn.scale)
                                }
                                if (tapped != null) {
                                    selectedNodeId = tapped.node.id
                                    showDetailModal = true
                                }
                            }
                        }
                ) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val maxRadius = size.minDimension / 2.5f

                    projectedNodes = nodes.map { node ->
                        val cosY = cos(currentRotY)
                        val sinY = sin(currentRotY)
                        val x1 = node.x * cosY - node.z * sinY
                        val z1 = node.x * sinY + node.z * cosY

                        val cosX = cos(currentRotX)
                        val sinX = sin(currentRotX)
                        val y2 = node.y * cosX - z1 * sinX
                        val z2 = node.y * sinX + z1 * cosX

                        val perspective = 1f + (z2 * 0.3f)

                        ProjectedNode(
                            node = node,
                            px = cx + x1 * maxRadius * perspective,
                            py = cy + y2 * maxRadius * perspective,
                            pz = z2,
                            scale = perspective
                        )
                    }.sortedBy { it.pz }

                    connections.forEach { conn ->
                        val fromP = projectedNodes.find { it.node.id == conn.fromId }
                        val toP = projectedNodes.find { it.node.id == conn.toId }

                        if (fromP != null && toP != null) {
                            val isActive = selectedNodeId == fromP.node.id || selectedNodeId == toP.node.id
                            val lineAlpha = if (isActive) 0.85f else 0.2f
                            val lineColor = if (isActive) SparkCyan else SurfaceVariant
                            val strokeW = if (isActive) 2.dp.toPx() else 1.dp.toPx()

                            drawLine(
                                color = lineColor.copy(alpha = lineAlpha),
                                start = Offset(fromP.px, fromP.py),
                                end = Offset(toP.px, toP.py),
                                strokeWidth = strokeW
                            )
                        }
                    }

                    projectedNodes.forEach { pn ->
                        val isSelected = pn.node.id == selectedNodeId

                        val alpha = 0.45f + (pn.scale - 0.7f)
                        val clampedAlpha = alpha.coerceIn(0.15f, 1f)

                        val glowRadius = if (isSelected) 36.dp.toPx() * pn.scale else 18.dp.toPx() * pn.scale
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(pn.node.color.copy(alpha = clampedAlpha * (if (isSelected) 0.85f else 0.45f)), Color.Transparent),
                                center = Offset(pn.px, pn.py),
                                radius = glowRadius
                            ),
                            radius = glowRadius,
                            center = Offset(pn.px, pn.py)
                        )

                        drawCircle(
                            color = if (isSelected) Color.White else pn.node.color.copy(alpha = clampedAlpha),
                            radius = (if (isSelected) 5.5.dp else 4.dp).toPx() * pn.scale,
                            center = Offset(pn.px, pn.py)
                        )

                        if (isSelected) {
                            drawCircle(
                                color = SparkCyan,
                                radius = 14.dp.toPx() * pn.scale,
                                center = Offset(pn.px, pn.py),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }

                        // Compact Floating Topic Pill (ONLY starting topic, NOT entire question!)
                        if (pn.pz > -0.6f) {
                            val topicText = pn.node.label.uppercase()
                            val textSizePx = 18f * pn.scale
                            val textPaint = Paint().apply {
                                color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#E2E8F0")
                                textSize = textSizePx
                                textAlign = Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                                this.alpha = (clampedAlpha * 255).toInt()
                            }

                            val textWidth = textPaint.measureText(topicText)
                            val padX = 10f * pn.scale
                            val padY = 5f * pn.scale
                            val pillW = textWidth + padX * 2
                            val pillH = textSizePx + padY * 2
                            val pillTop = pn.py - (12.dp.toPx() * pn.scale) - pillH
                            val pillLeft = pn.px - (pillW / 2f)

                            // Translucent dark pill container
                            drawRoundRect(
                                color = Color(0xF00B0F19).copy(alpha = clampedAlpha * 0.92f),
                                topLeft = Offset(pillLeft, pillTop),
                                size = Size(pillW, pillH),
                                cornerRadius = CornerRadius(6.dp.toPx() * pn.scale, 6.dp.toPx() * pn.scale)
                            )
                            // Clean topic-colored border
                            drawRoundRect(
                                color = if (isSelected) SparkCyan else pn.node.color.copy(alpha = clampedAlpha * 0.6f),
                                topLeft = Offset(pillLeft, pillTop),
                                size = Size(pillW, pillH),
                                cornerRadius = CornerRadius(6.dp.toPx() * pn.scale, 6.dp.toPx() * pn.scale),
                                style = Stroke(width = (if (isSelected) 1.5.dp else 0.8.dp).toPx() * pn.scale)
                            )
                            // Draw topic text centered
                            drawContext.canvas.nativeCanvas.drawText(
                                topicText,
                                pn.px,
                                pillTop + pillH - padY - (1.5f * pn.scale),
                                textPaint
                            )
                        }
                    }
                }
                if (nodes.isEmpty()) {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        SparkOrb(Modifier.size(86.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("Your first connection awaits", color = TextPrimary, fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Complete a spark to start building your constellation.", color = TextSecondary,
                            fontSize = 13.sp, lineHeight = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            // Interactive bottom preview bar when a node is highlighted
            selectedNode?.let { node ->
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBackground)
                        .border(1.dp, node.color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .softPress { showDetailModal = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(node.color.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = node.label.uppercase(),
                                color = node.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = node.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View →",
                        color = SparkCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Detailed Pop-Up Modal when user taps on the node
    if (showDetailModal && selectedNode != null) {
        NeuralNodeDetailModal(
            node = selectedNode,
            entry = selectedJournalEntry,
            onDismiss = { showDetailModal = false },
            onOpenJournal = { spark ->
                showDetailModal = false
                onOpenSpark(spark)
            }
        )
    }
}

@Composable
fun NeuralNodeDetailModal(
    node: NeuralNode,
    entry: JournalEntry?,
    onDismiss: () -> Unit,
    onOpenJournal: (GeneratedSpark) -> Unit
) {
    val scrollState = rememberScrollState()
    val fullTitle = node.title.ifBlank { node.label }
    val topic = node.label.ifBlank { "Insight" }
    val topicColor = node.color

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(topicColor.copy(alpha = 0.18f))
                            .border(1.dp, topicColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = topic.uppercase(),
                            color = topicColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+150 XP",
                        color = WondersGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Full Question in crisp typography
                Text(
                    text = fullTitle,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Core Concept / Hook Box
                val quoteText = entry?.quote ?: "Synthesized neural concept connected to your intellectual signature."
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, topicColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "CORE SYNTHESIS",
                            color = topicColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "“$quoteText”",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Steps / Insights if available
                if (entry != null && entry.steps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "KEY INSIGHTS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    entry.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(bottom = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                color = topicColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = step,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Fun Fact if available
                if (entry != null && entry.funFact.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBackground)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 ${entry.funFact}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stepsList = entry?.steps ?: emptyList()
                    val formattedSteps = if (stepsList.isNotEmpty()) {
                        stepsList.joinToString("\n- ", prefix = "- ")
                    } else {
                        "- Explore foundational principles\n- Reflect on key insights\n- Connect concept to daily reality"
                    }
                    val sparkContent = if (entry != null && entry.fullContent.isNotBlank()) {
                        "${entry.fullContent}\n\nSteps:\n$formattedSteps\n\nFun Fact: ${entry.funFact.ifBlank { entry.quote }}"
                    } else {
                        "“${entry?.quote ?: fullTitle}”\n\nThis past learning quest was completed and added to your encrypted neural signature.\n\nSteps:\n$formattedSteps\n\nFun Fact: Learning builds permanent physical neural pathways in your brain!"
                    }
                    val spark = GeneratedSpark(
                        title = fullTitle,
                        content = sparkContent,
                        emoji = entry?.emoji ?: "🧠",
                        type = topic
                    )
                    onOpenJournal(spark)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Explore in Journal →",
                    color = AppBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    )
}

@Composable
fun IntellectualDnaDetailModal(
    stats: MindPalaceStats,
    radarData: List<RadarDataPoint>,
    onDismiss: () -> Unit
) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeatureGlyph("🧬", size = 22.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Intellectual DNA Synthesis",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SparkFont
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "SYNTHESIS SCORE: ${stats.mindSequenceId}",
                    color = SparkCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your cognitive profile forms dynamically as you absorb sparks across scientific, philosophical, and psychological domains.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("DOMAIN MASTERY BREAKDOWN", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                radarData.forEach { point ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(point.label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("${point.value.toInt()}%", color = SparkCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (point.value / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                            color = SparkCyan,
                            trackColor = SurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(SurfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("🧠 ACTIVE NEURAL PATTERN", color = SparkCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${stats.activeNeurons} active synaptic clusters connected in 3D space.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
            ) {
                Text("Close", color = OnAccent, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
