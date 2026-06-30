package com.example.personallearning.ui.screen.daohen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private data class QuestionDef(val key: String, val label: String, val hint: String)

private val questions = listOf(
    QuestionDef("q1", "今天最起波澜的一件事是什么。", "写那件真正让你起波澜的事"),
    QuestionDef("q2", "当时我的第一反应是什么。", "身体感受、本能冲动都可以"),
    QuestionDef("q3", "我其实想得到什么。", "剥开表面，底下真正想要的"),
    QuestionDef("q4", "我其实在害怕什么。", "不敢直面的那部分"),
    QuestionDef("q5", "我给自己找了什么理由。", "自圆其说的那一套"),
    QuestionDef("q6", "今天捞出来的主石头是什么。", "核心洞见，一句话概括"),
    QuestionDef("q7", "如果明天再遇到同样的事，我准备怎么选。", "给明天留一个清醒的选择"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaoHenScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val todayEntry by viewModel.todayEntry.collectAsState()
    val yesterdayStone by viewModel.yesterdayStone.collectAsState()

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
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "历史")
                    }
                    TextButton(onClick = { viewModel.sync() }) { Text("同步", fontSize = 13.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(viewModel.todayStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("每日道痕", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }

            // === 今日写法：三步说明 ===
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("今日写法", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.height(12.dp))
                    WritingStep("01", "记下今日波澜", "先抓住那件真正让你起波澜的事，不急着解释，也不急着下结论。")
                    Spacer(Modifier.height(10.dp))
                    WritingStep("02", "捞出主石头", "把想要、害怕和自我辩护一起捞出来，看见真正卡住你的那块石头。")
                    Spacer(Modifier.height(10.dp))
                    WritingStep("03", "给明天留一选", "最后只留一个更清醒、能执行的选择，给下一次相似的处境做准备。")
                }
            }
            Spacer(Modifier.height(16.dp))

            // === 昨日石头 ===
            if (yesterdayStone.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E5F5).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("昨天的石头", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7B1FA2))
                        Spacer(Modifier.height(6.dp))
                        Text(yesterdayStone, fontSize = 14.sp, lineHeight = 22.sp,
                            color = Color(0xFF4A148C))
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(8.dp))
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(26.dp),
                                shape = RoundedCornerShape(13.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(q.label, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = text,
                            onValueChange = { viewModel.saveAnswer(q.key, it) },
                            placeholder = {
                                Text(q.hint, fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2, maxLines = 6,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // === 放下这块石头 ===
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { /* 已自动保存，无需额外操作 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B1FA2)
                )
            ) {
                Text("放下这块石头", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))

            // === 底部提示 ===
            Text(
                "答案自动保存到本地 · 点击右上角同步到云端",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WritingStep(step: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(step, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 12.sp, lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
        }
    }
}
