package com.example.personallearning.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private val bg = Color(0xFF1E1E2E)
private val surface = Color(0xFF2A2B40)
private val surfaceAlt = Color(0xFF313244)
private val fg = Color(0xFFCDD6F4)
private val muted = Color(0xFF6C7086)
private val accent = Color(0xFF89B4FA)
private val green = Color(0xFFA6E3A1)

private data class ModuleDef(
    val title: String, val subtitle: String,
    val color: Color, val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DaoHenViewModel,
    onDaoHenClick: () -> Unit,
    onExpressClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val daoHenProgress by viewModel.todayProgress.collectAsState()
    val modules = listOf(
        ModuleDef("道痕", "每日7问，自省反思", Color(0xFF89B4FA), onClick = onDaoHenClick),
        ModuleDef("英语口语", "即将推出", Color(0xFFF5C2E7), onClick = {}),
        ModuleDef("表达练习", "每天15分钟，磨一把嘴", Color(0xFFA6E3A1), onClick = onExpressClick),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("自我修行", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = fg)
                        Text("每日一点，日有所进", fontSize = 12.sp, color = muted)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = fg)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = fg)
            )
        },
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text("选择修行", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = muted)

            modules.forEach { module ->
                ModuleCard(
                    title = module.title,
                    subtitle = module.subtitle,
                    accentColor = module.color,
                    onClick = module.onClick
                )
            }

            Spacer(Modifier.weight(1f))

            // 今日道痕状态 - Obsidian 风格
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(surface)
                    .clickable(onClick = onDaoHenClick)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = surfaceAlt
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("📝", fontSize = 18.sp) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("今日道痕", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = fg)
                        Text(
                            when {
                                daoHenProgress.isComplete -> "今日已完成"
                                daoHenProgress.answeredCount > 0 -> "已写 ${daoHenProgress.answeredCount}/7"
                                else -> "记录今天的石头"
                            },
                            fontSize = 12.sp,
                            color = muted
                        )
                        if (daoHenProgress.mainStone.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                daoHenProgress.mainStone,
                                fontSize = 12.sp,
                                color = fg.copy(alpha = 0.78f),
                                maxLines = 1
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = surfaceAlt
                    ) {
                        Text(if (daoHenProgress.answeredCount > 0) " 继续 " else " 开始 ",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp, color = accent)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModuleCard(
    title: String, subtitle: String,
    accentColor: Color, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧色条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = fg)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = muted)
            }
            Text("›", fontSize = 22.sp, color = muted.copy(alpha = 0.4f))
        }
    }
}
