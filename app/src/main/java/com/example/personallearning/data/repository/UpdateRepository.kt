package com.example.personallearning.data.repository

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String = "",
    val publishedAt: String = "",
    val fileSize: Long = 0L
)

sealed interface UpdateCheckResult {
    data class Success(val info: AppUpdateInfo) : UpdateCheckResult
    data class Failure(val message: String) : UpdateCheckResult
}

data class DownloadProgress(
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val reason: Int
) {
    val percent: Int
        get() = if (totalBytes > 0) {
            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
        } else {
            0
        }
}

class UpdateRepository(private val context: Context) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    suspend fun check(serverUrl: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${serverUrl.trimEnd('/')}/api/app/update")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val message = if (response.code == 404) {
                        "服务器尚未启用更新接口，请先部署新版服务端"
                    } else {
                        "服务器返回 HTTP ${response.code}"
                    }
                    return@withContext UpdateCheckResult.Failure(message)
                }
                val body = response.body?.string().orEmpty()
                val info = gson.fromJson(body, AppUpdateInfo::class.java)
                if (info.versionCode <= 0) {
                    UpdateCheckResult.Failure("更新信息不完整")
                } else {
                    UpdateCheckResult.Success(info)
                }
            }
        } catch (e: Exception) {
            UpdateCheckResult.Failure(e.message ?: "无法连接更新服务器")
        }
    }

    fun enqueue(info: AppUpdateInfo, wifiOnly: Boolean): Long {
        val fileName = "personal-learning-${info.versionName}.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("自我修行 ${info.versionName}")
            .setDescription("正在下载应用更新")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(!wifiOnly)
            .setAllowedOverRoaming(false)
        if (wifiOnly) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        }
        return downloadManager.enqueue(request)
    }

    fun query(downloadId: Long): DownloadProgress? {
        if (downloadId < 0) return null
        val query = DownloadManager.Query().setFilterById(downloadId)
        return downloadManager.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            DownloadProgress(
                status = cursor.int(DownloadManager.COLUMN_STATUS),
                downloadedBytes = cursor.long(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                totalBytes = cursor.long(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                reason = cursor.int(DownloadManager.COLUMN_REASON)
            )
        }
    }

    fun downloadedFileUri(downloadId: Long): Uri? =
        downloadManager.getUriForDownloadedFile(downloadId)

    private fun Cursor.int(column: String): Int = getInt(getColumnIndexOrThrow(column))
    private fun Cursor.long(column: String): Long = getLong(getColumnIndexOrThrow(column))

    companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
