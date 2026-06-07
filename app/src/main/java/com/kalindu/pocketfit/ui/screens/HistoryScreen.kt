package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var expandedDate by remember { mutableStateOf<String?>(null) }

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
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedSessions.forEach { (date, sessions) ->
            val isExpanded = expandedDate == date

            item(key = "day-$date") {
                DailyHistoryCard(
                    date = date,
                    sessions = sessions,
                    isExpanded = isExpanded,
                    onClick = {
                        expandedDate = if (isExpanded) null else date
                    }
                )
            }

            if (isExpanded) {
                sessions.forEach { session ->
                    item(key = session.id) {
                        HistorySessionCard(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHistoryCard(
    date: String,
    sessions: List<ActivitySession>,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isExpanded) "Tap to hide sessions" else "Tap to view sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryValue(sessions.sumOf { it.steps }.toString(), "Steps")
                HistoryValue(sessions.sumOf { it.calories }.toString(), "Calories")
                HistoryValue(sessions.size.toString(), "Sessions")
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: ActivitySession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                        SessionCalculations.formatDuration(session.actualDurationSeconds),
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

@Composable
private fun HistoryValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
