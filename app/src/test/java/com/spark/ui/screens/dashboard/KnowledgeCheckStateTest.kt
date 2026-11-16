package com.spark.ui.screens.dashboard

import com.google.gson.Gson
import com.spark.data.network.*
import com.spark.ui.screens.main.JournalEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import java.io.IOException

class KnowledgeCheckStateTest {
    private fun unusedApi() = Proxy.newProxyInstance(SparkApiService::class.java.classLoader,
        arrayOf(SparkApiService::class.java)) { _, _, _ -> error("Unexpected API call") } as SparkApiService
    private val questions = List(5) { ReviewQuestion("source-$it", "Topic $it", "Question $it?", listOf("a", "b", "c", "d")) }
    private val result = ReviewResult("attempt", "2026-11-04T12:00:00Z", 4, 5,
        List(5) { ReviewFeedback("Topic $it", "Question $it?", listOf("a", "b", "c", "d"),
            if (it == 0) 1 else 0, 0, "A short explanation of the answer.") })

    @Test fun selectingAnswersAndSubmittingUsesOnlyIndexesThenShowsServerScore() = runBlocking {
        val api = object : SparkApiService by unusedApi() {
            override suspend fun getReviewHistory() = ReviewHistory(5, emptyList())
            override suspend fun startReview() = ReviewStarted("attempt", questions)
            override suspend fun submitReview(id: String, submission: ReviewSubmission): ReviewResult {
                assertEquals("attempt", id)
                return result.also { assertEquals(listOf(1, 0, 0, 0, 0), submission.answers) }
            }
        }
        val state = KnowledgeCheckState(api) { true }
        state.loadHistory()
        state.start()
        state.next()
        assertEquals(0, state.questionIndex)
        repeat(5) { index ->
            state.select(if (index == 0) 1 else 0)
            if (index < 4) state.next()
        }
        state.submit()
        assertEquals(4, state.result!!.correctCount)
        assertEquals(80, state.result!!.correctCount * 100 / state.result!!.totalCount)
        assertEquals(1, state.result!!.questions.count { it.correctOptionIndex != it.selectedOptionIndex })
        assertEquals(result, state.history!!.results.single())
        state.showHistory()
        assertNull(state.started)
        assertNull(state.result)
        assertTrue(state.answers.isEmpty())
    }

    @Test fun incompleteAnswersAreNeverSubmitted() = runBlocking {
        val api = object : SparkApiService by unusedApi() {
            override suspend fun startReview() = ReviewStarted("attempt", questions)
        }
        val state = KnowledgeCheckState(api) { true }
        state.start()
        state.select(0)
        state.submit()
        assertNull(state.result)
    }

    @Test fun loadingAndFailureStatesAllowManualRetry() = runBlocking {
        var fail = true
        lateinit var state: KnowledgeCheckState
        val api = object : SparkApiService by unusedApi() {
            override suspend fun getReviewHistory(): ReviewHistory {
                assertTrue(state.loading)
                if (fail) throw IOException("Offline")
                return ReviewHistory(2, emptyList())
            }
        }
        state = KnowledgeCheckState(api) { true }
        state.loadHistory()
        assertNotNull(state.error)
        assertFalse(state.loading)
        fail = false
        state.loadHistory()
        assertNull(state.error)
        assertEquals(2, state.history!!.eligibleCount)
    }

    @Test fun accountChangeDiscardsInFlightResponseAndNewAccountStartsEmpty() = runBlocking {
        var current = true
        val api = object : SparkApiService by unusedApi() {
            override suspend fun startReview(): ReviewStarted {
                current = false
                return ReviewStarted("old-account", questions)
            }
        }
        val old = KnowledgeCheckState(api) { current }
        old.start()
        assertNull(old.started)
        val next = KnowledgeCheckState(api) { true }
        assertNull(next.history)
        assertNull(next.result)
        assertTrue(next.answers.isEmpty())
        assertFalse(next.loading)
    }

    @Test fun exploreSourceSurvivesLocalAndApiJsonWhileLegacyRemainsUnmarked() {
        val gson = Gson()
        val entry = JournalEntry("id", "Science", 150, "A question?", "Summary", "04 Nov 2026",
            fullContent = "Learning content", completionSource = "interception_explore")
        assertEquals("interception_explore", gson.fromJson(gson.toJson(entry), JournalEntry::class.java).completionSource)
        val dto = CompletedTaskDto(id = "id", completionSource = "interception_explore")
        assertEquals("interception_explore", gson.fromJson(gson.toJson(dto), CompletedTaskDto::class.java).completionSource)
        assertNull(gson.fromJson("{\"id\":\"legacy\"}", CompletedTaskDto::class.java).completionSource)
    }
}
