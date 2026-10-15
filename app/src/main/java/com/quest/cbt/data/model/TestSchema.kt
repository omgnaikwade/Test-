package com.quest.cbt.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TestJson(
    val schemaVersion: Int = 1,
    val title: String,
    val durationMin: Int = 180,
    val syllabus: Map<String, List<String>> = emptyMap(),
    val questions: List<QuestionJson> = emptyList()
)

@Serializable
data class QuestionJson(
    val id: Int,
    val subject: String,
    val section: String,
    val type: String,
    val text: String,
    val images: List<String> = emptyList(),
    val options: List<OptionJson> = emptyList(),
    val answer: String,
    val tolerance: Double? = null,
    val solution: String? = null,
    val solutionImages: List<String> = emptyList()
)

@Serializable
data class OptionJson(
    val text: String = "",
    val image: String? = null
)
