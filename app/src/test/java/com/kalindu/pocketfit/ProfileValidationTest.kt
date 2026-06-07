package com.kalindu.pocketfit

import com.kalindu.pocketfit.utils.ProfileValidation
import com.kalindu.pocketfit.utils.SessionCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidationTest {
    @Test
    fun validProfileDetailsAreParsed() {
        val result = ProfileValidation.validate(
            weight = "84.5",
            height = "180",
            age = "24"
        )

        assertTrue(result.isValid)
        assertEquals(84.5, result.details?.weightKg ?: 0.0, 0.001)
        assertEquals(180, result.details?.heightCm)
        assertEquals(24, result.details?.age)
    }

    @Test
    fun invalidNumericRangesAreRejected() {
        assertFalse(ProfileValidation.validate("0", "170", "21").isValid)
        assertFalse(ProfileValidation.validate("70", "40", "21").isValid)
        assertFalse(ProfileValidation.validate("70", "170", "5").isValid)
        assertFalse(ProfileValidation.validate("abc", "170", "21").isValid)
    }

    @Test
    fun calorieEstimateUsesSessionWeight() {
        assertEquals(40, SessionCalculations.caloriesForSteps(1_000, 70.0))
        assertEquals(48, SessionCalculations.caloriesForSteps(1_000, 84.0))
    }
}
