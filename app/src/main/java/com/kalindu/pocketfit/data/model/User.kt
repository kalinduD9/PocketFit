package com.kalindu.pocketfit.data.model

data class User(
    val name: String = "John Doe",
    val email: String = "john.doe@example.com",
    val weight: String = "70",
    val height: String = "175",
    val goal: String = "Maintenance",
    val dateOfBirth: String = "1995-01-01"
)
