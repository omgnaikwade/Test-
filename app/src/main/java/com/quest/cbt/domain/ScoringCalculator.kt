package com.quest.cbt.domain

import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import kotlin.math.abs

data class SubjectScore(
    val subject: String,
    val score: Int,
    val maxScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val timeSpentSeconds: Long
)

data class TestScoreResult(
    val totalScore: Int,
    val maxScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val accuracyPercent: Float,
    val durationSeconds: Long,
    val subjectScores: Map<String, SubjectScore>,
    val evaluatedQuestions: Map<Int, QuestionAttemptState>
)

object ScoringCalculator {

    fun calculateScore(
        questions: List<QuestionJson>,
        answers: Map<Int, QuestionAttemptState>,
        marksCorrect: Int = 4,
        marksNegative: Int = 1,
        totalDurationSeconds: Long = 0L
    ): TestScoreResult {
        var totalScore = 0
        var totalCorrect = 0
        var totalWrong = 0
        var totalSkipped = 0

        val evaluatedMap = mutableMapOf<Int, QuestionAttemptState>()
        val subjectStats = mutableMapOf<String, MutableSubjectStats>()

        // Initialize subjects
        listOf("Physics", "Chemistry", "Maths").forEach { subj ->
            subjectStats[subj] = MutableSubjectStats(subj)
        }

        for (q in questions) {
            val stats = subjectStats.getOrPut(q.subject) { MutableSubjectStats(q.subject) }
            stats.questionCount++
            stats.maxScore += marksCorrect

            val attemptState = answers[q.id] ?: QuestionAttemptState(
                questionId = q.id,
                status = NtaStatus.NOT_VISITED
            )

            val isBonus = q.answer.trim().equals("BONUS", ignoreCase = true)
            val hasAnswered = attemptState.status == NtaStatus.ANSWERED ||
                    attemptState.status == NtaStatus.ANSWERED_AND_MARKED

            var awarded = 0
            var isCorrect = false

            if (isBonus) {
                // BONUS question: all candidates get full marks
                awarded = marksCorrect
                isCorrect = true
                totalCorrect++
                stats.correctCount++
            } else if (!hasAnswered) {
                // Unattempted or Marked for Review without answer
                awarded = 0
                isCorrect = false
                totalSkipped++
                stats.skippedCount++
            } else {
                // Evaluated answer
                if (q.type == "MCQ") {
                    val userAns = attemptState.selectedOption?.trim()?.uppercase()
                    val correctAns = q.answer.trim().uppercase()
                    if (userAns != null && userAns == correctAns) {
                        awarded = marksCorrect
                        isCorrect = true
                        totalCorrect++
                        stats.correctCount++
                    } else {
                        awarded = -marksNegative
                        isCorrect = false
                        totalWrong++
                        stats.wrongCount++
                    }
                } else {
                    // NUM type
                    val userNum = attemptState.numericAnswer?.trim()?.toDoubleOrNull()
                    val correctNum = q.answer.trim().toDoubleOrNull()
                    val tol = q.tolerance ?: 0.01

                    if (userNum != null && correctNum != null && abs(userNum - correctNum) <= tol) {
                        awarded = marksCorrect
                        isCorrect = true
                        totalCorrect++
                        stats.correctCount++
                    } else {
                        awarded = -marksNegative
                        isCorrect = false
                        totalWrong++
                        stats.wrongCount++
                    }
                }
            }

            totalScore += awarded
            stats.score += awarded
            stats.timeSpentSeconds += attemptState.timeSpentSeconds

            evaluatedMap[q.id] = attemptState.copy(
                scoreAwarded = awarded,
                isCorrect = isCorrect
            )
        }

        val totalAttempted = totalCorrect + totalWrong
        val accuracy = if (totalAttempted > 0) {
            (totalCorrect.toFloat() / totalAttempted) * 100f
        } else {
            0f
        }

        val finalSubjectScores = subjectStats.mapValues { (_, s) ->
            SubjectScore(
                subject = s.subject,
                score = s.score,
                maxScore = s.maxScore,
                correctCount = s.correctCount,
                wrongCount = s.wrongCount,
                skippedCount = s.skippedCount,
                timeSpentSeconds = s.timeSpentSeconds
            )
        }

        val totalMaxScore = questions.size * marksCorrect

        return TestScoreResult(
            totalScore = totalScore,
            maxScore = totalMaxScore,
            correctCount = totalCorrect,
            wrongCount = totalWrong,
            skippedCount = totalSkipped,
            accuracyPercent = accuracy,
            durationSeconds = totalDurationSeconds,
            subjectScores = finalSubjectScores,
            evaluatedQuestions = evaluatedMap
        )
    }

    private class MutableSubjectStats(
        val subject: String,
        var score: Int = 0,
        var maxScore: Int = 0,
        var correctCount: Int = 0,
        var wrongCount: Int = 0,
        var skippedCount: Int = 0,
        var questionCount: Int = 0,
        var timeSpentSeconds: Long = 0L
    )
}
