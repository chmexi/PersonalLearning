package com.example.personallearning.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.personallearning.data.repository.AppSettings
import com.example.personallearning.ui.viewmodel.SettingsViewModel
import com.example.personallearning.ui.viewmodel.UpdateUiState

private val bg = Color(0xFF191B22)
private val surface = Color(0xFF252832)
private val surfaceAlt = Color(0xFF30343F)
private val fg = Color(0xFFF2F0EA)
private val muted = Color(0xFFA5A7AE)
private val accent = Color(0xFF7FC8A9)
private val warm = Color(0xFFE8B86D)
private val danger = Color(0xFFE78B8B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = fg,
                    navigationIconContentColor = fg
                )
            )
        },
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("应用更新", "当前版本 ${viewModel.currentVersionName}")
            UpdateCard(
                state = updateState,
                onCheck = { viewModel.checkForUpdates() },
                onDownload = viewModel::downloadAvailableUpdate,
                onInstall = viewModel::installDownloadedUpdate
            )

            SectionTitle("更新偏好", "控制何时检查和下载新版本")
            SettingsGroup {
                SettingSwitch(
                    title = "启动时检查更新",
                    description = "打开应用后自动查询一次",
                    checked = settings.checkUpdatesOnLaunch,
                    onCheckedChange = viewModel::setCheckUpdatesOnLaunch
                )
                SettingSwitch(
                    title = "发现更新后自动下载",
                    description = "下载完成后仍需你确认安装",
                    checked = settings.autoDownloadUpdates,
                    onCheckedChange = viewModel::setAutoDownloadUpdates
                )
                SettingSwitch(
                    title = "仅使用 Wi-Fi 下载",
                    description = "避免消耗移动数据",
                    checked = settings.wifiOnlyDownloads,
                    onCheckedChange = viewModel::setWifiOnlyDownloads
                )
            }

            SectionTitle("连接配置", "道痕同步和更新检查共用此地址")
            ServerConfig(
                settings = settings,
                onSave = viewModel::setServerUrl
            )

            SectionTitle("AI 道痕", "由配置的服务器调用 DeepSeek，应用不保存 DeepSeek API Key")
            AiConfig(settings, viewModel)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AiConfig(settings: AppSettings, viewModel: SettingsViewModel) {
    var token by remember(settings.aiAccessToken) { mutableStateOf(settings.aiAccessToken) }
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("服务器访问令牌", color = fg, fontWeight = FontWeight.Medium)
            Text(
                "DeepSeek 状态：由服务器端密钥决定；未配置时普通记录和同步仍可用。",
                color = muted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = token, onValueChange = { token = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = PasswordVisualTransformation(), placeholder = { Text("Bearer 令牌") }
            )
            TextButton(onClick = { viewModel.setAiAccessToken(token) }, modifier = Modifier.align(Alignment.End)) { Text("保存令牌") }
            SettingSwitch("启用 AI", "分析失败时仍可保留并手动编辑讲述文字", settings.aiEnabled, viewModel::setAiEnabled)
            SettingSwitch("保留讲述文字", "将转写内容随记录保存并参与同步", settings.retainTranscript, viewModel::setRetainTranscript)
            Text("隐私说明：讲述文字会发送到配置的服务器和 DeepSeek。", color = muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, description: String) {
    Column {
        Text(title, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(description, color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun UpdateCard(
    state: UpdateUiState,
    onCheck: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            when (state) {
                UpdateUiState.Idle -> UpdateMessage(
                    icon = { Icon(Icons.Default.Refresh, null, tint = accent) },
                    title = "检查是否有新版本",
                    description = "从配置的服务器获取最新版本信息"
                )
                UpdateUiState.Checking -> UpdateMessage(
                    icon = { CircularProgressIndicator(Modifier.height(22.dp), strokeWidth = 2.dp, color = accent) },
                    title = "正在检查",
                    description = "请稍候…"
                )
                is UpdateUiState.UpToDate -> UpdateMessage(
                    icon = { Icon(Icons.Default.CheckCircle, null, tint = accent) },
                    title = "已是最新版本",
                    description = "版本 ${state.versionName}"
                )
                is UpdateUiState.Available -> {
                    UpdateMessage(
                        icon = { Icon(Icons.Default.CloudDownload, null, tint = warm) },
                        title = "发现新版本 ${state.info.versionName}",
                        description = state.info.releaseNotes.ifBlank { "新版本已可以下载" }
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = bg)
                    ) {
                        Icon(Icons.Default.CloudDownload, null)
                        Text("开始下载", Modifier.padding(start = 8.dp))
                    }
                }
                is UpdateUiState.Downloading -> {
                    UpdateMessage(
                        icon = { Icon(Icons.Default.CloudDownload, null, tint = accent) },
                        title = "正在下载 ${state.info.versionName}",
                        description = if (state.percent > 0) "${state.percent}%" else "等待网络连接"
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = accent,
                        trackColor = surfaceAlt
                    )
                }
                is UpdateUiState.Downloaded -> {
                    UpdateMessage(
                        icon = { Icon(Icons.Default.InstallMobile, null, tint = accent) },
                        title = "更新已下载",
                        description = "版本 ${state.info.versionName}，点击后由系统确认安装"
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = bg)
                    ) {
                        Icon(Icons.Default.InstallMobile, null)
                        Text("安装更新", Modifier.padding(start = 8.dp))
                    }
                }
                is UpdateUiState.Failure -> UpdateMessage(
                    icon = { Icon(Icons.Default.ErrorOutline, null, tint = danger) },
                    title = "检查或下载失败",
                    description = state.message
                )
            }

            if (state !is UpdateUiState.Checking &&
                state !is UpdateUiState.Downloading &&
                state !is UpdateUiState.Available &&
                state !is UpdateUiState.Downloaded
            ) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCheck,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Text(
                        if (state is UpdateUiState.Idle) "检查更新" else "重新检查",
                        Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateMessage(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(title, color = fg, fontWeight = FontWeight.Medium)
            Text(description, color = muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), content = { content() })
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = fg, fontSize = 14.sp)
            Text(description, color = muted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = bg,
                checkedTrackColor = accent,
                uncheckedThumbColor = muted,
                uncheckedTrackColor = surfaceAlt
            )
        )
    }
}

@Composable
private fun ServerConfig(
    settings: AppSettings,
    onSave: (String) -> Unit
) {
    var serverUrl by remember { mutableStateOf(settings.serverUrl) }
    LaunchedEffect(settings.serverUrl) {
        if (serverUrl != settings.serverUrl) serverUrl = settings.serverUrl
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = warm)
                Text("服务器地址", Modifier.padding(start = 10.dp), color = fg, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://example.com") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fg,
                    unfocusedTextColor = fg,
                    cursorColor = accent,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = surfaceAlt,
                    focusedContainerColor = bg.copy(alpha = 0.35f),
                    unfocusedContainerColor = bg.copy(alpha = 0.35f)
                )
            )
            Text(
                "修改后，下一次同步和更新检查会使用新地址。",
                Modifier.padding(top = 8.dp),
                color = muted,
                fontSize = 11.sp
            )
            TextButton(
                onClick = { onSave(serverUrl) },
                enabled = serverUrl.trim().isNotBlank() && serverUrl.trim().trimEnd('/') != settings.serverUrl,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("保存配置")
            }
        }
    }
}
