package com.quest.cbt.data.repository

import com.quest.cbt.data.local.AttemptDao
import com.quest.cbt.data.model.AttemptEntity
import kotlinx.coroutines.flow.Flow

class AttemptRepository(private val attemptDao: AttemptDao) {

    val allCompletedAttempts: Flow<List<AttemptEntity>> = attemptDao.getAllCompletedAttempts()

    fun getAttemptsForTest(testId: String): Flow<List<AttemptEntity>> = attemptDao.getAttemptsForTest(testId)

    suspend fun getAttemptById(attemptId: String): AttemptEntity? = attemptDao.getAttemptById(attemptId)

    fun getAttemptByIdFlow(attemptId: String): Flow<AttemptEntity?> = attemptDao.getAttemptByIdFlow(attemptId)

    suspend fun getInProgressAttempt(testId: String): AttemptEntity? = attemptDao.getInProgressAttempt(testId)

    suspend fun saveAttempt(attempt: AttemptEntity) {
        attemptDao.insertAttempt(attempt)
    }

    suspend fun deleteAttempt(attemptId: String) {
        attemptDao.deleteAttempt(attemptId)
    }

    suspend fun deleteAttemptsForTest(testId: String) {
        attemptDao.deleteAttemptsForTest(testId)
    }

    suspend fun deleteAll() {
        attemptDao.deleteAll()
    }
}
