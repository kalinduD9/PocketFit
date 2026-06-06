package com.kalindu.pocketfit.data.model

data class DailyGoals(
    val stepGoal: Int = DEFAULT_STEP_GOAL,
    val calorieGoal: Int = DEFAULT_CALORIE_GOAL
) {
    companion object {
        const val DEFAULT_STEP_GOAL = 10_000
        const val DEFAULT_CALORIE_GOAL = 500
    }
}
