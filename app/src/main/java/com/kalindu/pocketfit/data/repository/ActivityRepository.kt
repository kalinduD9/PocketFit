package com.kalindu.pocketfit.data.repository

import com.kalindu.pocketfit.data.local.ActivityDao
import com.kalindu.pocketfit.data.model.Activity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val activityDao: ActivityDao) {
    val allActivities: Flow<List<Activity>> = activityDao.getAllActivities()

    suspend fun insert(activity: Activity) {
        activityDao.insertActivity(activity)
    }

    suspend fun delete(activity: Activity) {
        activityDao.deleteActivity(activity)
    }
}
