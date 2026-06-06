package com.kalindu.pocketfit.data.repository

import android.content.Context
import com.kalindu.pocketfit.BuildConfig
import com.kalindu.pocketfit.data.api.WeatherApiService
import com.kalindu.pocketfit.data.model.WeatherResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class WeatherRepository(private val context: Context) {
    private val apiKey = BuildConfig.OPENWEATHER_API_KEY
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val gson = Gson()
    private val cacheFile = File(context.filesDir, WEATHER_CACHE_FILE)

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

    suspend fun fetchWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentWeatherByCoordinates(latitude, longitude, apiKey)
            val cachedAt = System.currentTimeMillis()
            cacheWeather(response, cachedAt)
            Result.success(
                WeatherData(
                    weather = response,
                    source = WeatherSource.LIVE,
                    savedAtEpochMillis = cachedAt
                )
            )
        } catch (error: Exception) {
            loadOfflineWeather().recoverCatching {
                throw IllegalStateException(
                    "Weather is unavailable online and no offline data could be loaded.",
                    error
                )
            }
        }
    }

    suspend fun loadOfflineWeather(): Result<WeatherData> = withContext(Dispatchers.IO) {
        runCatching {
            readCachedWeather() ?: readBundledWeather()
        }
    }

    private fun cacheWeather(weather: WeatherResponse, cachedAt: Long) {
        val temporaryFile = File(context.filesDir, "$WEATHER_CACHE_FILE.tmp")
        temporaryFile.writeText(gson.toJson(CachedWeather(weather, cachedAt)))
        if (!temporaryFile.renameTo(cacheFile)) {
            temporaryFile.copyTo(cacheFile, overwrite = true)
            temporaryFile.delete()
        }
    }

    private fun readCachedWeather(): WeatherData? {
        if (!cacheFile.exists()) return null

        return runCatching {
            gson.fromJson(cacheFile.readText(), CachedWeather::class.java)
        }.getOrNull()?.let { cached ->
            WeatherData(
                weather = cached.weather,
                source = WeatherSource.CACHED,
                savedAtEpochMillis = cached.cachedAtEpochMillis
            )
        }
    }

    private fun readBundledWeather(): WeatherData {
        val weather = context.assets.open(OFFLINE_WEATHER_ASSET).bufferedReader().use { reader ->
            gson.fromJson(reader, WeatherResponse::class.java)
        }
        return WeatherData(
            weather = weather,
            source = WeatherSource.BUNDLED,
            savedAtEpochMillis = null
        )
    }

    private data class CachedWeather(
        val weather: WeatherResponse,
        val cachedAtEpochMillis: Long
    )

    private companion object {
        const val WEATHER_CACHE_FILE = "weather_cache.json"
        const val OFFLINE_WEATHER_ASSET = "offline_weather.json"
    }
}

data class WeatherData(
    val weather: WeatherResponse,
    val source: WeatherSource,
    val savedAtEpochMillis: Long?
)

enum class WeatherSource {
    LIVE,
    CACHED,
    BUNDLED
}
