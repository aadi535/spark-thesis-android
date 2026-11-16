package com.spark.ui.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global state holder for active sparks.
 * Preserves the active spark, timer, and chat messages across tab navigation
 * (Dashboard -> Mind Palace -> Pro Lab -> Profile).
 */
object GlobalSparkState {
    var activeFullSpark by mutableStateOf<GeneratedSpark?>(null)
    var isFullSparkExpanded by mutableStateOf(false)
    var readingTimer by mutableIntStateOf(15)
    val chatMessages = mutableStateListOf<ChatMessage>()

    fun openSpark(spark: GeneratedSpark) {
        activeFullSpark = spark
        isFullSparkExpanded = true
        readingTimer = 15
        chatMessages.clear()
    }

    fun minimize() {
        isFullSparkExpanded = false
    }

    fun resume() {
        if (activeFullSpark != null) {
            isFullSparkExpanded = true
        }
    }

    fun completeAndClear() {
        activeFullSpark = null
        isFullSparkExpanded = false
        readingTimer = 0
        chatMessages.clear()
    }
}
