package com.quest.cbt.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.data.model.TestJson
import com.quest.cbt.data.repository.AttemptRepository
import com.quest.cbt.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Subject-wise breakdown row for the NTA-style summary table.
 */
data class SubjectResultStat(
    val subject: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val scoreAwarded: Int,
    val maxScore: Int,
    val accuracyPercent: Float,
    val timeSpentSeconds: Long,
    val answeredCount: Int = 0,
    val notAnsweredCount: Int = 0,
    val markedForReviewCount: Int = 0,
    val answeredAndMarkedCount: Int = 0,
    val notVisitedCount: Int = 0
)

/**
 * NTA-style summary table containing per-subject metrics and overall totals.
 */
data class NtaSummaryTable(
    val rows: List<SubjectResultStat>,
    val totalQuestions: Int,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val totalUnattempted: Int,
    val totalScore: Int,
    val totalMaxScore: Int,
    val totalAccuracyPercent: Float,
    val totalTimeSpentSeconds: Long,
    val performanceRating: String
)

sealed interface ResultSummaryUiState {
    data object Loading : ResultSummaryUiState
    data class Success(
        val attempt: AttemptEntity,
        val test: TestEntity?,
        val summaryTable: NtaSummaryTable,
        val questions: List<QuestionJson>,
        val answersMap: Map<Int, QuestionAttemptState>,
        val timeTakenFormatted: String
    ) : ResultSummaryUiState
    data class Error(val message: String) : ResultSummaryUiState
}

