package com.example.personallearning.data.local

import androidx.room.*
import com.example.personallearning.data.model.ExpressEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpressDao {
    @Query("SELECT * FROM express_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<ExpressEntry>>

    @Query("SELECT * FROM express_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): ExpressEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ExpressEntry): Long

    @Update
    suspend fun update(entry: ExpressEntry)

    @Query("SELECT * FROM express_entries ORDER BY date DESC LIMIT 7")
    suspend fun getRecent7(): List<ExpressEntry>
}
