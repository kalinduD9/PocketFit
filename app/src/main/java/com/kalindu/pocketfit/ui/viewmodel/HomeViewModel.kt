package com.kalindu.pocketfit.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.model.WeatherResponse
import com.kalindu.pocketfit.data.repository.WeatherRepository
import com.kalindu.pocketfit.utils.StepSensorManager
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _weatherState = mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: State<WeatherUiState> = _weatherState

    // Step Counter State
    private val _currentSteps = mutableStateOf(0)
    val currentSteps: State<Int> = _currentSteps
    
    private val _isStepSensorAvailable = mutableStateOf(true)
    val isStepSensorAvailable: State<Boolean> = _isStepSensorAvailable

    private var stepSensorManager: StepSensorManager? = null

    /**
     * Start tracking physical steps using the sensor.
     */
    fun startStepTracking(context: Context) {
        if (stepSensorManager == null) {
            stepSensorManager = StepSensorManager(context)
        }
        
        val success = stepSensorManager?.startListening { steps ->
            _currentSteps.value = steps
        } ?: false
        
        _isStepSensorAvailable.value = success
    }

    /**
     * Stop tracking to save power when screen is not visible.
     */
    fun stopStepTracking() {
        stepSensorManager?.stopListening()
    }

    fun setWeatherLoading() {
        _weatherState.value = WeatherUiState.Loading
    }

    fun setWeatherError(message: String) {
        _weatherState.value = WeatherUiState.Error(message)
    }

    fun getWeatherForLocation(latitude: Double, longitude: Double) {
        setWeatherLoading()
        viewModelScope.launch {
            repository.fetchWeatherByCoordinates(latitude, longitude).onSuccess { response ->
                _weatherState.value = WeatherUiState.Success(response)
            }.onFailure { error ->
                _weatherState.value = WeatherUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopStepTracking()
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
