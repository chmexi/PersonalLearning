package com.example.personallearning.ui.screen.daohen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
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
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private data class QuestionDef(val key: String, val label: String, val hint: String, val icon: String)

private val questions = listOf(
    QuestionDef("q1", "今天最起波澜的一件事是什么？", "写那件真正让你起波澜的事", "🌊"),
    QuestionDef("q2", "当时我的第一反应是什么？", "身体感受、本能冲动都可以", "⚡"),
    QuestionDef("q3", "我其实想得到什么？", "剥开表面，底下真正想要的", "🎯"),
    QuestionDef("q4", "我其实在害怕什么？", "不敢直面的那部分", "😨"),
    QuestionDef("q5", "我给自己找了什么理由？", "自圆其说的那一套", "🛡️"),
    QuestionDef("q6", "今天捞出来的主石头是什么？", "核心洞见，一句话概括", "🪨"),
    QuestionDef("q7", "如果明天再遇到同样的事，我准备怎么选？", "给明天留一个清醒的选择", "🌅"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaoHenScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit
) {
    val todayEntry by viewModel.todayEntry.collectAsState()
    val yesterdayStone by viewModel.yesterdayStone.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    // 使用说明弹窗
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("关于道痕", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("道痕是每日自省的工具，分三步：", fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("① 记下今日波澜 — 那件真正让你起波澜的事", fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("② 捞出主石头 — 看见真正卡住你的那块石头", fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("③ 给明天留一选 — 留一个更清醒的选择", fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("版本 1.0 · 每日道痕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("知道了") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("道痕", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "帮助")
                    }
                    TextButton(onClick = { viewModel.sync() }) { Text("同步", fontSize = 13.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // === 顶部氛围区 ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(viewModel.todayStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("每日道痕", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("先写今日波澜，再捞出主石头，最后给明天留一个更清醒的选择。",
                        fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // === 昨日石头 ===
            if (yesterdayStone.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E5F5).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪨", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("昨天的石头", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7B1FA2))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(yesterdayStone, fontSize = 14.sp, lineHeight = 22.sp,
                            color = Color(0xFF4A148C))
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(20.dp))
            }

            // === 7 个问题（日记卡片风格） ===
            questions.forEachIndexed { index, q ->
                val text = when (q.key) {
                    "q1" -> todayEntry?.q1 ?: ""
                    "q2" -> todayEntry?.q2 ?: ""
                    "q3" -> todayEntry?.q3 ?: ""
                    "q4" -> todayEntry?.q4 ?: ""
                    "q5" -> todayEntry?.q5 ?: ""
                    "q6" -> todayEntry?.q6 ?: ""
                    "q7" -> todayEntry?.q7 ?: ""
                    else -> ""
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 题目标题
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${q.icon}  ${q.label}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp)
                        }
                        Spacer(Modifier.height(10.dp))

                        // 输入框
                        OutlinedTextField(
                            value = text,
                            onValueChange = { viewModel.saveAnswer(q.key, it) },
                            placeholder = {
                                Text(q.hint, fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 6,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // === 底部提示 ===
            Spacer(Modifier.height(16.dp))
            Text(
                "答案自动保存到本地 · 点击右上角同步到云端",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
