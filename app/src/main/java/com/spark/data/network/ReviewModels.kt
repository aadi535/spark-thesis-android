package com.spark.data.network

data class ReviewQuestion(val sourceEntryId: String, val topic: String, val question: String, val options: List<String>)
data class ReviewStarted(val id: String, val questions: List<ReviewQuestion>)
data class ReviewSubmission(val answers: List<Int>)
data class ReviewFeedback(val topic: String, val question: String, val options: List<String>,
                          val selectedOptionIndex: Int, val correctOptionIndex: Int, val explanation: String)
data class ReviewResult(val id: String, val completedAt: String, val correctCount: Int,
                        val totalCount: Int, val questions: List<ReviewFeedback>)
data class ReviewHistory(val eligibleCount: Int, val results: List<ReviewResult>)
