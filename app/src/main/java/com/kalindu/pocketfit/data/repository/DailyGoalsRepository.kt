package com.kalindu.pocketfit.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kalindu.pocketfit.data.model.DailyGoals

class DailyGoalsRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): DailyGoals {
        val prefix = userPrefix()
        return DailyGoals(
            stepGoal = preferences.getInt(
                "$prefix.step_goal",
                DailyGoals.DEFAULT_STEP_GOAL
            ),
            calorieGoal = preferences.getInt(
                "$prefix.calorie_goal",
                DailyGoals.DEFAULT_CALORIE_GOAL
            )
        )
    }

    fun save(goals: DailyGoals) {
        val prefix = userPrefix()
        preferences.edit()
            .putInt("$prefix.step_goal", goals.stepGoal)
            .putInt("$prefix.calorie_goal", goals.calorieGoal)
            .apply()
    }

    private fun userPrefix(): String =
        auth.currentUser?.uid ?: LOCAL_USER_KEY

    private companion object {
        const val PREFERENCES_NAME = "daily_goals"
        const val LOCAL_USER_KEY = "signed_out"
    }
}
