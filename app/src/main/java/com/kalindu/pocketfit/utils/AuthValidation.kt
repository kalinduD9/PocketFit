package com.kalindu.pocketfit.utils

object AuthValidation {
    private val emailPattern =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Full name is required."
        name.trim().length < 2 -> "Name must contain at least 2 characters."
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required."
        !emailPattern.matches(email.trim()) -> "Enter a valid email address."
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Password is required."
        password.length < 6 -> "Password must contain at least 6 characters."
        else -> null
    }

    fun confirmPasswordError(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirm your password."
        password != confirmation -> "Passwords do not match."
        else -> null
    }
}
