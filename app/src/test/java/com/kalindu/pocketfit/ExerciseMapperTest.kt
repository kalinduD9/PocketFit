package com.kalindu.pocketfit

import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.data.model.ExerciseInfoDto
import com.kalindu.pocketfit.data.model.ExerciseMuscleDto
import com.kalindu.pocketfit.data.model.ExerciseNamedDto
import com.kalindu.pocketfit.data.model.ExerciseTranslationDto
import com.kalindu.pocketfit.data.repository.ExerciseFallbackResolver
import com.kalindu.pocketfit.data.repository.ExerciseSource
import com.kalindu.pocketfit.utils.ExerciseMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseMapperTest {
    @Test
    fun apiExerciseIsNormalizedForMasterAndDetailUi() {
        val exercise = ExerciseMapper.fromApi(
            ExerciseInfoDto(
                id = 10,
                category = ExerciseNamedDto("Legs"),
                muscles = listOf(
                    ExerciseMuscleDto(name = "Quadriceps femoris", englishName = "Quads")
                ),
                secondaryMuscles = listOf(
                    ExerciseMuscleDto(name = "Gluteus maximus", englishName = "Glutes")
                ),
                equipment = listOf(ExerciseNamedDto("Bodyweight")),
                translations = listOf(
                    ExerciseTranslationDto(
                        name = "Slow Squat",
                        description = "<p>Lower slowly.</p><ol><li>Keep chest up.</li></ol>",
                        language = 2
                    )
                )
            )
        )

        requireNotNull(exercise)
        assertEquals("Slow Squat", exercise.name)
        assertEquals("Legs", exercise.category)
        assertEquals(listOf("Quads", "Glutes"), exercise.muscles)
        assertEquals(listOf("Bodyweight"), exercise.equipment)
        assertTrue(exercise.description.contains("Lower slowly."))
        assertTrue(exercise.description.contains("• Keep chest up."))
    }

    @Test
    fun descriptionSourceIsPreferredOverHtml() {
        val exercise = ExerciseMapper.fromApi(
            ExerciseInfoDto(
                id = 11,
                category = null,
                translations = listOf(
                    ExerciseTranslationDto(
                        name = "Step Jack",
                        description = "<p>HTML description</p>",
                        descriptionSource = "Plain instructions",
                        language = 2
                    )
                )
            )
        )

        assertEquals("Plain instructions", exercise?.description)
        assertEquals("General", exercise?.category)
    }

    @Test
    fun exerciseWithoutTranslationIsIgnored() {
        assertNull(
            ExerciseMapper.fromApi(
                ExerciseInfoDto(
                    id = 12,
                    category = ExerciseNamedDto("Cardio")
                )
            )
        )
    }

    @Test
    fun fallbackUsesCacheBeforeBundledData() {
        val cached = exercise(1, "Cached")
        val bundled = exercise(1, "Bundled")

        val result = ExerciseFallbackResolver.resolve(
            id = 1,
            cached = listOf(cached),
            bundled = listOf(bundled)
        )

        assertEquals("Cached", result?.exercise?.name)
        assertEquals(ExerciseSource.CACHED, result?.source)
    }

    @Test
    fun bundledFallbackHasCorrectSource() {
        val result = ExerciseFallbackResolver.resolve(
            id = -1,
            cached = listOf(exercise(1, "Cached")),
            bundled = listOf(exercise(-1, "Bundled"))
        )

        assertEquals("Bundled", result?.exercise?.name)
        assertEquals(ExerciseSource.BUNDLED, result?.source)
    }

    private fun exercise(id: Int, name: String) = Exercise(
        id = id,
        name = name,
        description = "Description",
        category = "General",
        muscles = emptyList(),
        equipment = emptyList()
    )
}
