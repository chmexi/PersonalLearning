package com.example.personallearning.ui.screen.daohen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private data class QuestionDef(val key: String, val label: String, val hint: String)

private val questions = listOf(
    QuestionDef("q1", "今天最起波澜的一件事是什么？", "写那件真正让你起波澜的事"),
    QuestionDef("q2", "当时我的第一反应是什么？", "身体感受、本能冲动都可以"),
    QuestionDef("q3", "我其实想得到什么？", "剥开表面，底下真正想要的"),
    QuestionDef("q4", "我其实在害怕什么？", "不敢直面的那部分"),
    QuestionDef("q5", "我给自己找了什么理由？", "自圆其说的那一套"),
    QuestionDef("q6", "今天捞出来的主石头是什么？", "核心洞见，一句话概括"),
    QuestionDef("q7", "如果明天再遇到同样的事，我准备怎么选？", "给明天留一个清醒的选择"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaoHenScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit
) {
    val todayEntry by viewModel.todayEntry.collectAsState()
    val yesterdayStone by viewModel.yesterdayStone.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日道痕", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.sync() }) { Text("同步") }
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
                .padding(16.dp)
        ) {
            // 日期
            Text(
                viewModel.todayStr,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text("每日道痕", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // 昨日石头
            if (yesterdayStone.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("昨天的石头", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text(yesterdayStone, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // 7 个问题
            questions.forEach { q ->
                val currentText = when (q.key) {
                    "q1" -> todayEntry?.q1 ?: ""
                    "q2" -> todayEntry?.q2 ?: ""
                    "q3" -> todayEntry?.q3 ?: ""
                    "q4" -> todayEntry?.q4 ?: ""
                    "q5" -> todayEntry?.q5 ?: ""
                    "q6" -> todayEntry?.q6 ?: ""
                    "q7" -> todayEntry?.q7 ?: ""
                    else -> ""
                }

                Spacer(Modifier.height(8.dp))
                Text(q.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { viewModel.saveAnswer(q.key, it) },
                    placeholder = { Text(q.hint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 6
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "答案自动保存到本地",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
