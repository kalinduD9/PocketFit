package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.model.DailyGoals
import com.kalindu.pocketfit.data.repository.DailyGoalsRepository
import com.kalindu.pocketfit.data.repository.WeatherData
import com.kalindu.pocketfit.data.repository.WeatherRepository
import com.kalindu.pocketfit.utils.DailyGoalsValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application.applicationContext)
    private val goalsRepository =
        DailyGoalsRepository(application.applicationContext)

    private val _weatherState = mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: State<WeatherUiState> = _weatherState

    private val _dailyGoals = MutableStateFlow(goalsRepository.load())
    val dailyGoals: StateFlow<DailyGoals> = _dailyGoals.asStateFlow()

    private val _goalsMessage = MutableStateFlow<String?>(null)
    val goalsMessage: StateFlow<String?> = _goalsMessage.asStateFlow()

    fun saveDailyGoals(stepGoal: String, calorieGoal: String): Boolean {
        val validation = DailyGoalsValidation.validate(stepGoal, calorieGoal)
        val goals = validation.goals
        if (goals == null) {
            _goalsMessage.value = validation.message
            return false
        }

        goalsRepository.save(goals)
        _dailyGoals.value = goals
        _goalsMessage.value = "Daily goals saved."
        return true
    }

    fun clearGoalsMessage() {
        _goalsMessage.value = null
    }

    fun setWeatherLoading() {
        _weatherState.value = WeatherUiState.Loading
    }

    fun getWeatherForLocation(latitude: Double, longitude: Double) {
        setWeatherLoading()
        viewModelScope.launch {
            repository.fetchWeatherByCoordinates(latitude, longitude).onSuccess { weatherData ->
                _weatherState.value = WeatherUiState.Success(weatherData)
            }.onFailure { error ->
                _weatherState.value = WeatherUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun loadOfflineWeather() {
        setWeatherLoading()
        viewModelScope.launch {
            repository.loadOfflineWeather().onSuccess { weatherData ->
                _weatherState.value = WeatherUiState.Success(weatherData)
            }.onFailure {
                _weatherState.value = WeatherUiState.Error("Offline weather data is unavailable.")
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
