package com.example.personallearning.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val serverUrl: String = DEFAULT_SERVER_URL,
    val checkUpdatesOnLaunch: Boolean = true,
    val autoDownloadUpdates: Boolean = true,
    val wifiOnlyDownloads: Boolean = true,
    val lastDownloadId: Long = -1L,
    val lastDownloadVersionCode: Int = -1
) {
    companion object {
        const val DEFAULT_SERVER_URL = "http://49.232.149.194:5001"
    }
}

class SettingsRepository(private val context: Context) {
    private object Keys {
        val serverUrl = stringPreferencesKey("server_url")
        val checkUpdatesOnLaunch = booleanPreferencesKey("check_updates_on_launch")
        val autoDownloadUpdates = booleanPreferencesKey("auto_download_updates")
        val wifiOnlyDownloads = booleanPreferencesKey("wifi_only_downloads")
        val lastDownloadId = longPreferencesKey("last_download_id")
        val lastDownloadVersionCode = androidx.datastore.preferences.core.intPreferencesKey(
            "last_download_version_code"
        )
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            serverUrl = preferences[Keys.serverUrl] ?: AppSettings.DEFAULT_SERVER_URL,
            checkUpdatesOnLaunch = preferences[Keys.checkUpdatesOnLaunch] ?: true,
            autoDownloadUpdates = preferences[Keys.autoDownloadUpdates] ?: true,
            wifiOnlyDownloads = preferences[Keys.wifiOnlyDownloads] ?: true,
            lastDownloadId = preferences[Keys.lastDownloadId] ?: -1L,
            lastDownloadVersionCode = preferences[Keys.lastDownloadVersionCode] ?: -1
        )
    }

    suspend fun setServerUrl(value: String) {
        context.settingsDataStore.edit { it[Keys.serverUrl] = normalizeServerUrl(value) }
    }

    suspend fun setCheckUpdatesOnLaunch(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.checkUpdatesOnLaunch] = value }
    }

    suspend fun setAutoDownloadUpdates(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.autoDownloadUpdates] = value }
    }

    suspend fun setWifiOnlyDownloads(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.wifiOnlyDownloads] = value }
    }

    suspend fun setLastDownload(value: Long, versionCode: Int) {
        context.settingsDataStore.edit {
            it[Keys.lastDownloadId] = value
            it[Keys.lastDownloadVersionCode] = versionCode
        }
    }

    private fun normalizeServerUrl(value: String): String {
        val trimmed = value.trim().trimEnd('/')
        if (trimmed.isBlank()) return AppSettings.DEFAULT_SERVER_URL
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }
}
