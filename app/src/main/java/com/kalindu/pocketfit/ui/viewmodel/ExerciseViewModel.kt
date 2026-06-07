package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.data.repository.ExerciseRepository
import com.kalindu.pocketfit.data.repository.ExerciseSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExerciseRepository(application.applicationContext)

    private val _listState = MutableStateFlow<ExerciseListUiState>(
        ExerciseListUiState.Loading
    )
    val listState: StateFlow<ExerciseListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<ExerciseDetailUiState>(
        ExerciseDetailUiState.Idle
    )
    val detailState: StateFlow<ExerciseDetailUiState> = _detailState.asStateFlow()

    init {
        loadExercises()
    }

    fun loadExercises() {
        _listState.value = ExerciseListUiState.Loading
        viewModelScope.launch {
            repository.loadExercises()
                .onSuccess { data ->
                    _listState.value = if (data.exercises.isEmpty()) {
                        ExerciseListUiState.Empty
                    } else {
                        ExerciseListUiState.Success(data.exercises, data.source)
                    }
                }
                .onFailure { error ->
                    _listState.value = ExerciseListUiState.Error(
                        error.message ?: "Exercises could not be loaded."
                    )
                }
        }
    }

    fun loadExercise(id: Int) {
        _detailState.value = ExerciseDetailUiState.Loading
        viewModelScope.launch {
            repository.loadExercise(id)
                .onSuccess { data ->
                    _detailState.value = ExerciseDetailUiState.Success(
                        data.exercise,
                        data.source
                    )
                }
                .onFailure { error ->
                    _detailState.value = ExerciseDetailUiState.Error(
                        error.message ?: "Exercise details could not be loaded."
                    )
                }
        }
    }
}

sealed class ExerciseListUiState {
    data object Loading : ExerciseListUiState()
    data object Empty : ExerciseListUiState()
    data class Success(
        val exercises: List<Exercise>,
        val source: ExerciseSource
    ) : ExerciseListUiState()
    data class Error(val message: String) : ExerciseListUiState()
}

sealed class ExerciseDetailUiState {
    data object Idle : ExerciseDetailUiState()
    data object Loading : ExerciseDetailUiState()
    data class Success(
        val exercise: Exercise,
        val source: ExerciseSource
    ) : ExerciseDetailUiState()
    data class Error(val message: String) : ExerciseDetailUiState()
}
