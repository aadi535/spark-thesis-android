package com.spark.ui.screens.dashboard

import androidx.compose.runtime.*
import com.spark.data.network.*
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class KnowledgeCheckState(private val api: SparkApiService, private val sessionIsCurrent: () -> Boolean) {
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var history by mutableStateOf<ReviewHistory?>(null)
        private set
    var started by mutableStateOf<ReviewStarted?>(null)
        private set
    var result by mutableStateOf<ReviewResult?>(null)
        private set
    var answers by mutableStateOf<List<Int?>>(emptyList())
        private set
    var questionIndex by mutableIntStateOf(0)
        private set

    suspend fun loadHistory() = request {
        val response = api.getReviewHistory()
        if (sessionIsCurrent()) history = response
    }

    suspend fun start() = request {
        val response = api.startReview()
        if (sessionIsCurrent()) {
            started = response
            result = null
            answers = List(response.questions.size) { null }
            questionIndex = 0
        }
    }

    fun select(option: Int) {
        if (!sessionIsCurrent() || loading || result != null || option !in 0..3 || questionIndex !in answers.indices) return
        answers = answers.toMutableList().also { it[questionIndex] = option }
    }

    fun next() {
        if (sessionIsCurrent() && !loading && answers.getOrNull(questionIndex) != null && questionIndex < answers.lastIndex)
            questionIndex++
    }

    suspend fun submit() {
        val attempt = started ?: return
        if (answers.size != 5 || answers.any { it == null }) return
        request {
            val response = api.submitReview(attempt.id, ReviewSubmission(answers.filterNotNull()))
            if (sessionIsCurrent()) {
                result = response
                history = history?.copy(results = (listOf(response) + history!!.results.filter { it.id != response.id }).take(20))
            }
        }
    }

    fun viewResult(value: ReviewResult) { if (sessionIsCurrent() && !loading) result = value }
    fun showHistory() {
        if (!sessionIsCurrent() || loading) return
        started = null
        result = null
        answers = emptyList()
        questionIndex = 0
        error = null
    }

    private suspend fun request(action: suspend () -> Unit) {
        if (loading || !sessionIsCurrent()) return
        loading = true
        error = null
        try { action() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (sessionIsCurrent()) error = when {
                e is HttpException && e.code() == 409 -> "Explore five different ideas and wait for your history to sync."
                e is HttpException && (e.code() == 401 || e.code() == 403) -> "Please sign in again."
                else -> "Knowledge Check is unavailable. Please try again."
            }
        } finally { if (sessionIsCurrent()) loading = false }
    }
}
