package com.spark.ui.screens.dashboard

import com.spark.data.local.AccountSessionManager
import com.spark.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spark.data.network.LocalSparkContent
import com.spark.data.network.SparkApiService
import com.spark.data.network.SparkTaskDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Idle : DashboardState()
    object Generating : DashboardState()
    data class Generated(val spark: SparkTaskDto) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(
    private val aiClient: com.spark.data.network.SparkAIClient
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var lastGeneratedSpark: SparkTaskDto? = null

    val isSparkExpanded = MutableStateFlow(false)
    val currentSpark = MutableStateFlow<GeneratedSpark?>(null)
    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    init {
        viewModelScope.launch {
            androidx.compose.runtime.snapshotFlow { AccountSessionManager.accountKey }.collect {
                closeSpark()
            }
        }
    }

    fun generateSpark(mood: String, time: String, topic: String) {
        val session = AccountSessionManager.generation
        viewModelScope.launch {
            _state.value = DashboardState.Generating
            try {
                val task = aiClient.generateSparkQuest(
                    questionOrTopic = topic,
                    categoryPreference = mood
                )
                if (session != AccountSessionManager.generation) return@launch
                lastGeneratedSpark = task
                _state.value = DashboardState.Generated(task)
            } catch (e: Exception) {
                if (session != AccountSessionManager.generation) return@launch
                val fallback = LocalSparkContent.getCuratedHighQualityFallback(topic)
                lastGeneratedSpark = fallback
                _state.value = DashboardState.Generated(fallback)
            }
        }
    }

    fun resetState() {
        _state.value = DashboardState.Idle
    }

    fun closeSpark() {
        lastGeneratedSpark = null
        isSparkExpanded.value = false
        currentSpark.value = null
        chatMessages.value = emptyList()
        resetState()
    }

    suspend fun sendWormholeMessage(
        question: String,
        sparkTitle: String,
        sparkDescription: String,
        history: List<ChatMessage> = emptyList()
    ): String {
        val session = AccountSessionManager.generation
        val answer = aiClient.sendChatMessage(
            sparkTitle = sparkTitle,
            sparkContent = sparkDescription,
            history = history,
            newQuestion = question
        )
        if (session != AccountSessionManager.generation) throw kotlinx.coroutines.CancellationException("Account changed")
        return answer
    }
}
