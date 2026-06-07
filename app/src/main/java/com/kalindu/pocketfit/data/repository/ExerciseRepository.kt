package com.kalindu.pocketfit.data.repository

import android.content.Context
import com.google.gson.Gson
import com.kalindu.pocketfit.data.api.ExerciseJsonService
import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.utils.ExerciseJsonParser
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class ExerciseRepository(
    private val context: Context,
    private val jsonService: ExerciseJsonService = createJsonService()
) {
    private val gson = Gson()
    private val cacheFile = File(context.filesDir, CACHE_FILE)

    suspend fun loadExercises(): Result<ExerciseData> = withContext(Dispatchers.IO) {
        runCatching {
            val liveExercises = loadRemote()
            writeCache(liveExercises)
            ExerciseData(liveExercises, ExerciseSource.REMOTE)
        }.recoverCatching {
            ExerciseFallbackResolver.resolveList(
                cached = readCache(),
                bundled = readBundled()
            )
        }
    }

    suspend fun loadExercise(id: Int): Result<ExerciseDataItem> =
        withContext(Dispatchers.IO) {
            runCatching {
                val liveExercises = loadRemote()
                writeCache(liveExercises)
                val exercise = liveExercises.firstOrNull { it.id == id }
                    ?: error("Exercise details are unavailable.")
                ExerciseDataItem(exercise, ExerciseSource.REMOTE)
            }.recoverCatching {
                ExerciseFallbackResolver.resolve(
                    id = id,
                    cached = readCache(),
                    bundled = readBundled()
                ) ?: error("Exercise details are unavailable offline.")
            }
        }

    private suspend fun loadRemote(): List<Exercise> {
        val exercises = jsonService.getExercises().use { response ->
            ExerciseJsonParser.parseRemote(response.string())
        }
        require(exercises.isNotEmpty()) {
            "The external exercise JSON contained no usable data."
        }
        return exercises
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
        return ExerciseJsonParser.parseStored(cacheFile.readText())
            .takeIf(List<Exercise>::isNotEmpty)
    }

    private fun readBundled(): List<Exercise> =
        context.assets.open(OFFLINE_ASSET).bufferedReader().use { reader ->
            ExerciseJsonParser.parseStored(reader.readText())
        }

    companion object {
        private const val CACHE_FILE = "exercise_cache.json"
        private const val OFFLINE_ASSET = "offline_exercises.json"

        private fun createJsonService(): ExerciseJsonService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client(client)
                .build()
                .create(ExerciseJsonService::class.java)
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
    REMOTE,
    CACHED,
    BUNDLED
}

object ExerciseFallbackResolver {
    fun resolveList(
        cached: List<Exercise>?,
        bundled: List<Exercise>
    ): ExerciseData {
        cached?.takeIf(List<Exercise>::isNotEmpty)?.let {
            return ExerciseData(it, ExerciseSource.CACHED)
        }
        return ExerciseData(bundled, ExerciseSource.BUNDLED)
    }

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
