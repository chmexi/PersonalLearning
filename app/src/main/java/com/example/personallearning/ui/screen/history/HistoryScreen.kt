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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DaoHenViewModel,
    onBack: () -> Unit
) {
    val allEntries by viewModel.allEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (allEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allEntries, key = { it.id }) { entry ->
                    HistoryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: DaoHenEntry) {
    val hasStone = entry.q6.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasStone) Color(0xFFF3E5F5).copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (entry.q1.isNotBlank()) {
                    Text("今日已记",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            if (hasStone) {
                Spacer(Modifier.height(8.dp))
                Text("🪨 ", fontSize = 12.sp, color = Color(0xFF7B1FA2))
                Text(entry.q6, fontSize = 14.sp, lineHeight = 20.sp,
                    color = Color(0xFF4A148C),
                    maxLines = 3)
            }
            if (entry.q1.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(entry.q1.take(60),
                    fontSize = 12.sp, lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2)
            }
        }
    }
}
