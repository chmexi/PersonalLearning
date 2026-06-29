package com.example.personallearning.data.repository

import android.content.Context
import com.example.personallearning.data.local.AppDatabase
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.remote.DaoHenDto
import com.example.personallearning.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DaoHenRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.daoHenDao()

    fun getAllEntries(): Flow<List<DaoHenEntry>> = dao.getAllEntries()

    suspend fun getEntryByDate(date: String): DaoHenEntry? = dao.getEntryByDate(date)

    suspend fun getYesterdayStone(today: String): String? = dao.getYesterdayStone(today)

    suspend fun saveEntry(entry: DaoHenEntry) {
        val existing = dao.getEntryByDate(entry.date)
        if (existing != null) {
            dao.update(entry.copy(id = existing.id, syncStatus = 0))
        } else {
            dao.insert(entry)
        }
    }

    suspend fun syncToServer(serverUrl: String) {
        if (serverUrl.isBlank()) return
        try {
            val api = RetrofitClient.getApiService(serverUrl)
            val unsynced = dao.getUnsyncedEntries()
            for (entry in unsynced) {
                val resp = api.syncEntry(DaoHenDto(
                    date = entry.date, q1 = entry.q1, q2 = entry.q2,
                    q3 = entry.q3, q4 = entry.q4, q5 = entry.q5,
                    q6 = entry.q6, q7 = entry.q7
                ))
                if (resp.isSuccessful) {
                    dao.markSynced(entry.id)
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun pullFromServer(serverUrl: String) {
        if (serverUrl.isBlank()) return
        try {
            val api = RetrofitClient.getApiService(serverUrl)
            val today = LocalDate.now().toString()
            val resp = api.getEntry(today)
            if (resp.isSuccessful && resp.body() != null) {
                val dto = resp.body()!!
                val existing = dao.getEntryByDate(dto.date)
                if (existing == null) {
                    dao.insert(DaoHenEntry(
                        date = dto.date, q1 = dto.q1, q2 = dto.q2,
                        q3 = dto.q3, q4 = dto.q4, q5 = dto.q5,
                        q6 = dto.q6, q7 = dto.q7, syncStatus = 1
                    ))
                }
            }
        } catch (_: Exception) {}
    }
}
