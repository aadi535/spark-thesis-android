package com.spark.ui.screens.main

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class MindPalaceStats(
    val totalXp: String,
    val activeNeurons: Int,
    val intellectDnaRank: String,
    val mindSequenceId: String,
    val cognitiveDepth: Double,
    val syncRatio: Int,
    val synthesisPeakCategory: String,
    val synthesisPeakMessage: String
)

data class RadarDataPoint(
    val label: String,
    val value: Float // 0f to 100f
)

data class NeuralNode(
    val id: String,
    val label: String,
    val x: Float, // -1f to 1f
    val y: Float, // -1f to 1f
    val z: Float, // -1f to 1f
    val color: Color,
    val title: String = label,
    val topic: String = label
)

data class NeuralConnection(
    val fromId: String,
    val toId: String
)

data class JournalEntry(
    val id: String,
    val category: String,
    val xpGained: Int,
    val title: String,
    val quote: String,
    val dateStr: String,
    val fullContent: String = "",
    val emoji: String = "🧠",
    val steps: List<String> = emptyList(),
    val funFact: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val completionSource: String? = null
)

object MindPalaceData {
    private const val PREFS_NAME = "spark_mind_palace"
    private val gson = Gson()

    val neuralNodes = androidx.compose.runtime.mutableStateListOf<NeuralNode>()

    val neuralConnections = androidx.compose.runtime.mutableStateListOf<NeuralConnection>()

    fun extractShortTopic(category: String, title: String = ""): String {
        val text = "$category $title".lowercase()
        return when {
            text.contains("tech") || text.contains("ai") || text.contains("cyber") || text.contains("code") ||
                text.contains("comput") || text.contains("software") || text.contains("algo") -> "Tech"
            text.contains("physic") || text.contains("quantum") || text.contains("astro") ||
                text.contains("cosmos") || text.contains("space") || text.contains("science") -> "Science"
            text.contains("psych") || text.contains("mind") || text.contains("brain") ||
                text.contains("neuro") || text.contains("cognit") || text.contains("behavior") -> "Psychology"
            text.contains("hist") || text.contains("ancient") || text.contains("rome") ||
                text.contains("empire") || text.contains("civil") || text.contains("century") -> "History"
            text.contains("bio") || text.contains("gene") || text.contains("dna") ||
                text.contains("crispr") || text.contains("organism") || text.contains("cell") ||
                text.contains("evolution") -> "Biology"
            text.contains("philosoph") || text.contains("ethic") || text.contains("stoic") ||
                text.contains("exist") -> "Philosophy"
            text.contains("math") || text.contains("game theory") || text.contains("chaos") ||
                text.contains("logic") || text.contains("fractal") -> "Math"
            text.contains("art") || text.contains("music") || text.contains("paint") ||
                text.contains("literature") || text.contains("design") -> "Arts"
            category.isNotBlank() -> {
                val first = category.trim().split(" ", "/", "&").firstOrNull()?.trim() ?: "Insight"
                if (first.length > 12) first.take(10).replaceFirstChar { it.uppercase() }
                else first.replaceFirstChar { it.uppercase() }
            }
            else -> "Insight"
        }
    }

    fun getTopicColor(topic: String): Color {
        return when (topic.lowercase()) {
            "tech" -> Color(0xFF00E5FF)       // Cyan
            "science" -> Color(0xFF38BDF8)    // Sky Blue
            "psychology" -> Color(0xFFA855F7) // Purple
            "history" -> Color(0xFFF59E0B)    // Amber Gold
            "biology" -> Color(0xFF10B981)    // Emerald Green
            "philosophy" -> Color(0xFF8B5CF6) // Lavender
            "math" -> Color(0xFFEC4899)       // Pink
            "arts" -> Color(0xFFF43F5E)       // Rose
            else -> Color(0xFF00E5FF)
        }
    }

