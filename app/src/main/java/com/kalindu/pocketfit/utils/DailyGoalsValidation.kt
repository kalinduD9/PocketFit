package com.kalindu.pocketfit.utils

import com.kalindu.pocketfit.data.model.DailyGoals

data class DailyGoalsValidationResult(
    val goals: DailyGoals? = null,
    val message: String? = null
) {
    val isValid: Boolean
        get() = goals != null
}

object DailyGoalsValidation {
    fun validate(
        stepGoal: String,
        calorieGoal: String
    ): DailyGoalsValidationResult {
        val stepValue = stepGoal.toIntOrNull()
        if (stepValue == null || stepValue <= 0) {
            return DailyGoalsValidationResult(
                message = "Step goal must be a positive whole number."
            )
        }

        val calorieValue = calorieGoal.toIntOrNull()
        if (calorieValue == null || calorieValue <= 0) {
            return DailyGoalsValidationResult(
                message = "Calorie goal must be a positive whole number."
            )
        }

        return DailyGoalsValidationResult(
            goals = DailyGoals(
                stepGoal = stepValue,
                calorieGoal = calorieValue
            )
        )
    }

    fun progress(current: Int, goal: Int): Float =
        if (goal <= 0) 0f else (current.toFloat() / goal).coerceIn(0f, 1f)
}
