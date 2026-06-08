package com.kalindu.pocketfit.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

// Handles all Firebase Authentication operations
class FirebaseAuthService(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    // Register a new user with email and password
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String
    ): AuthResult {
        return try {
            // Create user account with Firebase
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with their name
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdate)?.await()

            AuthResult(success = true, userId = result.user?.uid)
        } catch (e: Exception) {
            AuthResult(
                success = false,
                errorMessage = registrationErrorMessage(e)
            )
        }
    }

    // Login user with email and password
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult(success = true, userId = result.user?.uid)
        } catch (e: Exception) {
            AuthResult(
                success = false,
                errorMessage = loginErrorMessage(e)
            )
        }
    }

    // Sign out the current user
    fun signOut() {
        auth.signOut()
    }

    // Get the currently logged-in user
    fun getCurrentUser() = auth.currentUser

    // Check if user is logged in
    fun isUserLoggedIn() = auth.currentUser != null

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult(success = true, message = "Password reset email sent")
        } catch (e: Exception) {
            AuthResult(
                success = false,
                errorMessage = when ((e as? FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection."
                    else -> "Unable to send the reset email. Please try again."
                }
            )
        }
    }

    suspend fun updateDisplayName(name: String): AuthResult {
        val user = auth.currentUser
            ?: return AuthResult(success = false, errorMessage = "No signed-in user found")

        return try {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdate).await()
            AuthResult(success = true, message = "Name updated successfully")
        } catch (e: Exception) {
            AuthResult(
                success = false,
                errorMessage = when ((e as? FirebaseAuthException)?.errorCode) {
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection."
                    else -> "Unable to update your name. Please try again."
                }
            )
        }
    }

    private fun loginErrorMessage(error: Exception): String =
        when ((error as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND",
            "ERROR_INVALID_EMAIL" -> "Incorrect email or password."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection."
            else -> "Unable to log in. Please try again."
        }

    private fun registrationErrorMessage(error: Exception): String =
        when ((error as? FirebaseAuthException)?.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
            "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
            "ERROR_WEAK_PASSWORD" -> "Password must contain at least 6 characters."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection."
            else -> "Unable to create the account. Please try again."
        }
}

// Data class to hold authentication results
data class AuthResult(
    val success: Boolean = false,
    val userId: String? = null,
    val errorMessage: String? = null,
    val message: String? = null
)
