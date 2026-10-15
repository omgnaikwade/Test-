package com.quest.cbt

import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.OptionJson
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.ui.screens.result.ResultSummaryViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultSummaryViewModelTest {

    @Test
    fun testGenerateSummaryTable() {
        val test = TestEntity(
            id = "test_1",
            title = "JEE Main Mock Test",
            durationMin = 180,
            marksCorrect = 4,
            marksNegative = 1,
            totalMarks = 300,
            questionCount = 4
        )

        val questions = listOf(
            QuestionJson(
                id = 1,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Physics Q1",
                options = listOf(OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")),
                answer = "A"
            ),
            QuestionJson(
                id = 2,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Physics Q2",
                options = listOf(OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")),
                answer = "B"
            ),
            QuestionJson(
                id = 3,
                subject = "Chemistry",
                section = "A",
                type = "MCQ",
                text = "Chemistry Q3",
                options = listOf(OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")),
                answer = "C"
            ),
            QuestionJson(
                id = 4,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "Maths Q4",
                answer = "10.0"
            )
        )

        val answersMap = mapOf(
            // Physics Q1: Correct (+4)
            1 to QuestionAttemptState(
                questionId = 1,
                selectedOption = "A",
                status = NtaStatus.ANSWERED,
                scoreAwarded = 4,
                isCorrect = true,
                timeSpentSeconds = 60
            ),
            // Physics Q2: Incorrect (-1)
            2 to QuestionAttemptState(
                questionId = 2,
                selectedOption = "C",
                status = NtaStatus.ANSWERED,
                scoreAwarded = -1,
                isCorrect = false,
                timeSpentSeconds = 40
            ),
            // Chemistry Q3: Unattempted (0)
            3 to QuestionAttemptState(
                questionId = 3,
                status = NtaStatus.NOT_VISITED,
                scoreAwarded = 0,
                isCorrect = false,
                timeSpentSeconds = 0
            ),
            // Maths Q4: Correct (+4)
            4 to QuestionAttemptState(
                questionId = 4,
                numericAnswer = "10.0",
                status = NtaStatus.ANSWERED,
                scoreAwarded = 4,
                isCorrect = true,
                timeSpentSeconds = 80
            )
        )

        val attempt = AttemptEntity(
            attemptId = "attempt_1",
            testId = "test_1",
            testTitle = "JEE Main Mock Test",
            startTime = 1000L,
            endTime = 2000L,
            durationSeconds = 180L,
            totalScore = 7,
            maxScore = 16,
            correctCount = 2,
            wrongCount = 1,
            skippedCount = 1,
            accuracyPercent = 66.67f,
            isCompleted = true
        )

        // Create a dummy repository-less call using direct generateSummaryTable
        val dummyAttemptRepo = com.quest.cbt.data.repository.AttemptRepository(
            object : com.quest.cbt.data.local.AttemptDao {
                override fun getAllCompletedAttempts() = kotlinx.coroutines.flow.emptyFlow<List<AttemptEntity>>()
                override fun getAllAttempts() = kotlinx.coroutines.flow.emptyFlow<List<AttemptEntity>>()
                override fun getAttemptsForTest(testId: String) = kotlinx.coroutines.flow.emptyFlow<List<AttemptEntity>>()
                override suspend fun getAttemptById(attemptId: String) = null
                override fun getAttemptByIdFlow(attemptId: String) = kotlinx.coroutines.flow.emptyFlow<AttemptEntity?>()
                override suspend fun getInProgressAttempt(testId: String) = null
                override suspend fun insertAttempt(attempt: AttemptEntity) {}
                override suspend fun deleteAttempt(attemptId: String) {}
                override suspend fun deleteAttemptsForTest(testId: String) {}
            }
        )

        val dummyTestRepo = com.quest.cbt.data.repository.TestRepository(
            object : com.quest.cbt.data.local.TestDao {
                override fun getAllTests() = kotlinx.coroutines.flow.emptyFlow<List<TestEntity>>()
                override fun getTestById(id: String) = kotlinx.coroutines.flow.emptyFlow<TestEntity?>()
                override suspend fun getTestByIdSuspend(id: String) = null
                override suspend fun insertTest(test: TestEntity) {}
                override suspend fun deleteTest(id: String) {}
                override suspend fun deleteAll() {}
            }
        )

        val viewModel = ResultSummaryViewModel("attempt_1", dummyAttemptRepo, dummyTestRepo)
        val table = viewModel.generateSummaryTable(attempt, test, questions, answersMap)

        // Verify overall counts
        assertEquals(4, table.totalQuestions)
        assertEquals(2, table.totalCorrect)
        assertEquals(1, table.totalIncorrect)
        assertEquals(1, table.totalUnattempted)

        // Total Accuracy = 2 / (2 + 1) * 100% = 66.6667%
        assertEquals(66.66667f, table.totalAccuracyPercent, 0.01f)

        // Physics row checks
        val physicsRow = table.rows.first { it.subject == "Physics" }
        assertEquals(2, physicsRow.totalQuestions)
        assertEquals(1, physicsRow.correctCount)
        assertEquals(1, physicsRow.incorrectCount)
        assertEquals(0, physicsRow.unattemptedCount)
        assertEquals(3, physicsRow.scoreAwarded) // +4 -1 = 3
        assertEquals(50f, physicsRow.accuracyPercent, 0.01f)

        // Chemistry row checks
        val chemRow = table.rows.first { it.subject == "Chemistry" }
        assertEquals(1, chemRow.totalQuestions)
        assertEquals(0, chemRow.correctCount)
        assertEquals(0, chemRow.incorrectCount)
        assertEquals(1, chemRow.unattemptedCount)
        assertEquals(0f, chemRow.accuracyPercent, 0.01f)

        // Maths row checks
        val mathsRow = table.rows.first { it.subject == "Maths" }
        assertEquals(1, mathsRow.totalQuestions)
        assertEquals(1, mathsRow.correctCount)
        assertEquals(0, mathsRow.incorrectCount)
        assertEquals(0, mathsRow.unattemptedCount)
        assertEquals(100f, mathsRow.accuracyPercent, 0.01f)
    }
}