    fun addSparkToMindPalace(
        title: String,
        category: String,
        hook: String,
        xp: Int,
        fullContent: String = "",
        emoji: String = "🧠",
        steps: List<String> = emptyList(),
        funFact: String = "",
        context: Context? = null,
        completionSource: String? = null
    ) {
        // Keep the existing one-journal-entry-per-title behaviour.
        if (!journalHistory.any { it.title.equals(title, ignoreCase = true) }) {
            val now = System.currentTimeMillis()
            journalHistory.add(0, JournalEntry(
                id = java.util.UUID.randomUUID().toString(),
                category = extractShortTopic(category, title).uppercase(),
                xpGained = xp,
                title = title,
                quote = if (hook.isNotBlank()) hook.take(80) + if (hook.length > 80) "..." else "" else title,
                dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(now)),
                fullContent = fullContent,
                emoji = emoji,
                steps = steps,
                funFact = funFact,
                timestamp = now,
                completionSource = completionSource
            ))
        }
        if (completionSource == "interception_explore") {
            val index = journalHistory.indexOfFirst { it.title.equals(title, ignoreCase = true) }
            if (index >= 0) journalHistory[index] = journalHistory[index].copy(completionSource = completionSource)
        }
        rebuildPresentation()
        context?.let { ctx ->
            saveToLocal(ctx)
            GlobalUserState.saveProgress(ctx)
        }
    }

    // Rebuild presentation only: never invoke completeTask or award XP during restoration.
    private fun rebuildPresentation() {
        neuralNodes.clear()
        neuralConnections.clear()
        radarData.indices.forEach { i -> radarData[i] = radarData[i].copy(value = 0f) }
        journalHistory.asReversed().forEach { entry ->
            val topic = extractShortTopic(entry.category, entry.title)
            val layout = kotlin.random.Random(entry.id.hashCode())
            neuralNodes.add(NeuralNode(entry.id, topic,
                layout.nextFloat() * 1.6f - 0.8f, layout.nextFloat() * 1.6f - 0.8f,
                layout.nextFloat() * 1.6f - 0.8f, getTopicColor(topic), entry.title, topic))
            if (neuralNodes.size > 1) {
                neuralConnections.add(NeuralConnection(entry.id, neuralNodes[layout.nextInt(neuralNodes.size - 1)].id))
            }
            val index = radarData.indexOfFirst { it.label == topic }.takeIf { it >= 0 }
                ?: radarData.indexOfFirst { it.label == "Arts" }
            val point = radarData[index]
            radarData[index] = point.copy(value = (point.value + 25f).coerceAtMost(100f))
        }
        GlobalUserState.activeNeurons = neuralNodes.size
        GlobalUserState.completedTasks = maxOf(GlobalUserState.completedTasks, journalHistory.size)
    }

    fun completedHistory(): List<com.spark.data.network.CompletedTaskDto> = journalHistory.map {
        com.spark.data.network.CompletedTaskDto(it.id, it.timestamp, it.title, it.category,
            it.xpGained, it.dateStr, it.fullContent, it.quote, it.emoji, it.steps, it.funFact, it.completionSource)
    }

    fun restoreBackendHistory(context: Context, history: List<com.spark.data.network.CompletedTaskDto>?) {
        // Retain account-local entries that have not reached the server; deduplicate by stable ID.
        val entries = journalHistory.associateBy { it.id }.toMutableMap()
        history.orEmpty().forEach { task ->
            val title = task.title?.takeIf { it.isNotBlank() } ?: return@forEach
            val id = task.id?.takeIf { it.isNotBlank() } ?: java.util.UUID.nameUUIDFromBytes(
                "$title|${task.category}|${task.date}|${task.journal}".toByteArray(Charsets.UTF_8)).toString()
            entries[id] = JournalEntry(id, task.category.orEmpty(), task.xp, title,
                task.summary ?: title, task.date.orEmpty(), task.journal.orEmpty(),
                task.emoji ?: "🧠", task.steps.orEmpty(), task.funFact.orEmpty(), task.timestamp,
                task.completionSource ?: entries[id]?.completionSource)
        }
        journalHistory.clear()
        journalHistory.addAll(entries.values.sortedByDescending { it.timestamp })
        rebuildPresentation()
        saveToLocal(context)
    }

    val stats: MindPalaceStats
        get() {
            val count = maxOf(GlobalUserState.activeNeurons, neuralNodes.size)
            val currentXp = GlobalUserState.xp

            // Dynamically computed sync ratio (65% base up to 99%)
            val dynamicSyncRatio = when {
                count == 0 && currentXp == 0 -> 0
                else -> (65 + (count * 3) + (currentXp / 300)).coerceIn(65, 99)
            }

            // Dynamic intellect DNA rank based on active neurons & XP
            val dynamicRank = when {
                count == 0 && currentXp == 0 -> "Unranked"
                count >= 20 || currentXp >= 3000 -> "Top 0.5% (Master)"
                count >= 12 || currentXp >= 1800 -> "Top 1% (Synthesizer)"
                count >= 6 || currentXp >= 900 -> "Top 3% (Explorer)"
                count >= 2 || currentXp >= 300 -> "Top 5% (Scholar)"
                else -> "Top 10% (Initiate)"
            }

            val seqCode = 1000 + ((count * 137 + currentXp) % 9000)
            val dynamicSequenceId = "SPK-AOA-$seqCode"

            val topDomain = radarData.maxByOrNull { it.value }?.takeIf { it.value > 0f }?.label ?: "General"
            val peakMsg = if (count == 0) {
                "Generate a Spark to begin your neural journey."
            } else {
                "Dominant neural synthesis in $topDomain. Synchronizing cognitive pathways."
            }

            return MindPalaceStats(
                totalXp = "%,d".format(currentXp),
                activeNeurons = count,
                intellectDnaRank = dynamicRank,
                mindSequenceId = dynamicSequenceId,
                cognitiveDepth = if (GlobalUserState.cognitiveDepth > 0) GlobalUserState.cognitiveDepth else (count * 1.5),
                syncRatio = dynamicSyncRatio,
                synthesisPeakCategory = topDomain,
                synthesisPeakMessage = peakMsg
            )
        }

    val radarData = androidx.compose.runtime.mutableStateListOf(
        RadarDataPoint("Science", 0f),
        RadarDataPoint("Psychology", 0f),
        RadarDataPoint("History", 0f),
        RadarDataPoint("Math", 0f),
        RadarDataPoint("Philosophy", 0f),
        RadarDataPoint("Biology", 0f),
        RadarDataPoint("Tech", 0f),
        RadarDataPoint("Arts", 0f)
    )

    val journalHistory = androidx.compose.runtime.mutableStateListOf<JournalEntry>()

    // ── SharedPreferences Persistence ──────────────────────────

    fun saveToLocal(context: Context) {
        val prefs = com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME)
        prefs.edit()
            .putString("journal_history", gson.toJson(journalHistory.toList()))
            .remove("neural_nodes").remove("neural_connections").remove("radar_data")
            .apply()
    }

    fun resetActive() {
        neuralNodes.clear()
        neuralConnections.clear()
        journalHistory.clear()
        radarData.indices.forEach { i -> radarData[i] = radarData[i].copy(value = 0f) }
    }

    fun loadFromLocal(context: Context) {
        resetActive()
        val prefs = com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME)
        val journalJson = prefs.getString("journal_history", null)
        if (journalJson != null) {
            try {
                val type = object : TypeToken<List<JournalEntry>>() {}.type
                val entries: List<JournalEntry> = gson.fromJson(journalJson, type)
                journalHistory.addAll(entries.map { entry ->
                    // Upgrade old account-local numeric IDs once, without reading shared legacy data.
                    if (entry.id.toLongOrNull() != null) entry.copy(id = java.util.UUID.nameUUIDFromBytes(
                        "${entry.title}|${entry.category}|${entry.timestamp}".toByteArray(Charsets.UTF_8)).toString())
                    else entry
                }.distinctBy { it.id })
            } catch (e: Exception) { /* Keep an unreadable cache untouched. */ }
        }
        rebuildPresentation()
    }

    fun clearLocal(context: Context) {
        com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME).edit().clear().apply()
    }
}
