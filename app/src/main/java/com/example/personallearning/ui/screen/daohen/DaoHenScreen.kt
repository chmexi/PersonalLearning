package com.example.personallearning.ui.screen.daohen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.ui.viewmodel.DaoHenViewModel
import com.example.personallearning.ui.viewmodel.SyncUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val bg = Color(0xFF191B22)
private val surface = Color(0xFF252832)
private val surfaceAlt = Color(0xFF30343F)
private val fg = Color(0xFFF2F0EA)
private val muted = Color(0xFFA5A7AE)
private val accent = Color(0xFF7FC8A9)
private val warm = Color(0xFFE8B86D)
private val danger = Color(0xFFE78B8B)

private data class QuestionDef(
    val key: String,
    val stage: String,
    val label: String,
    val hint: String
)

private val questions = listOf(
    QuestionDef("q1", "看见发生", "今天最起波澜的一件事是什么？", "只写事实：发生了什么，谁在场？"),
    QuestionDef("q2", "看见反应", "当时我的第一反应是什么？", "身体感受、情绪或本能冲动都可以"),
    QuestionDef("q3", "看见渴望", "我其实想得到什么？", "剥开表面的诉求，写下真正想要的"),
    QuestionDef("q4", "看见恐惧", "我其实在害怕什么？", "如果不再回避，最担心发生什么？"),
    QuestionDef("q5", "看见辩护", "我给自己找了什么理由？", "写下当时用来解释或保护自己的话"),
    QuestionDef("q6", "捞出石头", "今天捞出来的主石头是什么？", "用一句话概括今天看见的核心模式"),
    QuestionDef("q7", "留下选择", "明天再遇到同样的事，我准备怎么选？", "写一个具体、可执行的小动作")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaoHenScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentEntry by viewModel.currentEntry.collectAsState()
    val yesterdayStone by viewModel.yesterdayStone.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncConflicts by viewModel.syncConflicts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }

    val answers = currentEntry.answers()
    val answeredCount = answers.count { it.isNotBlank() }
    val isToday = selectedDate == LocalDate.now()

    LaunchedEffect(selectedDate) {
        currentStep = 0
        showReview = false
        focusManager.clearFocus()
    }

    LaunchedEffect(currentEntry?.date) {
        if (currentEntry?.date == selectedDate.toString()) {
            currentStep = answers.indexOfFirst { it.isBlank() }.takeIf { it >= 0 } ?: 6
        }
    }

    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncUiState.Success -> snackbarHostState.showSnackbar(state.message)
            is SyncUiState.Failure -> snackbarHostState.showSnackbar("同步失败：${state.message}")
            else -> Unit
        }
    }

    if (showDatePicker) {
        DaoHenDatePickerDialog(
            selectedDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                viewModel.selectDate(it)
                showDatePicker = false
            }
        )
    }

    syncConflicts.firstOrNull()?.let { conflict ->
        ConflictDialog(
            conflict = conflict,
            onKeepLocal = { viewModel.resolveConflict(keepLocal = true) },
            onKeepRemote = { viewModel.resolveConflict(keepLocal = false) }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(data, containerColor = surfaceAlt, contentColor = fg)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("道痕", fontWeight = FontWeight.Bold)
                        Text(
                            if (isToday) "今日记录" else selectedDate.toString(),
                            fontSize = 11.sp,
                            color = muted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, "历史记录")
                    }
                    IconButton(
                        onClick = { viewModel.sync() },
                        enabled = syncState !is SyncUiState.Syncing
                    ) {
                        Icon(Icons.Default.Sync, if (syncState is SyncUiState.Syncing) "同步中" else "同步")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = fg,
                    navigationIconContentColor = fg,
                    actionIconContentColor = fg
                )
            )
        },
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            DateNavigator(
                selectedDate = selectedDate,
                onPrevious = { viewModel.moveDate(-1) },
                onNext = { viewModel.moveDate(1) },
                onPickDate = { showDatePicker = true }
            )

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        questions[currentStep].stage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = warm
                    )
                    Text(
                        "第 ${currentStep + 1} 步，共 ${questions.size} 步",
                        fontSize = 13.sp,
                        color = muted
                    )
                }
                Text("$answeredCount / ${questions.size} 已回答", fontSize = 12.sp, color = muted)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { answeredCount / questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = accent,
                trackColor = surfaceAlt
            )

            if (yesterdayStone.isNotBlank() && currentStep == 0) {
                Spacer(Modifier.height(18.dp))
                YesterdayStone(yesterdayStone)
            }

            Spacer(Modifier.height(18.dp))
            QuestionEditor(
                question = questions[currentStep],
                value = answers[currentStep],
                saveStatus = saveStatus,
                onValueChange = { viewModel.saveAnswer(questions[currentStep].key, it) }
            )

            Spacer(Modifier.height(18.dp))
            StepActions(
                currentStep = currentStep,
                answerIsBlank = answers[currentStep].isBlank(),
                onBack = {
                    focusManager.clearFocus()
                    currentStep = (currentStep - 1).coerceAtLeast(0)
                },
                onNext = {
                    focusManager.clearFocus()
                    if (currentStep < questions.lastIndex) {
                        currentStep += 1
                    } else {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        showReview = true
                    }
                }
            )

            Spacer(Modifier.height(14.dp))
            ReviewSection(
                answers = answers,
                expanded = showReview,
                onToggle = { showReview = !showReview },
                onSelect = {
                    currentStep = it
                    showReview = false
                }
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DateNavigator(
    selectedDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    val today = LocalDate.now()
    val dayLabel = when (selectedDate) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)
    }
    Surface(color = surface, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "前一天", tint = fg)
            }
            Row(
                modifier = Modifier
                    .clickable(onClick = onPickDate)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, Modifier.size(18.dp), tint = accent)
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayLabel, fontWeight = FontWeight.SemiBold, color = fg, fontSize = 14.sp)
                    Text(
                        selectedDate.format(DateTimeFormatter.ofPattern("M月d日")),
                        color = muted,
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(onClick = onNext, enabled = selectedDate < today) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    "后一天",
                    tint = if (selectedDate < today) fg else muted.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun YesterdayStone(text: String) {
    Surface(color = warm.copy(alpha = 0.10f), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("上一次看见的石头", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = warm)
            Spacer(Modifier.height(5.dp))
            Text(text, fontSize = 13.sp, lineHeight = 20.sp, color = fg, maxLines = 3)
        }
    }
}

