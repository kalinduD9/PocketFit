package com.kalindu.pocketfit.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface ExerciseJsonService {
    @GET("kalinduD9/PocketFit/main/data/exercises.json")
    suspend fun getExercises(): ResponseBody
}
