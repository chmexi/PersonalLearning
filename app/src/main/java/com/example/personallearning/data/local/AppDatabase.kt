package com.example.personallearning.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.model.ExpressEntry

@Database(entities = [DaoHenEntry::class, ExpressEntry::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daoHenDao(): DaoHenDao
    abstract fun expressDao(): ExpressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_learning.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
