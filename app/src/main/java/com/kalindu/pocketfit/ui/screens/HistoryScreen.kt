package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalindu.pocketfit.data.model.ActivitySession
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel
import com.kalindu.pocketfit.utils.SessionCalculations

@Composable
fun HistoryScreen(
    viewModel: SessionViewModel,
    onSessionClick: (Int) -> Unit
) {
    val historicalSessions by viewModel.historicalSessions.collectAsState()
    val groupedSessions = historicalSessions.groupBy {
        SessionCalculations.formatDate(it.startTimeMillis)
    }

    if (groupedSessions.isEmpty()) {
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "Completed sessions from previous days will appear here.",
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedSessions.forEach { (date, sessions) ->
            item(key = "header-$date") {
                Text(
                    date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            item(key = "summary-$date") {
                DailySessionSummary(sessions)
            }
            items(sessions, key = { it.id }) { session ->
                Card(
                    onClick = { onSessionClick(session.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(session.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${SessionCalculations.formatTime(session.startTimeMillis)}  •  " +
                                    SessionCalculations.formatDuration(
                                        session.actualDurationSeconds
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${session.steps} steps")
                            Text(
                                "${session.calories} kcal",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DailySessionSummary(sessions: List<ActivitySession>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HistoryValue(sessions.sumOf { it.steps }.toString(), "Steps")
            HistoryValue(sessions.sumOf { it.calories }.toString(), "Calories")
            HistoryValue(sessions.size.toString(), "Sessions")
        }
    }
}

@Composable
private fun HistoryValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
