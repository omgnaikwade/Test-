package com.quest.cbt.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NtaStatus {
    NOT_VISITED,
    NOT_ANSWERED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED
}

@Serializable
data class QuestionAttemptState(
    val questionId: Int,
    val selectedOption: String? = null,
    val numericAnswer: String? = null,
    val status: NtaStatus = NtaStatus.NOT_VISITED,
    val timeSpentSeconds: Long = 0L,
    val scoreAwarded: Int = 0,
    val isCorrect: Boolean = false
)
