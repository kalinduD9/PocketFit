package com.kalindu.pocketfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kalindu.pocketfit.data.model.ActivitySession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<ActivitySession>>

    @Query(
        "SELECT * FROM sessions " +
            "WHERE status = 'ACTIVE' ORDER BY startTimeMillis DESC LIMIT 1"
    )
    fun getActiveSession(): Flow<ActivitySession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ActivitySession): Long

    @Update
    suspend fun updateSession(session: ActivitySession)

    @Delete
    suspend fun deleteSession(session: ActivitySession)
}
