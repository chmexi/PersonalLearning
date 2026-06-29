package com.example.personallearning.data.local

import androidx.room.*
import com.example.personallearning.data.model.DaoHenEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoHenDao {
    @Query("SELECT * FROM daohen_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DaoHenEntry>>

    @Query("SELECT * FROM daohen_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): DaoHenEntry?

    @Query("SELECT q6 FROM daohen_entries WHERE date < :today ORDER BY date DESC LIMIT 1")
    suspend fun getYesterdayStone(today: String): String?

    @Query("SELECT * FROM daohen_entries WHERE syncStatus = 0")
    suspend fun getUnsyncedEntries(): List<DaoHenEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DaoHenEntry): Long

    @Update
    suspend fun update(entry: DaoHenEntry)

    @Query("UPDATE daohen_entries SET syncStatus = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
