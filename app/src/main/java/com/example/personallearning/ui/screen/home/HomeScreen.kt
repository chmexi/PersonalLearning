package com.example.personallearning.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private val bg = Color(0xFF191B22)
private val surface = Color(0xFF252832)
private val surfaceAlt = Color(0xFF30343F)
private val fg = Color(0xFFF2F0EA)
private val muted = Color(0xFFA5A7AE)
private val accent = Color(0xFF7FC8A9)
private val warm = Color(0xFFE8B86D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DaoHenViewModel,
    onDaoHenClick: () -> Unit,
    onExpressClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val progress by viewModel.todayProgress.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val week by viewModel.weeklySummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("修行", color = fg, fontWeight = FontWeight.Bold)
                        Text("今天只向前走一小步", color = muted, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "设置", tint = fg)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        containerColor = bg
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("今日修行", color = muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            PrimaryTaskCard(
                answered = progress.answeredCount,
                complete = progress.isComplete,
                onClick = onDaoHenClick
            )

            if (pendingAction != null) {
                Surface(color = warm.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.fillMaxWidth().clickable(onClick = onDaoHenClick).padding(15.dp)) {
                        Text("待兑现的选择", color = warm, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            pendingAction!!.q7,
                            color = fg,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("进入道痕确认结果", color = muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }

            WeeklyReviewCard(
                recordedDays = week.recordedDays,
                completedDays = week.completedDays,
                verifiedActions = week.verifiedActions,
                fulfilledActions = week.fulfilledActions,
                tags = week.topTags
            )

            Text("专项练习", color = muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            SecondaryTaskCard(
                title = "表达训练",
                subtitle = if (progress.mainStone.isNotBlank()) "用今天的主石头练习说清一件事" else "计时练习，让表达更清楚",
                onClick = onExpressClick
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrimaryTaskCard(answered: Int, complete: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditNote, null, tint = accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (complete) "今天的道痕已完成" else "继续今天的道痕", color = fg, fontWeight = FontWeight.SemiBold)
                    Text(if (answered == 0) "从一件起波澜的事开始" else "已回答 $answered/7", color = muted, fontSize = 12.sp)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "进入", tint = accent)
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { answered / 7f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = accent,
                trackColor = surfaceAlt
            )
        }
    }
}

@Composable
private fun WeeklyReviewCard(
    recordedDays: Int,
    completedDays: Int,
    verifiedActions: Int,
    fulfilledActions: Int,
    tags: List<Pair<String, Int>>
) {
    Surface(color = surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoGraph, null, tint = warm)
                Text("本周变化", color = fg, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                Metric("记录", "$recordedDays 天", Modifier.weight(1f))
                Metric("完整道痕", "$completedDays 次", Modifier.weight(1f))
                Metric("行动兑现", "$fulfilledActions/$verifiedActions", Modifier.weight(1f))
            }
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("反复出现：${tags.joinToString(" · ") { "${it.first} ${it.second}" }}", color = muted, fontSize = 12.sp)
            } else {
                Spacer(Modifier.height(12.dp))
                Text("为道痕添加标签后，这里会显示反复出现的主题。", color = muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, color = fg, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = muted, fontSize = 10.sp)
    }
}

@Composable
private fun SecondaryTaskCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, null, tint = accent)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, color = fg, fontWeight = FontWeight.Medium)
                Text(subtitle, color = muted, fontSize = 11.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "进入", tint = muted)
        }
    }
}