class ResultSummaryViewModel(
    private val attemptId: String,
    private val attemptRepository: AttemptRepository,
    private val testRepository: TestRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<ResultSummaryUiState>(ResultSummaryUiState.Loading)
    val uiState: StateFlow<ResultSummaryUiState> = _uiState.asStateFlow()

    init {
        loadAndProcessAttempt()
    }

    fun refresh() {
        loadAndProcessAttempt()
    }

    private fun loadAndProcessAttempt() {
        viewModelScope.launch {
            _uiState.value = ResultSummaryUiState.Loading
            try {
                val attempt = attemptRepository.getAttemptById(attemptId)
                if (attempt == null) {
                    _uiState.value = ResultSummaryUiState.Error("Attempt not found with ID: $attemptId")
                    return@launch
                }

                val test = testRepository.getTestByIdSuspend(attempt.testId)
                val questions = if (test != null) {
                    try {
                        val parsed = json.decodeFromString<TestJson>(test.questionsJson)
                        parsed.questions
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val answersMap: Map<Int, QuestionAttemptState> = try {
                    json.decodeFromString(attempt.answersStateJson)
                } catch (_: Exception) {
                    emptyMap()
                }

                val summaryTable = generateSummaryTable(attempt, test, questions, answersMap)

                val hours = attempt.durationSeconds / 3600
                val minutes = (attempt.durationSeconds % 3600) / 60
                val seconds = attempt.durationSeconds % 60
                val timeTakenFormatted = buildString {
                    if (hours > 0) append("${hours}h ")
                    append("${minutes}m ${seconds}s")
                }

                _uiState.value = ResultSummaryUiState.Success(
                    attempt = attempt,
                    test = test,
                    summaryTable = summaryTable,
                    questions = questions,
                    answersMap = answersMap,
                    timeTakenFormatted = timeTakenFormatted
                )
            } catch (e: Exception) {
                _uiState.value = ResultSummaryUiState.Error(
                    e.localizedMessage ?: "Failed to process test results."
                )
            }
        }
    }

    /**
     * Processes completed test attempts and generates the NTA-style summary table.
     */
    fun generateSummaryTable(
        attempt: AttemptEntity,
        test: TestEntity?,
        questions: List<QuestionJson>,
        answersMap: Map<Int, QuestionAttemptState>
    ): NtaSummaryTable {
        val marksCorrect = test?.marksCorrect ?: 4
        val subjects = listOf("Physics", "Chemistry", "Maths")

        val rows = subjects.map { subject ->
            val subjQuestions = questions.filter { it.subject == subject }
            var correct = 0
            var incorrect = 0
            var unattempted = 0
            var score = 0
            var timeSpent = 0L

            var answered = 0
            var notAnswered = 0
            var marked = 0
            var ansMarked = 0
            var notVisited = 0

            subjQuestions.forEach { q ->
                val qState = answersMap[q.id]
                val isBonus = q.answer.trim().equals("BONUS", ignoreCase = true)

                // Track NTA palette statuses
                when (qState?.status) {
                    NtaStatus.ANSWERED -> answered++
                    NtaStatus.NOT_ANSWERED -> notAnswered++
                    NtaStatus.MARKED_FOR_REVIEW -> marked++
                    NtaStatus.ANSWERED_AND_MARKED -> ansMarked++
                    NtaStatus.NOT_VISITED, null -> notVisited++
                }

                if (qState != null) {
                    timeSpent += qState.timeSpentSeconds
                    score += qState.scoreAwarded

                    if (isBonus || qState.isCorrect) {
                        correct++
                    } else if (qState.status == NtaStatus.ANSWERED ||
                        qState.status == NtaStatus.ANSWERED_AND_MARKED ||
                        qState.scoreAwarded < 0
                    ) {
                        incorrect++
                    } else {
                        unattempted++
                    }
                } else {
                    if (isBonus) {
                        correct++
                        score += marksCorrect
                    } else {
                        unattempted++
                    }
                }
            }

            val maxScore = subjQuestions.size * marksCorrect
            val totalAttempted = correct + incorrect
            val accuracy = if (totalAttempted > 0) {
                (correct.toFloat() / totalAttempted) * 100f
            } else {
                0f
            }

            SubjectResultStat(
                subject = subject,
                totalQuestions = subjQuestions.size,
                correctCount = correct,
                incorrectCount = incorrect,
                unattemptedCount = unattempted,
                scoreAwarded = score,
                maxScore = maxScore,
                accuracyPercent = accuracy,
                timeSpentSeconds = timeSpent,
                answeredCount = answered,
                notAnsweredCount = notAnswered,
                markedForReviewCount = marked,
                answeredAndMarkedCount = ansMarked,
                notVisitedCount = notVisited
            )
        }

        val totalQ = rows.sumOf { it.totalQuestions }
        val totalCorr = rows.sumOf { it.correctCount }
        val totalIncorr = rows.sumOf { it.incorrectCount }
        val totalUnatt = rows.sumOf { it.unattemptedCount }
        val totalSc = rows.sumOf { it.scoreAwarded }.let { if (it != 0) it else attempt.totalScore }
        val totalMaxSc = rows.sumOf { it.maxScore }.let { if (it > 0) it else attempt.maxScore }
        val totalTime = rows.sumOf { it.timeSpentSeconds }.let { if (it > 0) it else attempt.durationSeconds }

        val totalAttempted = totalCorr + totalIncorr
        val totalAccuracy = if (totalAttempted > 0) {
            (totalCorr.toFloat() / totalAttempted) * 100f
        } else {
            attempt.accuracyPercent
        }

        val rating = when {
            totalSc >= 240 -> "Outstanding (99.8+ %ile Target)"
            totalSc >= 190 -> "Excellent (99.0+ %ile Target)"
            totalSc >= 140 -> "Strong (97.0+ %ile Target)"
            totalSc >= 100 -> "Moderate (92.0+ %ile Target)"
            else -> "Needs Improvement (< 90 %ile)"
        }

        return NtaSummaryTable(
            rows = rows,
            totalQuestions = totalQ,
            totalCorrect = totalCorr,
            totalIncorrect = totalIncorr,
            totalUnattempted = totalUnatt,
            totalScore = totalSc,
            totalMaxScore = totalMaxSc,
            totalAccuracyPercent = totalAccuracy,
            totalTimeSpentSeconds = totalTime,
            performanceRating = rating
        )
    }

    companion object {
        fun provideFactory(
            attemptId: String,
            attemptRepository: AttemptRepository,
            testRepository: TestRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ResultSummaryViewModel(attemptId, attemptRepository, testRepository) as T
            }
        }
    }
}
