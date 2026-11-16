package com.spark.data.network

import com.spark.data.local.AccountSessionManager
import com.spark.data.local.DataStoreManager
import com.spark.ui.screens.dashboard.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

data class SuggestedQuestions(val questions: List<QuestionCardItem>, val isAI: Boolean)

/** All AI networking uses our authenticated backend. Guests and failures use bundled content. */
class SparkAIClient(private val api: SparkApiService, private val store: DataStoreManager) {
    private suspend fun <T> backendOrLocal(fallback: () -> T, request: suspend (String) -> T): T {
        val owner = AccountSessionManager.accountKey
        if (owner == null || owner == AccountSessionManager.GUEST) return fallback()
        val session = AccountSessionManager.generation
        fun checkSession() {
            if (session != AccountSessionManager.generation) throw CancellationException("Account changed")
        }
        try {
            val (token, storedOwner) = store.sessionFlow.first()
            checkSession()
            if (storedOwner != owner || token.isNullOrBlank()) return fallback()
            val result = request("Bearer $token")
            checkSession()
            return result
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            checkSession()
            return fallback()
        }
    }

    suspend fun generateSparkQuest(
        questionOrTopic: String,
        categoryPreference: String = "General",
        allowLocalFallback: Boolean = true,
        purpose: String = "manual"
    ): SparkTaskDto =
        backendOrLocal({
            check(allowLocalFallback) { "AI generation failed. Please try again." }
            LocalSparkContent.getCuratedHighQualityFallback(questionOrTopic)
        }) { token ->
            api.generateSpark(SparkRequest("curious", "30 minutes", categoryPreference,
                question = questionOrTopic, purpose = purpose), token).task.also {
                check(!it.title.isNullOrBlank() && !it.description.isNullOrBlank())
            }.copy(isAI = true)
        }

    suspend fun generateInterceptorSpark(topicPreference: String?, seenTitles: List<String> = emptyList()): SparkTaskDto =
        backendOrLocal({ LocalSparkContent.getCuratedHighQualityFallback(topicPreference.orEmpty()) }) { token ->
            api.generateSpark(SparkRequest("curious", "30 seconds", topicPreference ?: "Surprise me",
                seenTasks = seenTitles.takeLast(30), purpose = "interception"), token).task.also {
                check(!it.title.isNullOrBlank() && !it.description.isNullOrBlank())
            }.copy(isAI = true)
        }

    suspend fun generateFreshMixedQuestions(seenQuestions: Set<String>): SuggestedQuestions =
        backendOrLocal({ SuggestedQuestions(LocalSparkContent.getDiverseMultidisciplinaryMix(seenQuestions), false) }) { token ->
            val questions = api.generateQuestions(QuestionsRequest(seenQuestions.toList().takeLast(30)), token).questions
            check(questions.size == 6 && questions.all { it.question.isNotBlank() })
            SuggestedQuestions(questions, true)
        }

    suspend fun sendChatMessage(sparkTitle: String, sparkContent: String, history: List<ChatMessage>, newQuestion: String): String =
        backendOrLocal({ "AI temporarily unavailable. Please try again later." }) { token ->
            api.generateWormholeResponse(WormholeRequest(newQuestion, sparkTitle, sparkContent,
                history.takeLast(12).map { ChatTurn(if (it.isUser) "user" else "assistant", it.text) }), token)
                .answer.also { check(it.isNotBlank()) }
        }
}
