package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalindu.pocketfit.ui.viewmodel.ExerciseDetailUiState
import com.kalindu.pocketfit.ui.viewmodel.ExerciseViewModel

@Composable
fun ExerciseDetailScreen(
    exerciseId: Int,
    viewModel: ExerciseViewModel
) {
    val state by viewModel.detailState.collectAsState()

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    when (val current = state) {
        ExerciseDetailUiState.Idle,
        ExerciseDetailUiState.Loading -> LoadingContent()
        is ExerciseDetailUiState.Error -> MessageContent(
            message = current.message,
            buttonLabel = "Retry",
            onClick = { viewModel.loadExercise(exerciseId) }
        )
        is ExerciseDetailUiState.Success -> {
            val exercise = current.exercise
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExerciseSourceBanner(current.source)
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            exercise.category,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                ExerciseDetailCard("Instructions", exercise.description)
                ExerciseDetailCard(
                    "Muscles",
                    exercise.muscles.joinToString().ifBlank { "Not specified" }
                )
                ExerciseDetailCard(
                    "Equipment",
                    exercise.equipment.joinToString().ifBlank { "Not specified" }
                )
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(title: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
