package com.kalindu.pocketfit

import com.kalindu.pocketfit.data.model.ActivitySession
import com.kalindu.pocketfit.data.model.SessionCompletionReason
import com.kalindu.pocketfit.utils.SessionCalculations
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCalculationsTest {
    @Test
    fun validSessionAllowsNoOptionalGoals() {
        val result = SessionCalculations.validateInput(
            name = "Morning walk",
            durationMinutes = 30,
            stepGoal = null,
            calorieGoal = null
        )

        assertTrue(result.isValid)
        assertNull(result.message)
    }

    @Test
    fun validationRejectsBlankNameAndInvalidValues() {
        assertFalse(
            SessionCalculations.validateInput("", 30, null, null).isValid
        )
        assertFalse(
            SessionCalculations.validateInput("Session", 0, null, null).isValid
        )
        assertFalse(
            SessionCalculations.validateInput("Session", 30, -1, null).isValid
        )
        assertFalse(
            SessionCalculations.validateInput("Session", 30, null, 0).isValid
        )
    }

    @Test
    fun caloriesUseStepsAndDefaultWeight() {
        assertEquals(40, SessionCalculations.caloriesForSteps(1_000))
        assertEquals(0, SessionCalculations.caloriesForSteps(-100))
    }

    @Test
    fun reachingEitherGoalReturnsCompletionReason() {
        assertEquals(
            SessionCompletionReason.STEP_GOAL,
            SessionCalculations.reachedGoalReason(
                stepGoal = 1_000,
                calorieGoal = 100,
                steps = 1_000,
                calories = 40
            )
        )
        assertEquals(
            SessionCompletionReason.CALORIE_GOAL,
            SessionCalculations.reachedGoalReason(
                stepGoal = 5_000,
                calorieGoal = 40,
                steps = 1_000,
                calories = 40
            )
        )
        assertNull(
            SessionCalculations.reachedGoalReason(
                stepGoal = 5_000,
                calorieGoal = 100,
                steps = 1_000,
                calories = 40
            )
        )
    }

    @Test
    fun liveMetricsClampElapsedTimeToPlannedDuration() {
        val session = session(
            id = 1,
            startTimeMillis = 1_000L,
            plannedDurationMinutes = 1,
            steps = 250
        )

        val result = SessionCalculations.liveMetrics(session, 100_000L)

        assertEquals(60L, result.durationSeconds)
        assertEquals(250, result.steps)
        assertEquals(10, result.calories)
    }

    @Test
    fun sessionsForDayExcludeOtherDates() {
        val zone = ZoneId.of("UTC")
        val today = Instant.parse("2026-06-06T12:00:00Z").toEpochMilli()
        val todaySession = session(
            id = 1,
            startTimeMillis = Instant.parse("2026-06-06T08:00:00Z").toEpochMilli()
        )
        val previousSession = session(
            id = 2,
            startTimeMillis = Instant.parse("2026-06-05T23:59:59Z").toEpochMilli()
        )

        val result = SessionCalculations.sessionsForDay(
            listOf(todaySession, previousSession),
            today,
            zone
        )

        assertEquals(listOf(1), result.map { it.id })
    }

    private fun session(
        id: Int,
        startTimeMillis: Long,
        plannedDurationMinutes: Int = 30,
        steps: Int = 0
    ) = ActivitySession(
        id = id,
        name = "Session $id",
        plannedDurationMinutes = plannedDurationMinutes,
        startTimeMillis = startTimeMillis,
        steps = steps
    )
}
