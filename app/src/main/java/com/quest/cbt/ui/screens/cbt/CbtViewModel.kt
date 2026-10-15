package com.quest.cbt.ui.screens.cbt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.data.model.TestJson
import com.quest.cbt.data.repository.AttemptRepository
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.domain.ScoringCalculator
import com.quest.cbt.domain.TestScoreResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

data class CbtUiState(
    val isLoading: Boolean = true,
    val test: TestEntity? = null,
    val questions: List<QuestionJson> = emptyList(),
    val currentSubject: String = "Physics",
    val currentQuestionIndex: Int = 0,
    val answers: Map<Int, QuestionAttemptState> = emptyMap(),
    val remainingSeconds: Long = 0L,
    val isTimerRunning: Boolean = false,
    val isPaletteDrawerOpen: Boolean = false,
    val isSubmitDialogOpen: Boolean = false,
    val isQuestionPaperDialogOpen: Boolean = false,
    val isInstructionsDialogOpen: Boolean = false,
    val isExitConfirmDialogOpen: Boolean = false,
    val currentAttemptId: String = ""
)

sealed class CbtEvent {
    data class Submitted(val attemptId: String) : CbtEvent()
}

class CbtViewModel(
    private val testId: String,
    private val testRepository: TestRepository,
    private val attemptRepository: AttemptRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(CbtUiState())
    val uiState: StateFlow<CbtUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CbtEvent>()
    val events: SharedFlow<CbtEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var absoluteEndTimeMs: Long = 0L
    private var currentQuestionStartTimeMs: Long = System.currentTimeMillis()

    init {
        loadTestAndInitialize()
    }

    private fun loadTestAndInitialize() {
        viewModelScope.launch {
            val test = testRepository.getTestByIdSuspend(testId) ?: return@launch
            val parsedQuestions = try {
                val parsed = json.decodeFromString<TestJson>(test.questionsJson)
                parsed.questions
            } catch (_: Exception) {
                emptyList()
            }

            // Check if there is an in-progress attempt to resume
            val inProgress = attemptRepository.getInProgressAttempt(testId)
            val attemptId = inProgress?.attemptId ?: UUID.randomUUID().toString()

            val existingAnswers = if (inProgress != null) {
                try {
                    json.decodeFromString<Map<Int, QuestionAttemptState>>(inProgress.answersStateJson)
                } catch (_: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val answersMap = mutableMapOf<Int, QuestionAttemptState>()
            parsedQuestions.forEach { q ->
                val prev = existingAnswers[q.id]
                answersMap[q.id] = prev ?: QuestionAttemptState(
                    questionId = q.id,
                    status = NtaStatus.NOT_VISITED
                )
            }

            // Set first question to NOT_ANSWERED if not visited
            if (answersMap.isNotEmpty()) {
                val firstQ = parsedQuestions.first()
                if (answersMap[firstQ.id]?.status == NtaStatus.NOT_VISITED) {
                    answersMap[firstQ.id] = answersMap[firstQ.id]!!.copy(status = NtaStatus.NOT_ANSWERED)
                }
            }

            val totalDurationMs = test.durationMin * 60 * 1000L
            val startTime = inProgress?.startTime ?: System.currentTimeMillis()
            absoluteEndTimeMs = inProgress?.endTime ?: (startTime + totalDurationMs)

            val now = System.currentTimeMillis()
            val remainingSec = maxOf(0L, (absoluteEndTimeMs - now) / 1000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    test = test,
                    questions = parsedQuestions,
                    currentSubject = parsedQuestions.firstOrNull()?.subject ?: "Physics",
                    currentQuestionIndex = 0,
                    answers = answersMap,
                    remainingSeconds = remainingSec,
                    isTimerRunning = true,
                    currentAttemptId = attemptId
                )
            }

            currentQuestionStartTimeMs = System.currentTimeMillis()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val rem = maxOf(0L, (absoluteEndTimeMs - now) / 1000)
                _uiState.update { it.copy(remainingSeconds = rem) }

                if (rem <= 0L) {
                    autoSubmit()
                    break
                }
                delay(1000)
            }
        }
    }

    fun selectSubject(subject: String) {
        val questions = _uiState.value.questions
        val firstIdx = questions.indexOfFirst { it.subject == subject }
        if (firstIdx >= 0) {
            navigateToQuestion(firstIdx)
        }
    }

    fun navigateToQuestion(index: Int) {
        val state = _uiState.value
        val questions = state.questions
        if (index !in questions.indices) return

        recordTimeSpentOnCurrentQuestion()

        val targetQ = questions[index]
        val currentAnswers = state.answers.toMutableMap()
        val targetState = currentAnswers[targetQ.id] ?: QuestionAttemptState(targetQ.id)

        // If target was NOT_VISITED, mark as NOT_ANSWERED when visited
        if (targetState.status == NtaStatus.NOT_VISITED) {
            currentAnswers[targetQ.id] = targetState.copy(status = NtaStatus.NOT_ANSWERED)
        }

        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentSubject = targetQ.subject,
                answers = currentAnswers
            )
        }
        currentQuestionStartTimeMs = System.currentTimeMillis()
        saveInProgressAttempt()
    }

    fun selectMcqOption(optionLetter: String) {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)

        currentAnswers[currentQ.id] = qState.copy(selectedOption = optionLetter)
        _uiState.update { it.copy(answers = currentAnswers) }
    }

    fun setNumericAnswer(value: String) {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)

        currentAnswers[currentQ.id] = qState.copy(numericAnswer = value)
        _uiState.update { it.copy(answers = currentAnswers) }
    }

    fun saveAndNext() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)

        val hasAnswer = (currentQ.type == "MCQ" && !qState.selectedOption.isNullOrBlank()) ||
                (currentQ.type == "NUM" && !qState.numericAnswer.isNullOrBlank())

        val newStatus = if (hasAnswer) NtaStatus.ANSWERED else NtaStatus.NOT_ANSWERED
        currentAnswers[currentQ.id] = qState.copy(status = newStatus)

        _uiState.update { it.copy(answers = currentAnswers) }

        // Move to next question if available
        if (state.currentQuestionIndex + 1 < state.questions.size) {
            navigateToQuestion(state.currentQuestionIndex + 1)
        } else {
            saveInProgressAttempt()
        }
    }

    fun clearResponse() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)

        currentAnswers[currentQ.id] = qState.copy(
            selectedOption = null,
            numericAnswer = null,
            status = NtaStatus.NOT_ANSWERED
        )

        _uiState.update { it.copy(answers = currentAnswers) }
        saveInProgressAttempt()
    }

    fun markForReviewAndNext() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)

        val hasAnswer = (currentQ.type == "MCQ" && !qState.selectedOption.isNullOrBlank()) ||
                (currentQ.type == "NUM" && !qState.numericAnswer.isNullOrBlank())

        val newStatus = if (hasAnswer) {
            NtaStatus.ANSWERED_AND_MARKED
        } else {
            NtaStatus.MARKED_FOR_REVIEW
        }

        currentAnswers[currentQ.id] = qState.copy(status = newStatus)
        _uiState.update { it.copy(answers = currentAnswers) }

        if (state.currentQuestionIndex + 1 < state.questions.size) {
            navigateToQuestion(state.currentQuestionIndex + 1)
        } else {
            saveInProgressAttempt()
        }
    }

    fun navigatePrevious() {
        val state = _uiState.value
        if (state.currentQuestionIndex > 0) {
            navigateToQuestion(state.currentQuestionIndex - 1)
        }
    }

    private fun recordTimeSpentOnCurrentQuestion() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val timeSpent = maxOf(0L, (System.currentTimeMillis() - currentQuestionStartTimeMs) / 1000)

        val currentAnswers = state.answers.toMutableMap()
        val qState = currentAnswers[currentQ.id] ?: QuestionAttemptState(currentQ.id)
        currentAnswers[currentQ.id] = qState.copy(
            timeSpentSeconds = qState.timeSpentSeconds + timeSpent
        )
        _uiState.update { it.copy(answers = currentAnswers) }
    }

    fun togglePaletteDrawer(open: Boolean) {
        _uiState.update { it.copy(isPaletteDrawerOpen = open) }
    }

    fun toggleSubmitDialog(open: Boolean) {
        _uiState.update { it.copy(isSubmitDialogOpen = open) }
    }

    fun toggleQuestionPaperDialog(open: Boolean) {
        _uiState.update { it.copy(isQuestionPaperDialogOpen = open) }
    }

    fun toggleInstructionsDialog(open: Boolean) {
        _uiState.update { it.copy(isInstructionsDialogOpen = open) }
    }

    fun toggleExitConfirmDialog(open: Boolean) {
        _uiState.update { it.copy(isExitConfirmDialogOpen = open) }
    }

    private fun autoSubmit() {
        submitTest()
    }

    fun submitTest() {
        timerJob?.cancel()
        recordTimeSpentOnCurrentQuestion()

        viewModelScope.launch {
            val state = _uiState.value
            val test = state.test ?: return@launch

            val totalDurationSec = (test.durationMin * 60) - state.remainingSeconds

            val scoreResult = ScoringCalculator.calculateScore(
                questions = state.questions,
                answers = state.answers,
                marksCorrect = test.marksCorrect,
                marksNegative = test.marksNegative,
                totalDurationSeconds = maxOf(0L, totalDurationSec)
            )

            val pScore = scoreResult.subjectScores["Physics"]
            val cScore = scoreResult.subjectScores["Chemistry"]
            val mScore = scoreResult.subjectScores["Maths"]

            val answersJsonStr = json.encodeToString(scoreResult.evaluatedQuestions)

            val attemptEntity = AttemptEntity(
                attemptId = state.currentAttemptId,
                testId = test.id,
                testTitle = test.title,
                startTime = System.currentTimeMillis() - (totalDurationSec * 1000L),
                endTime = System.currentTimeMillis(),
                durationSeconds = totalDurationSec,
                totalScore = scoreResult.totalScore,
                maxScore = scoreResult.maxScore,
                correctCount = scoreResult.correctCount,
                wrongCount = scoreResult.wrongCount,
                skippedCount = scoreResult.skippedCount,
                accuracyPercent = scoreResult.accuracyPercent,
                physicsScore = pScore?.score ?: 0,
                physicsMax = pScore?.maxScore ?: 100,
                chemistryScore = cScore?.score ?: 0,
                chemistryMax = cScore?.maxScore ?: 100,
                mathsScore = mScore?.score ?: 0,
                mathsMax = mScore?.maxScore ?: 100,
                answersStateJson = answersJsonStr,
                isCompleted = true
            )

            attemptRepository.saveAttempt(attemptEntity)
            _events.emit(CbtEvent.Submitted(state.currentAttemptId))
        }
    }

    private fun saveInProgressAttempt() {
        viewModelScope.launch {
            val state = _uiState.value
            val test = state.test ?: return@launch
            val answersJsonStr = json.encodeToString(state.answers)

            val attemptEntity = AttemptEntity(
                attemptId = state.currentAttemptId,
                testId = test.id,
                testTitle = test.title,
                startTime = absoluteEndTimeMs - (test.durationMin * 60 * 1000L),
                endTime = absoluteEndTimeMs,
                answersStateJson = answersJsonStr,
                isCompleted = false
            )
            attemptRepository.saveAttempt(attemptEntity)
        }
    }
}
