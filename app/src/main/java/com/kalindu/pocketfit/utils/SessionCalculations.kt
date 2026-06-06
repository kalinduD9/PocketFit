package com.kalindu.pocketfit.utils

import com.kalindu.pocketfit.data.model.ActivitySession
import com.kalindu.pocketfit.data.model.SessionCompletionReason
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class SessionMetrics(
    val durationSeconds: Long,
    val steps: Int,
    val calories: Int
)

data class SessionInputValidation(
    val isValid: Boolean,
    val message: String? = null
)

object SessionCalculations {
    private const val DEFAULT_WEIGHT_KG = 70.0
    private const val CALORIES_PER_STEP = 0.04

    fun validateInput(
        name: String,
        durationMinutes: Int?,
        stepGoal: Int?,
        calorieGoal: Int?
    ): SessionInputValidation {
        if (name.isBlank()) {
            return SessionInputValidation(false, "Session name is required.")
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            return SessionInputValidation(false, "Duration must be a positive whole number.")
        }
        if (stepGoal != null && stepGoal <= 0) {
            return SessionInputValidation(false, "Step goal must be positive.")
        }
        if (calorieGoal != null && calorieGoal <= 0) {
            return SessionInputValidation(false, "Calorie goal must be positive.")
        }
        return SessionInputValidation(true)
    }

    fun caloriesForSteps(
        steps: Int,
        weightKg: Double = DEFAULT_WEIGHT_KG
    ): Int {
        val safeSteps = steps.coerceAtLeast(0)
        val safeWeight = weightKg.takeIf { it > 0.0 } ?: DEFAULT_WEIGHT_KG
        return (safeSteps * CALORIES_PER_STEP * (safeWeight / DEFAULT_WEIGHT_KG))
            .roundToInt()
            .coerceAtLeast(0)
    }

    fun liveMetrics(session: ActivitySession, nowMillis: Long): SessionMetrics {
        val plannedSeconds = session.plannedDurationMinutes * 60L
        val elapsedSeconds = ((nowMillis - session.startTimeMillis) / 1_000L)
            .coerceIn(0L, plannedSeconds)
        return SessionMetrics(
            durationSeconds = elapsedSeconds,
            steps = session.steps.coerceAtLeast(0),
            calories = caloriesForSteps(session.steps)
        )
    }

    fun reachedGoalReason(
        stepGoal: Int?,
        calorieGoal: Int?,
        steps: Int,
        calories: Int
    ): String? = when {
        stepGoal != null && steps >= stepGoal -> SessionCompletionReason.STEP_GOAL
        calorieGoal != null && calories >= calorieGoal ->
            SessionCompletionReason.CALORIE_GOAL
        else -> null
    }

    fun dayBounds(
        timeMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val date = Instant.ofEpochMilli(timeMillis).atZone(zoneId).toLocalDate()
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to end
    }

    fun sessionsForDay(
        sessions: List<ActivitySession>,
        timeMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<ActivitySession> {
        val (start, end) = dayBounds(timeMillis, zoneId)
        return sessions.filter { it.startTimeMillis in start until end }
    }

    fun formatDuration(seconds: Long): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        return "%d:%02d".format(safeSeconds / 60, safeSeconds % 60)
    }

    fun formatDate(timeMillis: Long): String =
        DATE_FORMATTER.format(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()))

    fun formatTime(timeMillis: Long): String =
        TIME_FORMATTER.format(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()))

    fun completionReasonLabel(reason: String?): String = when (reason) {
        SessionCompletionReason.MANUAL -> "Finished manually"
        SessionCompletionReason.DURATION -> "Duration completed"
        SessionCompletionReason.STEP_GOAL -> "Step goal reached"
        SessionCompletionReason.CALORIE_GOAL -> "Calorie goal reached"
        else -> "In progress"
    }

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a")
}
