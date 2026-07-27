package com.example.personallearning.ui.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallearning.data.repository.AppSettings
import com.example.personallearning.data.repository.AppUpdateInfo
import com.example.personallearning.data.repository.SettingsRepository
import com.example.personallearning.data.repository.UpdateCheckResult
import com.example.personallearning.data.repository.UpdateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class UpToDate(val versionName: String) : UpdateUiState
    data class Available(val info: AppUpdateInfo) : UpdateUiState
    data class Downloading(val info: AppUpdateInfo, val percent: Int) : UpdateUiState
    data class Downloaded(val info: AppUpdateInfo, val downloadId: Long) : UpdateUiState
    data class Failure(val message: String) : UpdateUiState
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val updateRepository = UpdateRepository(application)
    private var downloadMonitorJob: Job? = null

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings()
    )

    private val _updateState = kotlinx.coroutines.flow.MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState

    val currentVersionName: String = packageInfo().versionName ?: "未知"
    private val currentVersionCode: Long
        get() {
            val info = packageInfo()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        }

    init {
        viewModelScope.launch {
            val currentSettings = settingsRepository.settings.first()
            if (currentSettings.checkUpdatesOnLaunch) {
                checkForUpdates(manual = false)
            }
        }
    }

    fun setServerUrl(value: String) {
        viewModelScope.launch { settingsRepository.setServerUrl(value) }
    }

    fun setAiAccessToken(value: String) { viewModelScope.launch { settingsRepository.setAiAccessToken(value) } }
    fun setAiEnabled(value: Boolean) { viewModelScope.launch { settingsRepository.setAiEnabled(value) } }
    fun setRetainTranscript(value: Boolean) { viewModelScope.launch { settingsRepository.setRetainTranscript(value) } }

    fun setCheckUpdatesOnLaunch(value: Boolean) {
        viewModelScope.launch { settingsRepository.setCheckUpdatesOnLaunch(value) }
    }

    fun setAutoDownloadUpdates(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoDownloadUpdates(value) }
    }

    fun setWifiOnlyDownloads(value: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnlyDownloads(value) }
    }

    fun checkForUpdates(manual: Boolean = true) {
        if (_updateState.value is UpdateUiState.Checking) return
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Checking
            val currentSettings = settingsRepository.settings.first()
            when (val result = updateRepository.check(currentSettings.serverUrl)) {
                is UpdateCheckResult.Failure -> {
                    if (manual || _updateState.value is UpdateUiState.Checking) {
                        _updateState.value = UpdateUiState.Failure(result.message)
                    }
                }
                is UpdateCheckResult.Success -> {
                    if (result.info.versionCode.toLong() <= currentVersionCode) {
                        _updateState.value = UpdateUiState.UpToDate(currentVersionName)
                    } else if (result.info.apkUrl.isBlank()) {
                        _updateState.value = UpdateUiState.Failure("服务器已发布新版本信息，但尚未上传 APK")
                    } else if (currentSettings.autoDownloadUpdates) {
                        resumeOrStartDownload(result.info, currentSettings)
                    } else {
                        _updateState.value = UpdateUiState.Available(result.info)
                    }
                }
            }
        }
    }

    fun downloadAvailableUpdate() {
        val info = (_updateState.value as? UpdateUiState.Available)?.info ?: return
        viewModelScope.launch {
            val currentSettings = settingsRepository.settings.first()
            startDownload(info, currentSettings.wifiOnlyDownloads)
        }
    }

    fun installDownloadedUpdate() {
        val state = _updateState.value as? UpdateUiState.Downloaded ?: return
        val application = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !application.packageManager.canRequestPackageInstalls()
        ) {
            application.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${application.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }
        val apkUri = updateRepository.downloadedFileUri(state.downloadId) ?: run {
            _updateState.value = UpdateUiState.Failure("找不到已下载的安装包，请重新检查更新")
            return
        }
        application.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(apkUri, UpdateRepository.APK_MIME_TYPE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    private suspend fun startDownload(info: AppUpdateInfo, wifiOnly: Boolean) {
        try {
            val downloadId = updateRepository.enqueue(info, wifiOnly)
            settingsRepository.setLastDownload(downloadId, info.versionCode)
            _updateState.value = UpdateUiState.Downloading(info, 0)
            monitorDownload(downloadId, info)
        } catch (e: Exception) {
            _updateState.value = UpdateUiState.Failure(e.message ?: "无法开始下载")
        }
    }

    private suspend fun resumeOrStartDownload(info: AppUpdateInfo, currentSettings: AppSettings) {
        val canResume = currentSettings.lastDownloadId >= 0 &&
            currentSettings.lastDownloadVersionCode == info.versionCode
        val progress = if (canResume) {
            updateRepository.query(currentSettings.lastDownloadId)
        } else {
            null
        }
        when (progress?.status) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                _updateState.value = UpdateUiState.Downloaded(info, currentSettings.lastDownloadId)
            }
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_PAUSED,
            DownloadManager.STATUS_RUNNING -> {
                _updateState.value = UpdateUiState.Downloading(info, progress.percent)
                monitorDownload(currentSettings.lastDownloadId, info)
            }
            else -> startDownload(info, currentSettings.wifiOnlyDownloads)
        }
    }

    private fun monitorDownload(downloadId: Long, info: AppUpdateInfo) {
        downloadMonitorJob?.cancel()
        downloadMonitorJob = viewModelScope.launch {
            while (true) {
                when (val progress = updateRepository.query(downloadId)) {
                    null -> {
                        _updateState.value = UpdateUiState.Failure("下载任务不存在")
                        return@launch
                    }
                    else -> when (progress.status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            _updateState.value = UpdateUiState.Downloaded(info, downloadId)
                            return@launch
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _updateState.value = UpdateUiState.Failure("下载失败，错误码 ${progress.reason}")
                            return@launch
                        }
                        else -> _updateState.value = UpdateUiState.Downloading(info, progress.percent)
                    }
                }
                delay(700)
            }
        }
    }

    private fun packageInfo() =
        getApplication<Application>().packageManager.getPackageInfo(
            getApplication<Application>().packageName,
            0
        )
}
