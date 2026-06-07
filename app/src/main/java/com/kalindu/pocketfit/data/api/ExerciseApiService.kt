package com.kalindu.pocketfit.data.api

import com.kalindu.pocketfit.data.model.ExerciseInfoDto
import com.kalindu.pocketfit.data.model.ExerciseInfoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20
    ): ExerciseInfoResponse

    @GET("exerciseinfo/{id}/")
    suspend fun getExercise(
        @Path("id") id: Int,
        @Query("language") language: Int = 2
    ): ExerciseInfoDto
}
