package com.quest.cbt

import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.OptionJson
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.domain.ScoringCalculator
import com.quest.cbt.domain.TestValidator
import com.quest.cbt.domain.ValidationResult
import com.quest.cbt.util.SampleTestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringCalculatorTest {

    @Test
    fun testScoringRules() {
        val questions = listOf(
            QuestionJson(
                id = 1,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Question 1",
                options = listOf(
                    OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")
                ),
                answer = "A"
            ),
            QuestionJson(
                id = 2,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Question 2",
                options = listOf(
                    OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")
                ),
                answer = "B"
            ),
            QuestionJson(
                id = 3,
                subject = "Chemistry",
                section = "A",
                type = "MCQ",
                text = "Question 3 (BONUS)",
                options = listOf(
                    OptionJson("A"), OptionJson("B"), OptionJson("C"), OptionJson("D")
                ),
                answer = "BONUS"
            ),
            QuestionJson(
                id = 4,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "Question 4 (NUM)",
                answer = "25.0",
                tolerance = 0.05
            )
        )

        val answers = mapOf(
            // Correct MCQ -> +4
            1 to QuestionAttemptState(questionId = 1, selectedOption = "A", status = NtaStatus.ANSWERED),
            // Wrong MCQ -> -1
            2 to QuestionAttemptState(questionId = 2, selectedOption = "C", status = NtaStatus.ANSWERED),
            // Unattempted BONUS -> should still get +4
            3 to QuestionAttemptState(questionId = 3, status = NtaStatus.NOT_VISITED),
            // Correct NUM with tolerance -> +4
            4 to QuestionAttemptState(questionId = 4, numericAnswer = "25.02", status = NtaStatus.ANSWERED)
        )

        val result = ScoringCalculator.calculateScore(
            questions = questions,
            answers = answers,
            marksCorrect = 4,
            marksNegative = 1
        )

        // Expected score: +4 -1 +4 +4 = 11
        assertEquals(11, result.totalScore)
        assertEquals(3, result.correctCount)
        assertEquals(1, result.wrongCount)
    }

    @Test
    fun testSampleDataValidation() {
        val sample = SampleTestData.getSampleTest()
        assertEquals(75, sample.questions.size)
        val validation = TestValidator.validate(sample)
        assertTrue(validation is ValidationResult.Success)
    }
}
