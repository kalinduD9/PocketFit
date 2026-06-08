package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kalindu.pocketfit.data.local.AppDatabase
import com.kalindu.pocketfit.data.model.ActivitySession
import com.kalindu.pocketfit.data.model.SessionCompletionReason
import com.kalindu.pocketfit.data.model.SessionStatus
import com.kalindu.pocketfit.data.repository.SessionRepository
import com.kalindu.pocketfit.data.repository.ProfileDetailsRepository
import com.kalindu.pocketfit.utils.SessionCalculations
import com.kalindu.pocketfit.utils.StepSensorManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SessionSensorState {
    IDLE,
    TRACKING,
    UNAVAILABLE,
    PERMISSION_DENIED
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val repository = SessionRepository(
        AppDatabase.getDatabase(application).sessionDao()
    )
    private val profileDetailsRepository =
        ProfileDetailsRepository(application.applicationContext)
    private val stepSensorManager = StepSensorManager(application.applicationContext)
    private val currentUserId = MutableStateFlow(auth.currentUser?.uid.orEmpty())

    val sessions: StateFlow<List<ActivitySession>> = currentUserId.flatMapLatest {
        repository.allSessions(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val activeSession: StateFlow<ActivitySession?> = currentUserId.flatMapLatest {
        repository.activeSession(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

    private val _sensorState = MutableStateFlow(SessionSensorState.IDLE)
    val sensorState: StateFlow<SessionSensorState> = _sensorState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val todaySessions: StateFlow<List<ActivitySession>> = combine(
        sessions,
        nowMillis
    ) { allSessions, now ->
        SessionCalculations.sessionsForDay(allSessions, now)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val historicalSessions: StateFlow<List<ActivitySession>> = combine(
        sessions,
        nowMillis
    ) { allSessions, now ->
        val todayStart = SessionCalculations.dayBounds(now).first
        allSessions.filter {
            it.status == SessionStatus.COMPLETED &&
                it.startTimeMillis < todayStart
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private var sensorBaseline: Int? = null
    private var trackedSessionId: Int? = null
    private var latestSessionSteps = 0
    private var creatingSession = false
    private var completingSession = false
    private val rawStepReadings = Channel<Int>(Channel.CONFLATED)

    init {
        assignExistingSessions()

        viewModelScope.launch {
            activeSession.collect { session ->
                sensorBaseline = session?.stepBaseline
                if (trackedSessionId != session?.id) {
                    trackedSessionId = session?.id
                    latestSessionSteps = session?.steps ?: 0
                }
                if (session == null) {
                    stopStepTracking()
                } else {
                    completeIfExpired(session)
                }
            }
        }

        viewModelScope.launch {
            for (rawSteps in rawStepReadings) {
                processRawStepReading(rawSteps)
            }
        }

        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _nowMillis.value = now
                activeSession.value?.let(::completeIfExpired)
                delay(1_000)
            }
        }
    }

    fun createSession(
        name: String,
        durationMinutes: Int?,
        stepGoal: Int?,
        calorieGoal: Int?
    ): Boolean {
        val userId = currentUserId.value
        if (userId.isBlank()) {
            _message.value = "Sign in before starting a session."
            return false
        }
        val validation = SessionCalculations.validateInput(
            name,
            durationMinutes,
            stepGoal,
            calorieGoal
        )
        if (!validation.isValid) {
            _message.value = validation.message
            return false
        }
        if (activeSession.value != null || creatingSession) {
            reportActiveSessionConflict()
            return false
        }

        creatingSession = true
        viewModelScope.launch {
            try {
                repository.insert(
                    ActivitySession(
                        userId = userId,
                        name = name.trim(),
                        plannedDurationMinutes = requireNotNull(durationMinutes),
                        stepGoal = stepGoal,
                        calorieGoal = calorieGoal,
                        weightUsedKg = profileDetailsRepository.currentWeightKg(),
                        startTimeMillis = System.currentTimeMillis()
                    )
                )
            } finally {
                creatingSession = false
            }
        }
        return true
    }

    fun refreshUser() {
        currentUserId.value = auth.currentUser?.uid.orEmpty()
        assignExistingSessions()
    }

    fun startStepTracking() {
        if (activeSession.value == null) {
            stopStepTracking()
            return
        }

        val started = stepSensorManager.startListening(::handleRawStepReading)
        _sensorState.value =
            if (started) SessionSensorState.TRACKING else SessionSensorState.UNAVAILABLE
    }

    fun reportStepPermissionDenied() {
        stopStepTracking()
        _sensorState.value = SessionSensorState.PERMISSION_DENIED
    }

    fun stopStepTracking() {
        stepSensorManager.stopListening()
        if (_sensorState.value == SessionSensorState.TRACKING) {
            _sensorState.value = SessionSensorState.IDLE
        }
    }

    fun finishActiveSession() {
        activeSession.value?.let {
            finishSession(
                session = it,
                endTimeMillis = System.currentTimeMillis(),
                reason = SessionCompletionReason.MANUAL
            )
        }
    }

    fun deleteSession(session: ActivitySession) {
        if (session.status == SessionStatus.ACTIVE) {
            _message.value = "Finish the active session before deleting it."
            return
        }
        viewModelScope.launch { repository.delete(session) }
    }

    fun reportActiveSessionConflict() {
        _message.value = "Finish the current session before starting another one."
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun handleRawStepReading(rawSteps: Int) {
        rawStepReadings.trySend(rawSteps)
    }

    private suspend fun processRawStepReading(rawSteps: Int) {
        val session = activeSession.value ?: return
        val baseline = sensorBaseline ?: rawSteps.also { firstReading ->
            sensorBaseline = firstReading
        }
        val measuredSteps = (rawSteps - baseline).coerceAtLeast(0)
        val sessionSteps = SessionCalculations.recordedSteps(
            sensorSteps = measuredSteps,
            currentSteps = latestSessionSteps,
            stepGoal = session.stepGoal
        )
        val calories = SessionCalculations.caloriesForSteps(
            sessionSteps,
            session.weightUsedKg
        )
        val updatedSession = session.copy(
            stepBaseline = baseline,
            steps = sessionSteps,
            calories = calories
        )
        latestSessionSteps = sessionSteps
        val goalReason = SessionCalculations.reachedGoalReason(
            stepGoal = session.stepGoal,
            calorieGoal = session.calorieGoal,
            steps = sessionSteps,
            calories = calories
        )

        if (goalReason != null) {
            finishSession(
                session = updatedSession,
                endTimeMillis = System.currentTimeMillis(),
                reason = goalReason
            )
        } else if (sessionSteps != session.steps || calories != session.calories) {
            repository.update(updatedSession)
        } else if (session.stepBaseline == null) {
            repository.update(updatedSession)
        }
    }

    private fun assignExistingSessions() {
        val userId = currentUserId.value
        if (userId.isBlank()) return
        viewModelScope.launch {
            repository.assignUnownedSessions(userId)
        }
    }

    private fun completeIfExpired(session: ActivitySession) {
        val plannedEnd =
            session.startTimeMillis + session.plannedDurationMinutes * 60_000L
        if (System.currentTimeMillis() >= plannedEnd) {
            finishSession(
                session = session,
                endTimeMillis = plannedEnd,
                reason = SessionCompletionReason.DURATION
            )
        }
    }

    private fun finishSession(
        session: ActivitySession,
        endTimeMillis: Long,
        reason: String
    ) {
        if (session.status != SessionStatus.ACTIVE || completingSession) return
        completingSession = true
        stopStepTracking()

        viewModelScope.launch {
            try {
                val plannedEnd =
                    session.startTimeMillis + session.plannedDurationMinutes * 60_000L
                val finalEndTime = endTimeMillis.coerceIn(
                    session.startTimeMillis,
                    plannedEnd
                )
                val durationSeconds =
                    ((finalEndTime - session.startTimeMillis) / 1_000L).coerceAtLeast(0)
                repository.update(
                    session.copy(
                        endTimeMillis = finalEndTime,
                        status = SessionStatus.COMPLETED,
                        completionReason = reason,
                        steps = session.steps.coerceAtLeast(0),
                        calories = SessionCalculations.caloriesForSteps(
                            session.steps,
                            session.weightUsedKg
                        ),
                        actualDurationSeconds = durationSeconds
                    )
                )
            } finally {
                completingSession = false
            }
        }
    }

    override fun onCleared() {
        stopStepTracking()
        super.onCleared()
    }
}
