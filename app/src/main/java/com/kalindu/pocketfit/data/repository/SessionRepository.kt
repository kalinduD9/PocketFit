package com.kalindu.pocketfit.data.repository

import com.kalindu.pocketfit.data.local.SessionDao
import com.kalindu.pocketfit.data.model.ActivitySession
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    fun allSessions(userId: String): Flow<List<ActivitySession>> =
        sessionDao.getAllSessions(userId)

    fun activeSession(userId: String): Flow<ActivitySession?> =
        sessionDao.getActiveSession(userId)

    suspend fun assignUnownedSessions(userId: String) =
        sessionDao.assignUnownedSessions(userId)

    suspend fun insert(session: ActivitySession): Long =
        sessionDao.insertSession(session)

    suspend fun update(session: ActivitySession) =
        sessionDao.updateSession(session)

    suspend fun delete(session: ActivitySession) =
        sessionDao.deleteSession(session)
}
