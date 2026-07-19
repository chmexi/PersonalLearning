package com.example.personallearning.data.repository

import android.content.Context
import com.example.personallearning.data.local.AppDatabase
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.remote.DaoHenDto
import com.example.personallearning.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class DaoHenRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.daoHenDao()

    fun getAllEntries(): Flow<List<DaoHenEntry>> = dao.getAllEntries()

    suspend fun getEntryByDate(date: String): DaoHenEntry? = dao.getEntryByDate(date)

    suspend fun getYesterdayStone(today: String): String? = dao.getYesterdayStone(today)

    suspend fun getPendingAction(today: String): DaoHenEntry? = dao.getPendingAction(today)

    suspend fun saveEntry(entry: DaoHenEntry) {
        val existing = dao.getEntryByDate(entry.date)
        if (existing != null) {
            dao.update(entry.copy(id = existing.id, syncStatus = 0))
        } else {
            dao.insert(entry)
        }
    }

    suspend fun syncToServer(serverUrl: String): SyncResult {
        if (serverUrl.isBlank()) return SyncResult.Failure("服务器地址为空")

        return try {
            val api = RetrofitClient.getApiService(serverUrl)
            val unsynced = dao.getUnsyncedEntries()
            var pushedCount = 0

            for (entry in unsynced) {
                val resp = api.syncEntry(entry.toDto())
                if (resp.isSuccessful) {
                    val saved = resp.body()
                        ?: return SyncResult.Failure("服务器未返回 ${entry.date} 的同步结果")
                    if (saved.revision <= entry.serverRevision) {
                        return SyncResult.Failure("服务器版本过旧，已停止同步以保护本地记录")
                    }
                    dao.update(saved.toEntry(syncStatus = 1).copy(id = entry.id))
                    pushedCount++
                } else if (resp.code() == 409) {
                    val remoteResp = api.getEntry(entry.date)
                    val remote = remoteResp.body()
                        ?: return SyncResult.Failure("无法读取 ${entry.date} 的云端冲突记录")
                    return SyncResult.Conflict(listOf(SyncConflict(entry, remote.toEntry(1))))
                } else {
                    return SyncResult.Failure("上传 ${entry.date} 失败：HTTP ${resp.code()}")
                }
            }

            SyncResult.Success(pushedCount = pushedCount)
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "同步失败")
        }
    }

    suspend fun pullFromServer(serverUrl: String): SyncResult {
        if (serverUrl.isBlank()) return SyncResult.Failure("服务器地址为空")

        return try {
            val api = RetrofitClient.getApiService(serverUrl)
            val resp = api.getRange()
            if (!resp.isSuccessful) {
                return SyncResult.Failure("拉取失败：HTTP ${resp.code()}")
            }

            var pulledCount = 0
            val conflicts = mutableListOf<SyncConflict>()
            resp.body().orEmpty().forEach { dto ->
                if (dto.revision <= 0) {
                    return SyncResult.Failure("服务器版本过旧，已停止同步以保护本地记录")
                }
                val existing = dao.getEntryByDate(dto.date)
                if (existing == null) {
                    dao.insert(dto.toEntry(syncStatus = 1))
                    pulledCount++
                } else if (existing.syncStatus == 0 && existing.serverRevision != dto.revision) {
                    conflicts += SyncConflict(existing, dto.toEntry(syncStatus = 1))
                } else if (existing.syncStatus == 1) {
                    dao.update(dto.toEntry(syncStatus = 1).copy(id = existing.id))
                    pulledCount++
                }
            }

            if (conflicts.isEmpty()) {
                SyncResult.Success(pulledCount = pulledCount)
            } else {
                SyncResult.Conflict(conflicts)
            }
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "拉取失败")
        }
    }

    suspend fun syncAll(serverUrl: String): SyncResult {
        val pull = pullFromServer(serverUrl)
        if (pull !is SyncResult.Success) return pull

        val push = syncToServer(serverUrl)
        if (push !is SyncResult.Success) return push

        return SyncResult.Success(
            pushedCount = (push as SyncResult.Success).pushedCount,
            pulledCount = (pull as SyncResult.Success).pulledCount
        )
    }

    suspend fun resolveWithRemote(conflict: SyncConflict) {
        val existing = dao.getEntryByDate(conflict.local.date)
        dao.update(conflict.remote.copy(id = existing?.id ?: conflict.local.id, syncStatus = 1))
    }

    suspend fun resolveWithLocal(serverUrl: String, conflict: SyncConflict): SyncResult {
        return try {
            val api = RetrofitClient.getApiService(serverUrl)
            val candidate = conflict.local.copy(serverRevision = conflict.remote.serverRevision)
            val response = api.syncEntry(candidate.toDto())
            if (!response.isSuccessful) {
                return SyncResult.Failure("保留本机版本失败：HTTP ${response.code()}")
            }
            val saved = response.body()
                ?: return SyncResult.Failure("服务器未返回冲突处理结果")
            val existing = dao.getEntryByDate(candidate.date)
            dao.update(saved.toEntry(syncStatus = 1).copy(id = existing?.id ?: candidate.id))
            SyncResult.Success(pushedCount = 1)
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "冲突处理失败")
        }
    }

    private fun DaoHenEntry.toDto(): DaoHenDto {
        return DaoHenDto(
            date = date,
            q1 = q1,
            q2 = q2,
            q3 = q3,
            q4 = q4,
            q5 = q5,
            q6 = q6,
            q7 = q7,
            tags = tags,
            actionStatus = actionStatus,
            actionNote = actionNote,
            id = id,
            revision = serverRevision
        )
    }

    private fun DaoHenDto.toEntry(syncStatus: Int): DaoHenEntry {
        return DaoHenEntry(
            date = date,
            q1 = q1.orEmpty(),
            q2 = q2.orEmpty(),
            q3 = q3.orEmpty(),
            q4 = q4.orEmpty(),
            q5 = q5.orEmpty(),
            q6 = q6.orEmpty(),
            q7 = q7.orEmpty(),
            tags = tags.orEmpty(),
            actionStatus = actionStatus,
            actionNote = actionNote.orEmpty(),
            syncStatus = syncStatus,
            serverRevision = revision
        )
    }
}

sealed interface SyncResult {
    data class Success(
        val pushedCount: Int = 0,
        val pulledCount: Int = 0
    ) : SyncResult

    data class Failure(val message: String) : SyncResult

    data class Conflict(val conflicts: List<SyncConflict>) : SyncResult
}

data class SyncConflict(
    val local: DaoHenEntry,
    val remote: DaoHenEntry
)
