package com.spark.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import com.spark.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Tuner Types
enum class TunerType { MOOD, TIME, TOPIC }

// Data models
data class MoodOption(val id: String, val emoji: String, val label: String)
data class TimeOption(val id: String, val label: String)
data class TopicOption(val id: String, val label: String, val emoji: String)

val MOODS = listOf(
    MoodOption("curious", "🧐", "Curious"),
    MoodOption("exhausted", "🥱", "Exhausted"),
    MoodOption("focused", "🧠", "Focused"),
    MoodOption("creative", "🎨", "Creative"),
    MoodOption("stressed", "😫", "Stressed")
)

val TIMES = listOf(
    TimeOption("2 mins", "2 mins"),
    TimeOption("5 mins", "5 mins"),
    TimeOption("15 mins", "15 mins")
)

val TOPIC_HUBS = listOf(
    TopicOption("surprise", "Surprise", "🎲"),
    TopicOption("classics", "Classics", "📚"),
    TopicOption("mastery", "Mastery", "🧠"),
    TopicOption("crafts", "Crafts", "🧵")
)

@Composable
fun SpringCapsule(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // iOS-like spring physics
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "capsule_scale"
    )

    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerBottomSheet(
    tunerType: TunerType?,
    currentMood: String?,
    currentTime: String?,
    currentTopicHub: String,
    currentInterest: String,
    onDismiss: () -> Unit,
    onMoodSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onTopicHubSelected: (String) -> Unit,
    onInterestSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (tunerType != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    text = when (tunerType) {
                        TunerType.MOOD -> "SELECT COGNITIVE STATE"
                        TunerType.TIME -> "TEMPORAL ALLOCATION"
                        TunerType.TOPIC -> "TARGET FIELD OF STUDY"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                when (tunerType) {
                    TunerType.MOOD -> MoodSelector(currentMood, onMoodSelected)
                    TunerType.TIME -> TimeSelector(currentTime, onTimeSelected)
                    TunerType.TOPIC -> TopicSelector(
                        currentTopicHub, currentInterest,
                        onTopicHubSelected, onInterestSelected
                    )
                }
            }
        }
    }
}

@Composable
fun MoodSelector(currentMood: String?, onSelect: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MOODS.forEach { mood ->
            val isSelected = mood.id == currentMood
            SpringCapsule(
                isSelected = isSelected,
                onClick = { onSelect(mood.id) },
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(text = mood.emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = mood.label,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TimeSelector(currentTime: String?, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TIMES.forEach { time ->
            val isSelected = time.id == currentTime
            SpringCapsule(
                isSelected = isSelected,
                onClick = { onSelect(time.id) },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = time.label,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TopicSelector(
    currentHub: String,
    currentInterest: String,
    onHubSelect: (String) -> Unit,
    onInterestSelect: (String) -> Unit
) {
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TOPIC_HUBS.forEach { hub ->
                val isSelected = hub.id == currentHub
                SpringCapsule(
                    isSelected = isSelected,
                    onClick = {
                        onHubSelect(hub.id)
                        // Reset interest if changing hubs
                        if (hub.id == "surprise") onInterestSelect("Surprise me!")
                    },
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = hub.emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = hub.label,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (currentHub != "surprise") {
            val items = when (currentHub) {
                "classics" -> listOf("Philosophy", "History", "Literature")
                "crafts" -> listOf("Sewing & Tailoring", "Knitting", "Woodworking")
                else -> listOf("Quantum Physics", "Psychology", "Mathematics", "Biology")
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    val isSelected = item == currentInterest
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .softPress { onInterestSelect(item) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = item,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// FlowRow polyfill for earlier Compose versions if needed, but Compose 1.5+ has ExperimentalLayoutApi FlowRow.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
