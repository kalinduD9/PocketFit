package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.repository.WeatherData
import com.kalindu.pocketfit.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application.applicationContext)

    private val _weatherState = mutableStateOf<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: State<WeatherUiState> = _weatherState

    fun setWeatherLoading() {
        _weatherState.value = WeatherUiState.Loading
    }

    fun setWeatherError(message: String) {
        _weatherState.value = WeatherUiState.Error(message)
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