@Composable
private fun QuestionEditor(
    question: QuestionDef,
    value: String,
    saveStatus: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                question.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 30.sp,
                color = fg
            )
            Spacer(Modifier.height(8.dp))
            Text(question.hint, fontSize = 13.sp, lineHeight = 20.sp, color = muted)
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("从这里写下，不必组织得很完整…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                maxLines = 10,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fg,
                    unfocusedTextColor = fg,
                    cursorColor = accent,
                    focusedContainerColor = bg.copy(alpha = 0.45f),
                    unfocusedContainerColor = bg.copy(alpha = 0.45f),
                    focusedBorderColor = accent,
                    unfocusedBorderColor = surfaceAlt,
                    focusedPlaceholderColor = muted.copy(alpha = 0.65f),
                    unfocusedPlaceholderColor = muted.copy(alpha = 0.65f)
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    saveStatus.isNotBlank() -> saveStatus
                    value.isNotBlank() -> "已保存在本机"
                    else -> "输入后自动保存"
                },
                modifier = Modifier.align(Alignment.End),
                color = muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StepActions(
    currentStep: Int,
    answerIsBlank: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(0.36f).height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = fg),
                border = BorderStroke(1.dp, surfaceAlt)
            ) {
                Text("上一步")
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(0.64f).height(48.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = bg)
        ) {
            if (currentStep == questions.lastIndex && !answerIsBlank) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("完成记录", fontWeight = FontWeight.SemiBold)
            } else {
                Text(if (answerIsBlank) "暂时跳过" else "下一步", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReviewSection(
    answers: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Surface(color = surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("回看全部回答", modifier = Modifier.weight(1f), color = fg, fontWeight = FontWeight.Medium)
                Text("${answers.count { it.isNotBlank() }}/${answers.size}", fontSize = 12.sp, color = muted)
                Spacer(Modifier.width(6.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    if (expanded) "收起" else "展开",
                    tint = muted
                )
            }
            if (expanded) {
                answers.forEachIndexed { index, answer ->
                    HorizontalDivider(color = surfaceAlt)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (answer.isBlank()) surfaceAlt else accent.copy(alpha = 0.16f),
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (answer.isBlank()) muted else accent
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                questions[index].stage,
                                fontSize = 12.sp,
                                color = if (answer.isBlank()) muted else fg
                            )
                            Text(
                                answer.ifBlank { "未回答" },
                                fontSize = 11.sp,
                                color = muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaoHenDatePickerDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val todayMillis = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli(),
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Instant.ofEpochMilli(it).atZone(zone).toLocalDate())
                } ?: onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun ConflictDialog(
    conflict: com.example.personallearning.data.repository.SyncConflict,
    onKeepLocal: () -> Unit,
    onKeepRemote: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("${conflict.local.date} 的记录有冲突") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("本机和云端都修改了这一天，请选择要保留的版本。")
                ConflictVersion("本机", conflict.local)
                ConflictVersion("云端", conflict.remote)
            }
        },
        confirmButton = { TextButton(onClick = onKeepLocal) { Text("保留本机") } },
        dismissButton = { TextButton(onClick = onKeepRemote) { Text("保留云端") } }
    )
}

@Composable
private fun ConflictVersion(title: String, entry: DaoHenEntry) {
    val summary = entry.answers().filter { it.isNotBlank() }.joinToString("\n").ifBlank { "（空记录）" }
    Surface(color = surfaceAlt, shape = RoundedCornerShape(6.dp)) {
        Column(Modifier.fillMaxWidth().padding(10.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(summary, color = fg, maxLines = 3, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

private fun DaoHenEntry?.answers(): List<String> = listOf(
    this?.q1.orEmpty(),
    this?.q2.orEmpty(),
    this?.q3.orEmpty(),
    this?.q4.orEmpty(),
    this?.q5.orEmpty(),
    this?.q6.orEmpty(),
    this?.q7.orEmpty()
)
