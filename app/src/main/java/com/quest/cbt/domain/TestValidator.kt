package com.quest.cbt.domain

import com.quest.cbt.data.model.TestJson

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String, val detailedErrors: List<String>) : ValidationResult()
}

object TestValidator {
    private val VALID_SUBJECTS = setOf("Physics", "Chemistry", "Maths")
    private val VALID_SECTIONS = setOf("A", "B")
    private val VALID_TYPES = setOf("MCQ", "NUM")
    private val VALID_MCQ_ANSWERS = setOf("A", "B", "C", "D", "BONUS")

    fun validate(test: TestJson): ValidationResult {
        val errors = mutableListOf<String>()

        if (test.title.isBlank()) {
            errors.add("Test title cannot be empty.")
        }
        if (test.durationMin <= 0) {
            errors.add("Test duration must be greater than 0 minutes (found: ${test.durationMin}).")
        }
        if (test.questions.isEmpty()) {
            errors.add("The test contains 0 questions. Expected question list.")
        }

        test.questions.forEachIndexed { index, q ->
            val qNum = if (q.id > 0) q.id else index + 1
            if (!VALID_SUBJECTS.contains(q.subject)) {
                errors.add("Question $qNum: Invalid subject '${q.subject}'. Allowed subjects: Physics, Chemistry, Maths.")
            }
            if (!VALID_SECTIONS.contains(q.section)) {
                errors.add("Question $qNum: Invalid section '${q.section}'. Allowed sections: 'A' or 'B'.")
            }
            if (!VALID_TYPES.contains(q.type)) {
                errors.add("Question $qNum: Invalid type '${q.type}'. Allowed types: 'MCQ' or 'NUM'.")
            }
            if (q.text.isBlank() && q.images.isEmpty()) {
                errors.add("Question $qNum: Question text or image must be provided.")
            }

            if (q.type == "MCQ") {
                if (q.options.size != 4) {
                    errors.add("Question $qNum: MCQ must have exactly 4 options (found ${q.options.size}).")
                }
                val ans = q.answer.trim().uppercase()
                if (!VALID_MCQ_ANSWERS.contains(ans)) {
                    errors.add("Question $qNum: answer '${q.answer}' is not a valid option. Expected A, B, C, D, or BONUS.")
                }
            } else if (q.type == "NUM") {
                val ans = q.answer.trim().uppercase()
                if (ans != "BONUS") {
                    val num = ans.toDoubleOrNull()
                    if (num == null) {
                        errors.add("Question $qNum: Numeric question answer '${q.answer}' is not a valid number or BONUS.")
                    }
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            val summary = if (errors.size == 1) errors.first() else "${errors.size} validation issues found: ${errors.first()}"
            ValidationResult.Error(summary, errors)
        }
    }
}
