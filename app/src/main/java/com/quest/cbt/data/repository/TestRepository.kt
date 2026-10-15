package com.quest.cbt.data.repository

import com.quest.cbt.data.local.TestDao
import com.quest.cbt.data.model.TestEntity
import kotlinx.coroutines.flow.Flow

class TestRepository(private val testDao: TestDao) {

    val allTests: Flow<List<TestEntity>> = testDao.getAllTests()

    fun getTestById(id: String): Flow<TestEntity?> = testDao.getTestById(id)

    suspend fun getTestByIdSuspend(id: String): TestEntity? = testDao.getTestByIdSuspend(id)

    suspend fun saveTest(test: TestEntity) {
        testDao.insertTest(test)
    }

    suspend fun deleteTest(id: String) {
        testDao.deleteTest(id)
    }

    suspend fun deleteAll() {
        testDao.deleteAll()
    }
}
