package com.kalindu.pocketfit.data.model

data class Activity(
    val id: Int,
    val type: String,
    val duration: String,
    val steps: Int,
    val calories: Int,
    val distance: String,
    val pace: String,
    val date: String,
    val time: String
)
