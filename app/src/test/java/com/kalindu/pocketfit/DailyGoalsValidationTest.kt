package com.kalindu.pocketfit

import com.kalindu.pocketfit.data.model.DailyGoals
import com.kalindu.pocketfit.utils.DailyGoalsValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyGoalsValidationTest {
    @Test
    fun defaultsAreAssignmentFriendly() {
        val goals = DailyGoals()

        assertEquals(10_000, goals.stepGoal)
        assertEquals(500, goals.calorieGoal)
    }

    @Test
    fun positiveWholeNumberGoalsAreAccepted() {
        val result = DailyGoalsValidation.validate("8000", "400")

        assertTrue(result.isValid)
        assertEquals(8_000, result.goals?.stepGoal)
        assertEquals(400, result.goals?.calorieGoal)
    }

    @Test
    fun invalidGoalsAreRejected() {
        assertFalse(DailyGoalsValidation.validate("", "500").isValid)
        assertFalse(DailyGoalsValidation.validate("0", "500").isValid)
        assertFalse(DailyGoalsValidation.validate("10000", "-1").isValid)
        assertFalse(DailyGoalsValidation.validate("10000", "abc").isValid)
    }

    @Test
    fun progressIsClampedToValidRange() {
        assertEquals(0f, DailyGoalsValidation.progress(-10, 100), 0f)
        assertEquals(0.5f, DailyGoalsValidation.progress(50, 100), 0f)
        assertEquals(1f, DailyGoalsValidation.progress(150, 100), 0f)
        assertEquals(0f, DailyGoalsValidation.progress(50, 0), 0f)
    }
}
