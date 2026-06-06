package com.kalindu.pocketfit.utils

import com.kalindu.pocketfit.data.model.ProfileDetails

data class ProfileValidationResult(
    val details: ProfileDetails? = null,
    val message: String? = null
) {
    val isValid: Boolean
        get() = details != null
}

object ProfileValidation {
    fun validate(
        weight: String,
        height: String,
        age: String,
        fitnessGoal: String
    ): ProfileValidationResult {
        val weightValue = weight.toDoubleOrNull()
        if (weightValue == null || weightValue !in 20.0..300.0) {
            return ProfileValidationResult(
                message = "Weight must be between 20 and 300 kg."
            )
        }

        val heightValue = height.toIntOrNull()
        if (heightValue == null || heightValue !in 80..250) {
            return ProfileValidationResult(
                message = "Height must be between 80 and 250 cm."
            )
        }

        val ageValue = age.toIntOrNull()
        if (ageValue == null || ageValue !in 13..120) {
            return ProfileValidationResult(
                message = "Age must be between 13 and 120."
            )
        }

        return ProfileValidationResult(
            details = ProfileDetails(
                weightKg = weightValue,
                heightCm = heightValue,
                age = ageValue,
                fitnessGoal = fitnessGoal.trim()
            )
        )
    }
}
