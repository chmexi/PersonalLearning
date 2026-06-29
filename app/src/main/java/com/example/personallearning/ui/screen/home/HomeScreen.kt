package com.example.personallearning.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ModuleDef(
    val title: String, val subtitle: String, val emoji: String,
    val gradient: List<Color>, val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDaoHenClick: () -> Unit
) {
    val modules = listOf(
        ModuleDef("道痕", "每日7问，自省反思", "✍️",
            listOf(Color(0xFF667eea), Color(0xFF764ba2)),
            onClick = onDaoHenClick),
        ModuleDef("英语口语", "即将推出", "🗣️",
            listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
            onClick = {}),
        ModuleDef("表达练习", "即将推出", "🎤",
            listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
            onClick = {}),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("自我修行", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("每日一点，日有所进", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("选择修行", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            modules.forEach { module ->
                ModuleGradientCard(
                    title = module.title,
                    subtitle = module.subtitle,
                    emoji = module.emoji,
                    gradient = module.gradient,
                    onClick = module.onClick
                )
            }

            Spacer(Modifier.weight(1f))

            // 今日道痕状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📝", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("今日道痕", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(" 开始记录 ", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModuleGradientCard(
    title: String, subtitle: String, emoji: String,
    gradient: List<Color>, onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradient),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(Modifier.weight(1f))
                Text("›", fontSize = 28.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}
