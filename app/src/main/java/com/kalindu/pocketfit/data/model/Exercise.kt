package com.kalindu.pocketfit.data.model

data class Exercise(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val muscles: List<String>,
    val equipment: List<String>
)
