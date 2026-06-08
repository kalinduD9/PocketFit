package com.kalindu.pocketfit.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object SessionStatus {
    const val ACTIVE = "ACTIVE"
    const val COMPLETED = "COMPLETED"
}

object SessionCompletionReason {
    const val MANUAL = "MANUAL"
    const val DURATION = "DURATION"
    const val STEP_GOAL = "STEP_GOAL"
    const val CALORIE_GOAL = "CALORIE_GOAL"
}

@Entity(tableName = "sessions")
data class ActivitySession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(defaultValue = "''")
    val userId: String = "",
    val name: String,
    val plannedDurationMinutes: Int,
    val stepGoal: Int? = null,
    val calorieGoal: Int? = null,
    val weightUsedKg: Double = 70.0,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val status: String = SessionStatus.ACTIVE,
    val completionReason: String? = null,
    val stepBaseline: Int? = null,
    val steps: Int = 0,
    val calories: Int = 0,
    val actualDurationSeconds: Long = 0
)
