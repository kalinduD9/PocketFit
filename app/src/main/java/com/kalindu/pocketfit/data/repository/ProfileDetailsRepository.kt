package com.kalindu.pocketfit.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kalindu.pocketfit.data.model.ProfileDetails

class ProfileDetailsRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): ProfileDetails {
        val prefix = userPrefix()
        val weight = preferences.getFloat("$prefix.weight", MISSING_FLOAT)
            .takeUnless { it == MISSING_FLOAT }
            ?.toDouble()
        val height = preferences.getInt("$prefix.height", MISSING_INT)
            .takeUnless { it == MISSING_INT }
        val age = preferences.getInt("$prefix.age", MISSING_INT)
            .takeUnless { it == MISSING_INT }

        return ProfileDetails(
            weightKg = weight,
            heightCm = height,
            age = age,
            fitnessGoal = preferences.getString("$prefix.goal", "").orEmpty()
        )
    }

    fun save(details: ProfileDetails) {
        val prefix = userPrefix()
        preferences.edit()
            .putFloat("$prefix.weight", details.weightKg!!.toFloat())
            .putInt("$prefix.height", details.heightCm!!)
            .putInt("$prefix.age", details.age!!)
            .putString("$prefix.goal", details.fitnessGoal)
            .apply()
    }

    fun currentWeightKg(defaultWeightKg: Double = 70.0): Double =
        load().weightKg?.takeIf { it > 0.0 } ?: defaultWeightKg

    private fun userPrefix(): String =
        auth.currentUser?.uid ?: LOCAL_USER_KEY

    private companion object {
        const val PREFERENCES_NAME = "profile_details"
        const val LOCAL_USER_KEY = "signed_out"
        const val MISSING_FLOAT = -1f
        const val MISSING_INT = -1
    }
}
