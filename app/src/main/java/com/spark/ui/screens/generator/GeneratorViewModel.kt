package com.spark.ui.screens.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spark.data.network.SparkApiService
import com.spark.data.network.SparkRequest
import com.spark.data.network.SparkTaskDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GeneratorState {
    object Idle : GeneratorState()
    object Generating : GeneratorState()
    data class Success(val spark: SparkTaskDto) : GeneratorState()
    data class Error(val message: String) : GeneratorState()
}

class GeneratorViewModel(
    private val aiClient: com.spark.data.network.SparkAIClient
) : ViewModel() {

    private val _state = MutableStateFlow<GeneratorState>(GeneratorState.Idle)
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    fun generateSpark(mood: String, time: String, interests: String) {
        viewModelScope.launch {
            _state.value = GeneratorState.Generating
            try {
                val spark = aiClient.generateSparkQuest(interests, mood)
                _state.value = GeneratorState.Success(spark)
            } catch (e: Exception) {
                _state.value = GeneratorState.Error(e.message ?: "Failed to generate Spark. Ensure backend is running.")
            }
        }
    }
    
    fun resetState() {
        _state.value = GeneratorState.Idle
    }
}
