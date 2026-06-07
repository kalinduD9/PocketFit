package com.kalindu.pocketfit.utils

import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.data.model.ExerciseInfoDto

object ExerciseMapper {
    fun fromApi(dto: ExerciseInfoDto): Exercise? {
        val translation = dto.translations.firstOrNull { it.language == ENGLISH_LANGUAGE_ID }
            ?: dto.translations.firstOrNull()
            ?: return null
        val name = translation.name.trim()
        if (name.isBlank()) return null

        val descriptionText = translation.descriptionSource
            .ifBlank { stripHtml(translation.description) }
            .trim()

        val muscles = (dto.muscles + dto.secondaryMuscles)
            .map { it.englishName.ifBlank { it.name }.trim() }
            .filter(String::isNotBlank)
            .distinct()

        return Exercise(
            id = dto.id,
            name = name,
            description = descriptionText.ifBlank {
                "No instructions are available for this exercise."
            },
            category = dto.category?.name?.trim().orEmpty().ifBlank { "General" },
            muscles = muscles,
            equipment = dto.equipment
                .map { it.name.trim() }
                .filter(String::isNotBlank)
                .distinct()
        )
    }

    fun stripHtml(value: String): String = value
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "\n• ")
        .replace(Regex("</(p|li|ol|ul|div|br)>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex("\\n\\s*\\n+"), "\n")
        .trim()

    private const val ENGLISH_LANGUAGE_ID = 2
}
