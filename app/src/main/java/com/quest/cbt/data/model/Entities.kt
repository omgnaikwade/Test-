package com.quest.cbt.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tests")
@Serializable
data class TestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val durationMin: Int,
    val marksCorrect: Int = 4,
    val marksNegative: Int = 1,
    val totalMarks: Int = 300,
    val questionCount: Int = 75,
    val syllabusJson: String = "{}",
    val questionsJson: String = "[]",
    val baseDir: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attempts")
@Serializable
data class AttemptEntity(
    @PrimaryKey val attemptId: String,
    val testId: String,
    val testTitle: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val durationSeconds: Long = 0L,
    val totalScore: Int = 0,
    val maxScore: Int = 300,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val accuracyPercent: Float = 0f,
    val physicsScore: Int = 0,
    val physicsMax: Int = 100,
    val chemistryScore: Int = 0,
    val chemistryMax: Int = 100,
    val mathsScore: Int = 0,
    val mathsMax: Int = 100,
    val answersStateJson: String = "{}",
    val isCompleted: Boolean = false
)
