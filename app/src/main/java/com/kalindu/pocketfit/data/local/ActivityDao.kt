package com.kalindu.pocketfit.data.local

import androidx.room.*
import com.kalindu.pocketfit.data.model.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY id DESC")
    fun getAllActivities(): Flow<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
}
