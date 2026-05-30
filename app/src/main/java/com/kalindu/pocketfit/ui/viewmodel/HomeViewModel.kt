package com.kalindu.pocketfit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.model.WeatherResponse
import com.kalindu.pocketfit.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

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
            repository.fetchWeatherByCoordinates(latitude, longitude).onSuccess { response ->
                _weatherState.value = WeatherUiState.Success(response)
            }.onFailure { error ->
                _weatherState.value = WeatherUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
