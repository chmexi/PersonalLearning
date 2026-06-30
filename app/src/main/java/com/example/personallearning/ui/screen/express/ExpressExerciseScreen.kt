package com.example.personallearning.ui.screen.express

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.ExpressViewModel
import kotlinx.coroutines.delay

private val bg = Color(0xFF1A1B2F)
private val cardBg = Color(0xFF2A2B40)
private val fg = Color(0xFFE4E6F0)
private val muted = Color(0xFF8888A0)
private val green = Color(0xFF6CD4A0)

private data class ExerciseDetail(
    val id: String, val title: String, val subtitle: String,
    val icon: String, val promptText: String, val durationSec: Int
)

private val details = listOf(
    ExerciseDetail("retelling", "📖 复述训练",
        "看一段文字，用自己的话复述核心观点",
        "📖", "今天学到了什么？用你的话复述一遍。\n不要求完整，抓住核心就行。", 180),
    ExerciseDetail("keywords", "🔑 关键词串联",
        "用3个随机词撑起一段话",
        "🔑", "☕ 咖啡 · 🏢 会议 · 🌧️ 雨", 120),
    ExerciseDetail("observation", "🔍 微观观察",
        "描述今天一件被忽略的小事",
        "🔍", "今天你注意到什么被忽略的小事？\n把它写出画面感来。", 180),
    ExerciseDetail("listening", "🎧 听觉预线",
        "听一段音频，复述听到的内容",
        "🎧", "播放一段音频或播客片段\n（音量调低到刚好能听见）\n然后写下你听到的内容。", 180),
    ExerciseDetail("compression", "🗣️ 压缩表达",
        "2分钟内说清一件事",
        "🗣️", "选一个你今天遇到的事，\n用2分钟把它说清楚。\n有开头、有细节、有结论。", 120),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressExerciseScreen(
    viewModel: ExpressViewModel,
    exerciseId: String,
    onBack: () -> Unit
) {
    val detail = details.find { it.id == exerciseId } ?: return
    val exerciseStates by viewModel.exerciseStates.collectAsState()
    val currentEx = exerciseStates.find { it.id == exerciseId } ?: return

    var text by remember(currentEx) { mutableStateOf(currentEx.content) }
    var isRunning by remember { mutableStateOf(false) }
    var remaining by remember { mutableStateOf(detail.durationSec) }
    var showPrompt by remember { mutableStateOf(true) }

    // 倒计时
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (remaining > 0 && isRunning) {
            delay(1000L)
            remaining--
        }
        if (remaining == 0) isRunning = false
    }

    val progress by animateFloatAsState(
        targetValue = if (detail.durationSec > 0) remaining.toFloat() / detail.durationSec else 0f,
        label = "timer"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail.title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = fg)
            )
        },
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 提示卡片
            if (showPrompt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("练习提示", fontSize = 11.sp, color = muted, letterSpacing = 1.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(detail.promptText, fontSize = 15.sp, color = fg, lineHeight = 24.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showPrompt = false },
                            colors = ButtonDefaults.buttonColors(containerColor = green),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("开始", color = bg) }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // 计时器
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // 进度圈
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                            .background(cardBg)
                    )
                    // 时针效果
                    CircularProgressIndicator(
                        progress = { 1f - progress },
                        modifier = Modifier.size(120.dp),
                        color = green,
                        trackColor = Color(0xFF3A3B50),
                        strokeWidth = 3.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val min = remaining / 60
                        val sec = remaining % 60
                        Text("%02d:%02d".format(min, sec), fontSize = 32.sp, fontWeight = FontWeight.Light, color = fg)
                        Text(if (isRunning) "训练中..." else "准备就绪", fontSize = 11.sp, color = muted)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 控制按钮
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                Button(
                    onClick = {
                        isRunning = !isRunning
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFF44336) else green
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isRunning) "停止" else if (remaining < detail.durationSec) "继续" else "开始计时",
                        color = bg, fontWeight = FontWeight.SemiBold)
                }
                if (isRunning || remaining < detail.durationSec) {
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            remaining = detail.durationSec
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = muted),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("重置") }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 输入区
            Text("我的练习", fontSize = 12.sp, color = muted, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    viewModel.saveExercise(exerciseId, it)
                },
                placeholder = { Text("写下你的练习内容...", color = muted.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fg, unfocusedTextColor = fg,
                    focusedBorderColor = green.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color(0xFF3A3B50)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 完成按钮
            Button(
                onClick = {
                    viewModel.saveExercise(exerciseId, text)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("完成并返回", color = bg, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
