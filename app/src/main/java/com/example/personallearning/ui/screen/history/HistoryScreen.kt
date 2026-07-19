package com.example.personallearning.ui.screen.history

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.ui.viewmodel.DaoHenViewModel

private val bg = Color(0xFF1E1E2E)
private val surface = Color(0xFF2A2B40)
private val surfaceAlt = Color(0xFF313244)
private val fg = Color(0xFFCDD6F4)
private val muted = Color(0xFF6C7086)
private val accent = Color(0xFF89B4FA)
private val pink = Color(0xFFF5C2E7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit,
    onEntryClick: (String) -> Unit
) {
    val allEntries by viewModel.allEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = fg,
                    navigationIconContentColor = fg
                )
            )
        },
        containerColor = bg
    ) { padding ->
        if (allEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无记录", color = muted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allEntries, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onClick = { onEntryClick(entry.date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: DaoHenEntry,
    onClick: () -> Unit
) {
    val hasStone = entry.q6.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasStone) surface else surfaceAlt.copy(alpha = 0.72f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = fg)
                if (entry.q1.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = surfaceAlt
                    ) {
                        Text("已记录",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = accent)
                    }
                }
            }
            if (hasStone) {
                Spacer(Modifier.height(8.dp))
                Text(entry.q6, fontSize = 14.sp, lineHeight = 20.sp,
                    color = pink,
                    maxLines = 3)
            }
            if (entry.q1.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(entry.q1.take(60),
                    fontSize = 12.sp, lineHeight = 18.sp,
                    color = muted,
                    maxLines = 2)
            }
        }
    }
}
