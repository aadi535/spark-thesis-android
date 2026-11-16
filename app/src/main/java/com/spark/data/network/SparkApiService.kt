package com.spark.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data Models
data class GoogleAuthRequest(val idToken: String)
data class AuthResponse(val token: String, val email: String, val name: String, @com.google.gson.annotations.SerializedName(value = "pro", alternate = ["isPro"]) val isPro: Boolean)

data class UserProgressDto(
    val name: String,
    val xp: Int,
    val streak: Int,
    val lastClaimDate: String?,
    @com.google.gson.annotations.SerializedName(value = "pro", alternate = ["isPro"]) val isPro: Boolean,
    val proExpiresAt: String?,
    val dailySparksCount: Int,
    val dailyWormholeCount: Int,
    val couponRedeemed: Boolean,
    val completedTasks: List<CompletedTaskDto>? = null
)

// Field names match the existing backend CompletedTaskDto; optional fields support older records.
data class CompletedTaskDto(
    val id: String? = null,
    val timestamp: Long = 0,
    val title: String? = null,
    val category: String? = null,
    val xp: Int = 0,
    val date: String? = null,
    val journal: String? = null,
    val summary: String? = null,
    val emoji: String? = null,
    val steps: List<String>? = null,
    val funFact: String? = null,
    val completionSource: String? = null
)

data class SparkRequest(
    val mood: String,
    val time: String,
    val interests: String,
    val seenTasks: List<String> = emptyList(),
    val question: String? = null,
    val purpose: String = "manual"
)

data class SparkLink(val text: String?, val url: String?)

data class WormholeRequest(
    val question: String,
    val sparkTitle: String,
    val sparkDescription: String,
    val history: List<ChatTurn> = emptyList()
)

data class ChatTurn(val role: String, val content: String)
data class QuestionsRequest(val seenQuestions: List<String>)
data class QuestionsResponse(val questions: List<QuestionCardItem>)

data class WormholeResponse(
    val answer: String
)

data class SparkTaskDto(
    val category: String?,
    val icon: String?,
    val color: String?,
    val title: String?,
    val hook: String?,
    val description: String?,
    val steps: List<String>?,
    val funFact: String?,
    val xp: Int,
    val badge: String?,
    @com.google.gson.annotations.SerializedName(value = "ai", alternate = ["isAI"]) val isAI: Boolean,
    val link: SparkLink?
)

data class SparkResponse(
    val task: SparkTaskDto
)

data class LeaderboardEntry(val position: Long?, val name: String, val xp: Int, val currentUser: Boolean)
data class LeaderboardResponse(val entries: List<LeaderboardEntry>, val currentUser: LeaderboardEntry?)

interface SparkApiService {

    @POST("api/v1/reviews/start")
    suspend fun startReview(): ReviewStarted

    @POST("api/v1/reviews/{id}/submit")
    suspend fun submitReview(@retrofit2.http.Path("id") id: String, @Body submission: ReviewSubmission): ReviewResult

    @GET("api/v1/reviews/history")
    suspend fun getReviewHistory(): ReviewHistory

    @GET("api/v1/user/leaderboard")
    suspend fun getLeaderboard(): LeaderboardResponse

    // Auth
    @POST("api/v1/auth/google")
    suspend fun authenticateWithGoogle(@Body request: GoogleAuthRequest): AuthResponse

    @retrofit2.http.DELETE("api/v1/user/delete")
    suspend fun deleteAccount(@retrofit2.http.Header("Authorization") authorization: String)

    // User Progress
    @GET("api/v1/user/progress")
    suspend fun getUserProgress(@retrofit2.http.Header("Authorization") authorization: String? = null): UserProgressDto

    @POST("api/v1/user/progress")
    suspend fun saveUserProgress(@Body progress: UserProgressDto, @retrofit2.http.Header("Authorization") authorization: String? = null): UserProgressDto

    @POST("api/v1/spark/wormhole")
    suspend fun generateWormholeResponse(@Body request: WormholeRequest, @retrofit2.http.Header("Authorization") authorization: String? = null): WormholeResponse

    @POST("api/v1/spark")
    suspend fun generateSpark(@Body request: SparkRequest, @retrofit2.http.Header("Authorization") authorization: String? = null): SparkResponse

    @POST("api/v1/spark/questions")
    suspend fun generateQuestions(@Body request: QuestionsRequest, @retrofit2.http.Header("Authorization") authorization: String): QuestionsResponse
}
