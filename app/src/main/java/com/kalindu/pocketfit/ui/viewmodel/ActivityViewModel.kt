package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kalindu.pocketfit.data.local.AppDatabase
import com.kalindu.pocketfit.data.model.Activity
import com.kalindu.pocketfit.data.repository.ActivityRepository
import com.kalindu.pocketfit.utils.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ActivityRepository
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    init {
        val activityDao = AppDatabase.getDatabase(application).activityDao()
        repository = ActivityRepository(activityDao)
        
        viewModelScope.launch {
            repository.allActivities.collectLatest { list ->
                if (list.isEmpty()) {
                    // Pre-populate with sample data on first run
                    SampleData.sampleActivities.forEach {
                        repository.insert(it.copy(id = 0)) // auto-generate ID
                    }
                } else {
                    _activities.value = list
                }
            }
        }
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insert(activity)
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.delete(activity)
        }
    }
}
