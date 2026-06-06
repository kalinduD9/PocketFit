package com.kalindu.pocketfit.data.repository

import com.kalindu.pocketfit.data.local.SessionDao
import com.kalindu.pocketfit.data.model.ActivitySession
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<ActivitySession>> = sessionDao.getAllSessions()
    val activeSession: Flow<ActivitySession?> = sessionDao.getActiveSession()

    suspend fun insert(session: ActivitySession): Long =
        sessionDao.insertSession(session)

    suspend fun update(session: ActivitySession) =
        sessionDao.updateSession(session)

    suspend fun delete(session: ActivitySession) =
        sessionDao.deleteSession(session)
}
