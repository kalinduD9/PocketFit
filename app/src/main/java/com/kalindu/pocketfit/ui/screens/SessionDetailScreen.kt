package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalindu.pocketfit.data.model.SessionStatus
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel
import com.kalindu.pocketfit.utils.SessionCalculations
import com.kalindu.pocketfit.utils.SessionMetrics

@Composable
fun SessionDetailScreen(
    sessionId: Int,
    viewModel: SessionViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val now by viewModel.nowMillis.collectAsState()
    val session = sessions.find { it.id == sessionId }

    if (session == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Session not found")
        }
        return
    }

    val metrics = if (session.status == SessionStatus.ACTIVE) {
        SessionCalculations.liveMetrics(session, now)
    } else {
        SessionMetrics(
            durationSeconds = session.actualDurationSeconds,
            steps = session.steps,
            calories = session.calories
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    session.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${SessionCalculations.formatDate(session.startTimeMillis)} at " +
                        SessionCalculations.formatTime(session.startTimeMillis),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (session.status == SessionStatus.ACTIVE) {
                        "Active"
                    } else {
                        SessionCalculations.completionReasonLabel(session.completionReason)
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailValue(
                "Actual Time",
                SessionCalculations.formatDuration(metrics.durationSeconds),
                Modifier.weight(1f)
            )
            DetailValue(
                "Planned Time",
                "${session.plannedDurationMinutes} min",
                Modifier.weight(1f)
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailValue("Steps", metrics.steps.toString(), Modifier.weight(1f))
            DetailValue("Calories", "${metrics.calories} kcal", Modifier.weight(1f))
        }

        GoalCard(
            title = "Step Goal",
            current = metrics.steps,
            goal = session.stepGoal
        )
        GoalCard(
            title = "Calorie Goal",
            current = metrics.calories,
            goal = session.calorieGoal
        )

        if (session.endTimeMillis != null) {
            DetailValue(
                "End Time",
                SessionCalculations.formatTime(session.endTimeMillis),
                Modifier.fillMaxWidth()
            )
        }

        Text(
            "Calories are estimated from steps using the saved session weight " +
                "(${"%.1f".format(session.weightUsedKg)} kg).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (session.status == SessionStatus.ACTIVE) {
            Button(
                onClick = {
                    viewModel.finishActiveSession()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish Session")
            }
        } else {
            OutlinedButton(
                onClick = {
                    viewModel.deleteSession(session)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Delete Session")
            }
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoalCard(title: String, current: Int, goal: Int?) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (goal == null) {
                Text(
                    "No goal set",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    progress = { (current.toFloat() / goal).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("$current / $goal")
            }
        }
    }
}
