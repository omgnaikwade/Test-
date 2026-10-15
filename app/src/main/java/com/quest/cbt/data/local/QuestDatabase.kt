package com.quest.cbt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.TestEntity

@Database(
    entities = [TestEntity::class, AttemptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
    abstract fun attemptDao(): AttemptDao

    companion object {
        @Volatile
        private var INSTANCE: QuestDatabase? = null

        fun getInstance(context: Context): QuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestDatabase::class.java,
                    "quest_cbt.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
