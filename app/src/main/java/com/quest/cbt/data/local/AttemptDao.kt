package com.quest.cbt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quest.cbt.data.model.AttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Query("SELECT * FROM attempts WHERE isCompleted = 1 ORDER BY endTime DESC")
    fun getAllCompletedAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts ORDER BY startTime DESC")
    fun getAllAttempts(): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE testId = :testId AND isCompleted = 1 ORDER BY endTime DESC")
    fun getAttemptsForTest(testId: String): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE attemptId = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: String): AttemptEntity?

    @Query("SELECT * FROM attempts WHERE attemptId = :attemptId LIMIT 1")
    fun getAttemptByIdFlow(attemptId: String): Flow<AttemptEntity?>

    @Query("SELECT * FROM attempts WHERE testId = :testId AND isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getInProgressAttempt(testId: String): AttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Query("DELETE FROM attempts WHERE attemptId = :attemptId")
    suspend fun deleteAttempt(attemptId: String)

    @Query("DELETE FROM attempts WHERE testId = :testId")
    suspend fun deleteAttemptsForTest(testId: String)
}
