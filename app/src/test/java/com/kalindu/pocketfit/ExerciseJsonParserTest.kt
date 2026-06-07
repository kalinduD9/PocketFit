package com.kalindu.pocketfit

import com.kalindu.pocketfit.data.model.Exercise
import com.kalindu.pocketfit.data.repository.ExerciseFallbackResolver
import com.kalindu.pocketfit.data.repository.ExerciseSource
import com.kalindu.pocketfit.utils.ExerciseJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseJsonParserTest {
    @Test
    fun validRemoteJsonIsParsedForMasterAndDetail() {
        val exercises = ExerciseJsonParser.parseRemote(
            """
            [
              {
                "id": 1,
                "name": "Bodyweight Squat",
                "description": "Lower with control.",
                "category": "Legs",
                "muscles": ["Quads", "Glutes"],
                "equipment": ["Bodyweight"]
              }
            ]
            """.trimIndent()
        )

        assertEquals(1, exercises.size)
        assertEquals("Bodyweight Squat", exercises.single().name)
        assertEquals(listOf("Quads", "Glutes"), exercises.single().muscles)
    }

    @Test
    fun malformedRemoteEntriesAreIgnoredAndDefaultsAreApplied() {
        val exercises = ExerciseJsonParser.parseRemote(
            """
            [
              {"id": -1, "name": "Negative ID"},
              {"id": "not a number", "name": "Invalid ID"},
              {"id": 2, "name": ""},
              {"name": "Missing ID"},
              {"id": 3, "name": "Valid Exercise"},
              {"id": 3, "name": "Duplicate ID"}
            ]
            """.trimIndent()
        )

        assertEquals(1, exercises.size)
        assertEquals("Valid Exercise", exercises.single().name)
        assertEquals("General", exercises.single().category)
        assertTrue(exercises.single().description.isNotBlank())
    }

    @Test
    fun invalidJsonProducesAnEmptyList() {
        assertTrue(ExerciseJsonParser.parseRemote("not json").isEmpty())
    }

    @Test
    fun fallbackUsesCacheBeforeBundledData() {
        val result = ExerciseFallbackResolver.resolve(
            id = 1,
            cached = listOf(exercise(1, "Cached")),
            bundled = listOf(exercise(1, "Bundled"))
        )

        assertEquals("Cached", result?.exercise?.name)
        assertEquals(ExerciseSource.CACHED, result?.source)
    }

    @Test
    fun listFallbackUsesCacheBeforeBundledData() {
        val result = ExerciseFallbackResolver.resolveList(
            cached = listOf(exercise(1, "Cached")),
            bundled = listOf(exercise(-1, "Bundled"))
        )

        assertEquals("Cached", result.exercises.single().name)
        assertEquals(ExerciseSource.CACHED, result.source)
    }

    @Test
    fun listFallbackUsesBundledDataWithoutCache() {
        val result = ExerciseFallbackResolver.resolveList(
            cached = null,
            bundled = listOf(exercise(-1, "Bundled"))
        )

        assertEquals("Bundled", result.exercises.single().name)
        assertEquals(ExerciseSource.BUNDLED, result.source)
    }

    @Test
    fun fallbackUsesBundledDataWhenCacheDoesNotContainTheExercise() {
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
