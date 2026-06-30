package com.example.personallearning.ui.screen.express

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.ui.viewmodel.ExerciseState
import com.example.personallearning.ui.viewmodel.ExpressViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val bg = Color(0xFF1E1E2E)
private val fg = Color(0xFFCDD6F4)
private val muted = Color(0xFF6C7086)
private val accent = Color(0xFF89B4FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressHomeScreen(
    viewModel: ExpressViewModel,
    onBack: () -> Unit,
    onExerciseClick: (String) -> Unit
) {
    val exerciseStates by viewModel.exerciseStates.collectAsState()
    val todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE))
    val doneCount = exerciseStates.count { it.done }
    val totalCount = exerciseStates.size

    // 打卡天数
    val streakDays = 7

    LaunchedEffect(Unit) { viewModel.loadToday() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("表达训练", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg, titleContentColor = fg
                )
            )
        },
        containerColor = bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            // 日期 + 连续天数
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(todayDate, fontSize = 12.sp, color = muted)
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF313244)
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("连续 $streakDays 天", fontSize = 12.sp, color = Color(0xFFA6E3A1))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 进度条
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF313244))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accent)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("今日进度", fontSize = 11.sp, color = muted)
                    Text("$doneCount/$totalCount", fontSize = 11.sp, color = if (doneCount == totalCount) Color(0xFFA6E3A1) else muted)
                }
                Spacer(Modifier.height(20.dp))
            }

            // 练习列表
            items(exerciseStates, key = { it.id }) { ex ->
                ExerciseRow(
                    exercise = ex,
                    onClick = { onExerciseClick(ex.id) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: ExerciseState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 序号圆
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (exercise.done) Color(0xFF45475A) else Color(0xFF313244)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (exercise.done) {
                    Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA6E3A1))
                } else {
                    val idx = listOf("retelling","keywords","observation","listening","compression").indexOf(exercise.id) + 1
                    Text("$idx", fontSize = 12.sp, color = muted)
                }
            }
        }
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(exercise.icon + "  " + exercise.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = fg)
            Text(exercise.subtitle, fontSize = 12.sp, color = muted, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Text("${exercise.durationMin}分钟", fontSize = 11.sp, color = Color(0xFF585B70))
    }

    // 分割线
    Box(
        modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF313244))
    )
}
