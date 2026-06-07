package com.kalindu.pocketfit.data.model

import com.google.gson.annotations.SerializedName

data class ExerciseInfoResponse(
    val results: List<ExerciseInfoDto>
)

data class ExerciseInfoDto(
    val id: Int,
    val category: ExerciseNamedDto?,
    val muscles: List<ExerciseMuscleDto> = emptyList(),
    @SerializedName("muscles_secondary")
    val secondaryMuscles: List<ExerciseMuscleDto> = emptyList(),
    val equipment: List<ExerciseNamedDto> = emptyList(),
    val translations: List<ExerciseTranslationDto> = emptyList()
)

data class ExerciseNamedDto(
    val name: String = ""
)

data class ExerciseMuscleDto(
    val name: String = "",
    @SerializedName("name_en")
    val englishName: String = ""
)

data class ExerciseTranslationDto(
    val name: String = "",
    val description: String = "",
    @SerializedName("description_source")
    val descriptionSource: String = "",
    val language: Int = 0
)
