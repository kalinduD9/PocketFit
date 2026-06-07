package com.kalindu.pocketfit.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kalindu.pocketfit.data.api.ExerciseApiService
import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.utils.ExerciseMapper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExerciseRepository(
    private val context: Context,
    private val apiService: ExerciseApiService = createApiService()
) {
    private val gson = Gson()
    private val cacheFile = File(context.filesDir, CACHE_FILE)

    suspend fun loadExercises(): Result<ExerciseData> = withContext(Dispatchers.IO) {
        runCatching {
            val liveExercises = apiService.getExercises().results
                .mapNotNull(ExerciseMapper::fromApi)
            require(liveExercises.isNotEmpty()) { "The exercise service returned no usable data." }
            writeCache(liveExercises)
            ExerciseData(liveExercises, ExerciseSource.LIVE)
        }.recoverCatching {
            readCache()?.let { cached ->
                return@recoverCatching ExerciseData(cached, ExerciseSource.CACHED)
            }
            ExerciseData(readBundled(), ExerciseSource.BUNDLED)
        }
    }

    suspend fun loadExercise(id: Int): Result<ExerciseDataItem> =
        withContext(Dispatchers.IO) {
            runCatching {
                val exercise = ExerciseMapper.fromApi(apiService.getExercise(id))
                    ?: error("Exercise details are unavailable.")
                ExerciseDataItem(exercise, ExerciseSource.LIVE)
            }.recoverCatching {
                ExerciseFallbackResolver.resolve(
                    id = id,
                    cached = readCache(),
                    bundled = readBundled()
                ) ?: error("Exercise details are unavailable offline.")
            }
        }

    private fun writeCache(exercises: List<Exercise>) {
        val temporaryFile = File(context.filesDir, "$CACHE_FILE.tmp")
        temporaryFile.writeText(gson.toJson(exercises))
        if (!temporaryFile.renameTo(cacheFile)) {
            temporaryFile.copyTo(cacheFile, overwrite = true)
            temporaryFile.delete()
        }
    }

    private fun readCache(): List<Exercise>? {
        if (!cacheFile.exists()) return null
        return runCatching {
            gson.fromJson<List<Exercise>>(
                cacheFile.readText(),
                object : TypeToken<List<Exercise>>() {}.type
            )
        }.getOrNull()?.takeIf(List<Exercise>::isNotEmpty)
    }

    private fun readBundled(): List<Exercise> =
        context.assets.open(OFFLINE_ASSET).bufferedReader().use { reader ->
            gson.fromJson(
                reader,
                object : TypeToken<List<Exercise>>() {}.type
            )
        }

    companion object {
        private const val CACHE_FILE = "exercise_cache.json"
        private const val OFFLINE_ASSET = "offline_exercises.json"

        private fun createApiService(): ExerciseApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://wger.de/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ExerciseApiService::class.java)
        }
    }
}

data class ExerciseData(
    val exercises: List<Exercise>,
    val source: ExerciseSource
)

data class ExerciseDataItem(
    val exercise: Exercise,
    val source: ExerciseSource
)

enum class ExerciseSource {
    LIVE,
    CACHED,
    BUNDLED
}

object ExerciseFallbackResolver {
    fun resolve(
        id: Int,
        cached: List<Exercise>?,
        bundled: List<Exercise>
    ): ExerciseDataItem? {
        cached?.firstOrNull { it.id == id }?.let {
            return ExerciseDataItem(it, ExerciseSource.CACHED)
        }
        return bundled.firstOrNull { it.id == id }?.let {
            ExerciseDataItem(it, ExerciseSource.BUNDLED)
        }
    }
}
