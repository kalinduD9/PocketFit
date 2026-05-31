package com.kalindu.pocketfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val duration: String,
    val steps: Int,
    val calories: Int,
    val distance: String,
    val pace: String,
    val date: String,
    val time: String
)
