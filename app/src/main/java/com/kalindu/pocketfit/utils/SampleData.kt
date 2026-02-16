package com.kalindu.pocketfit.utils

import com.kalindu.pocketfit.data.model.Activity
import com.kalindu.pocketfit.data.model.User

object SampleData {
    val sampleUser = User(
        name = "Kalindu Dulshan",
        email = "kalindu@example.com",
        weight = "70",
        height = "175",
        goal = "Maintenance",
        age = 21
    )

    val sampleActivities = listOf(
        Activity(
            id = 1,
            type = "Walking",
            duration = "55 Min",
            steps = 6050,
            calories = 260,
            distance = "4.2 km",
            pace = "7:30 /km",
            date = "January 15, 2025",
            time = "08:30 AM"
        ),
        Activity(
            id = 2,
            type = "Running",
            duration = "35 Min",
            steps = 5600,
            calories = 380,
            distance = "5.5 km",
            pace = "6:22 /km",
            date = "January 15, 2025",
            time = "06:00 AM"
        ),
        Activity(
            id = 3,
            type = "Cycling",
            duration = "45 Min",
            steps = 0,
            calories = 420,
            distance = "12.0 km",
            pace = "3:45 /km",
            date = "January 14, 2025",
            time = "07:00 PM"
        ),
        Activity(
            id = 4,
            type = "Walking",
            duration = "30 Min",
            steps = 3500,
            calories = 150,
            distance = "2.5 km",
            pace = "8:00 /km",
            date = "January 14, 2025",
            time = "12:00 PM"
        ),
        Activity(
            id = 5,
            type = "Running",
            duration = "40 Min",
            steps = 6200,
            calories = 450,
            distance = "6.2 km",
            pace = "6:27 /km",
            date = "January 13, 2025",
            time = "06:30 AM"
        )
    )

    val todaySteps = 3000
    val todayCalories = 250
    val currentActivity = "Walking"
}