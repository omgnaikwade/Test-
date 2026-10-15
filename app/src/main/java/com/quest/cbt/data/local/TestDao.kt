package com.quest.cbt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quest.cbt.data.model.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    fun getTestById(id: String): Flow<TestEntity?>

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    suspend fun getTestByIdSuspend(id: String): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Query("DELETE FROM tests WHERE id = :id")
    suspend fun deleteTest(id: String)

    @Query("DELETE FROM tests")
    suspend fun deleteAll()
}
