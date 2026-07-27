package com.example.personallearning.ui.screen.daohen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.personallearning.data.remote.AnalyzeDaoHenResponse
import com.example.personallearning.data.remote.BetterChoiceDto
import com.example.personallearning.data.remote.EmotionDto
import com.example.personallearning.data.remote.StoneDto
import com.example.personallearning.ui.viewmodel.AnalysisUiState
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private val bg = Color(0xFF191B22)
private val surface = Color(0xFF252832)
private val fg = Color(0xFFF2F0EA)
private val muted = Color(0xFFA5A7AE)
private val accent = Color(0xFF7FC8A9)
private val danger = Color(0xFFE78B8B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaoHenScreen(viewModel: DaoHenViewModel, onBack: () -> Unit, onHistoryClick: () -> Unit) {
    val context = LocalContext.current
    val entry by viewModel.currentEntry.collectAsState()
    val analysis by viewModel.analysisState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var transcript by remember { mutableStateOf("") }
    var facts by remember { mutableStateOf("") }
    var emotions by remember { mutableStateOf("") }
    var stone by remember { mutableStateOf("") }
    var betterChoice by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var partial by remember { mutableStateOf("") }
    val transcriptFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    val recognizer = remember(context) { SpeechRecognizer.createSpeechRecognizer(context) }
    DisposableEffect(recognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) { listening = true }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false }
            override fun onError(error: Int) { listening = false }
            override fun onResults(results: android.os.Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (result.isNotBlank()) transcript = (transcript + " " + result).trim()
                partial = ""
                listening = false
            }
            override fun onPartialResults(results: android.os.Bundle?) {
                partial = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }
            override fun onEvent(eventType: Int, params: android.os.Bundle?) = Unit
        }
        recognizer.setRecognitionListener(listener)
        onDispose { recognizer.destroy() }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecognition(recognizer)
    }
    LaunchedEffect(analysis) {
        val response = (analysis as? AnalysisUiState.Success)?.response ?: return@LaunchedEffect
        facts = formatFacts(response.facts)
        emotions = formatEmotions(response.emotions)
        stone = formatStone(response.stone)
        betterChoice = formatChoice(response.betterChoice)
        question = response.questionForUser
    }
    LaunchedEffect(entry?.date) {
        entry?.let {
            transcript = it.transcript
            facts = it.facts
            emotions = it.emotions
            stone = it.stone
            betterChoice = it.betterChoice
            question = it.aiQuestion
        }
    }
    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is com.example.personallearning.ui.viewmodel.SyncUiState.Success -> snackbar.showSnackbar(state.message)
            is com.example.personallearning.ui.viewmodel.SyncUiState.Failure -> snackbar.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("修行", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            actions = {
                IconButton(onClick = onHistoryClick) { Icon(Icons.Default.History, "历史记录") }
                IconButton(onClick = viewModel::sync, enabled = syncState !is com.example.personallearning.ui.viewmodel.SyncUiState.Syncing) { Icon(Icons.Default.Sync, "同步") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = fg, navigationIconContentColor = fg, actionIconContentColor = fg)
        ) },
        snackbarHost = { SnackbarHost(snackbar) }, containerColor = bg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("说说今天发生了什么", color = fg, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text("先把事情讲出来，再一起看见其中的模式。", color = muted, fontSize = 13.sp)
            Button(
                onClick = {
                    if (listening) recognizer.stopListening()
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecognition(recognizer)
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp).size(104.dp), shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = if (listening) danger else accent, contentColor = bg)
            ) { Icon(if (listening) Icons.Default.Stop else Icons.Default.Mic, "录音") }
            if (partial.isNotBlank()) Text(partial, color = muted, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(transcript, { transcript = it }, Modifier.fillMaxWidth().height(150.dp).focusRequester(transcriptFocusRequester), label = { Text("讲述文字") }, placeholder = { Text("也可以直接用键盘输入") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { transcript = ""; partial = ""; viewModel.clearAnalysis() }, modifier = Modifier.weight(1f)) { Text("重新讲述") }
                OutlinedButton(onClick = { transcriptFocusRequester.requestFocus() }, modifier = Modifier.weight(1f)) { Text("手动记录") }
            }
            Button(onClick = { viewModel.analyze(transcript) }, enabled = transcript.isNotBlank() && analysis !is AnalysisUiState.Loading, modifier = Modifier.fillMaxWidth()) {
                if (analysis is AnalysisUiState.Loading) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = fg)
                Text(if (analysis is AnalysisUiState.Loading) "正在分析" else "帮我看见")
            }
            if (analysis is AnalysisUiState.Failure) Text((analysis as AnalysisUiState.Failure).message, color = danger)
            if (analysis is AnalysisUiState.Success || analysis is AnalysisUiState.Failure) {
                Editor("发生了什么", facts) { facts = it }
                Editor("我的情绪", emotions) { emotions = it }
                Editor("我的石头", stone) { stone = it }
                Editor("下次更好的选择", betterChoice) { betterChoice = it }
                if (question.isNotBlank()) Text("AI 追问：$question", color = muted, fontSize = 13.sp)
                Button(onClick = { viewModel.saveAnalyzedEntry(transcript, facts, emotions, stone, betterChoice, question) }, enabled = transcript.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("确认保存") }
            }
            if (analysis is AnalysisUiState.Saved) Text("已保存", color = accent)
            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun startRecognition(recognizer: SpeechRecognizer) {
    recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
    })
}

@Composable
private fun Editor(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().height(120.dp), label = { Text(label) })
}

private fun formatFacts(facts: List<String>) = facts.joinToString("\n") { "- $it" }
private fun formatEmotions(items: List<EmotionDto>) = items.joinToString("\n") { "${it.name}（${it.intensity}/10）：${it.evidence}" }
private fun formatStone(item: StoneDto) = "模式：${item.pattern}\n信心：${item.confidence}\n另一种可能：${item.alternative}"
private fun formatChoice(item: BetterChoiceDto) = "触发：${item.trigger}\n行动：${item.action}\n最小一步：${item.smallestStep}"
