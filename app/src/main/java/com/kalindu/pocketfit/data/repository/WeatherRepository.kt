package com.kalindu.pocketfit.data.repository

import com.kalindu.pocketfit.BuildConfig
import com.kalindu.pocketfit.data.api.WeatherApiService
import com.kalindu.pocketfit.data.model.WeatherResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"

    private val apiService: WeatherApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(WeatherApiService::class.java)
    }

    suspend fun fetchWeatherByCoordinates(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return try {
            val response = apiService.getCurrentWeatherByCoordinates(latitude, longitude, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
