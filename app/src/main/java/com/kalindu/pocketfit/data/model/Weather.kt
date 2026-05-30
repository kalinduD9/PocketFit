package com.kalindu.pocketfit.data.model

import com.google.gson.annotations.SerializedName

// Data model for the OpenWeather API response.
data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherInfo>,
    @SerializedName("main") val main: MainInfo,
    @SerializedName("name") val cityName: String
)

data class WeatherInfo(
    @SerializedName("main") val main: String, // e.g., "Rain", "Clouds", "Clear"
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class MainInfo(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int
)
