package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kalindu.pocketfit.data.model.ActivitySession
import com.kalindu.pocketfit.data.model.SessionStatus
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel
import com.kalindu.pocketfit.utils.SessionCalculations

@Composable
fun SessionScreen(
    onSessionClick: (Int) -> Unit,
    viewModel: SessionViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val sessions by viewModel.todaySessions.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val now by viewModel.nowMillis.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showAddDialog) {
        AddSessionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, duration, stepGoal, calorieGoal ->
                if (viewModel.createSession(name, duration, stepGoal, calorieGoal)) {
                    showAddDialog = false
                }
            }
        )
    }

    val totalSteps = sessions.sumOf { it.steps }
    val totalCalories = sessions.sumOf {
        if (it.status == SessionStatus.ACTIVE) {
            SessionCalculations.caloriesForSteps(it.steps, it.weightUsedKg)
        } else {
            it.calories
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSession == null) {
                        showAddDialog = true
                    } else {
                        viewModel.reportActiveSessionConflict()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Session")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(
                            "Today's Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SummaryValue(
                                totalSteps.toString(),
                                "Steps",
                                Icons.AutoMirrored.Filled.DirectionsWalk
                            )
                            SummaryValue(
                                totalCalories.toString(),
                                "Calories",
                                Icons.Default.LocalFireDepartment
                            )
                            SummaryValue(
                                sessions.size.toString(),
                                "Sessions",
                                Icons.Default.FitnessCenter
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Today's Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (sessions.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            "No sessions created today. Tap + to start one.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    nowMillis = now,
                    onClick = { onSessionClick(session.id) }
                )
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun SessionCard(
    session: ActivitySession,
    nowMillis: Long,
    onClick: () -> Unit
) {
    val metrics = if (session.status == SessionStatus.ACTIVE) {
        SessionCalculations.liveMetrics(session, nowMillis)
    } else {
        com.kalindu.pocketfit.utils.SessionMetrics(
            durationSeconds = session.actualDurationSeconds,
            steps = session.steps,
            calories = session.calories
        )
    }
    val goalProgress = sessionProgress(session, metrics.steps, metrics.calories)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        session.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        SessionCalculations.formatDuration(metrics.durationSeconds),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (session.status == SessionStatus.ACTIVE) {
                    AssistChip(onClick = {}, label = { Text("Active") })
                } else {
                    Text(
                        SessionCalculations.completionReasonLabel(session.completionReason),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${metrics.steps} steps")
                Text("${metrics.calories} kcal")
            }

            if (goalProgress != null) {
                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    goalDescription(session, metrics.steps, metrics.calories),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "No step or calorie goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int?, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var stepGoal by remember { mutableStateOf("") }
    var calorieGoal by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Session name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(
                    value = duration,
                    onValueChange = {
                        duration = it
                        validationError = null
                    },
                    label = "Duration in minutes"
                )
                NumberField(
                    value = stepGoal,
                    onValueChange = {
                        stepGoal = it
                        validationError = null
                    },
                    label = "Step goal (optional)"
                )
                NumberField(
                    value = calorieGoal,
                    onValueChange = {
                        calorieGoal = it
                        validationError = null
                    },
                    label = "Calorie goal (optional)"
                )
                validationError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "The session finishes when its duration expires, either goal is reached, or you finish it manually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationValue = duration.toIntOrNull()
                    val stepGoalValue = stepGoal.takeIf(String::isNotBlank)?.toIntOrNull()
                    val calorieGoalValue =
                        calorieGoal.takeIf(String::isNotBlank)?.toIntOrNull()
                    validationError = when {
                        name.isBlank() -> "Session name is required."
                        durationValue == null || durationValue <= 0 ->
                            "Enter a positive duration."
                        stepGoal.isNotBlank() &&
                            (stepGoalValue == null || stepGoalValue <= 0) ->
                            "Enter a positive step goal or leave it blank."
                        calorieGoal.isNotBlank() &&
                            (calorieGoalValue == null || calorieGoalValue <= 0) ->
                            "Enter a positive calorie goal or leave it blank."
                        else -> null
                    }
                    if (validationError == null) {
                        onAdd(
                            name,
                            requireNotNull(durationValue),
                            stepGoalValue,
                            calorieGoalValue
                        )
                    }
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun sessionProgress(
    session: ActivitySession,
    steps: Int,
    calories: Int
): Float? {
    val progressValues = buildList {
        session.stepGoal?.let { add(steps.toFloat() / it) }
        session.calorieGoal?.let { add(calories.toFloat() / it) }
    }
    return progressValues.maxOrNull()?.coerceIn(0f, 1f)
}

private fun goalDescription(
    session: ActivitySession,
    steps: Int,
    calories: Int
): String = buildList {
    session.stepGoal?.let { add("$steps / $it steps") }
    session.calorieGoal?.let { add("$calories / $it kcal") }
}.joinToString("  •  ")
