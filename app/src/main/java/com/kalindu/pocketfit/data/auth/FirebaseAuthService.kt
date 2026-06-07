package com.kalindu.pocketfit.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

// Handles all Firebase Authentication operations
class FirebaseAuthService(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    // Register a new user with email and passworde
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
                errorMessage = when {
                    e.message?.contains("already in use") == true ->
                        "This email is already registered"
                    e.message?.contains("weak") == true ->
                        "Password must be at least 6 characters"
                    e.message?.contains("invalid email") == true ->
                        "Please enter a valid email address"
                    else -> e.message ?: "Registration failed"
                }
            )
        }
    }

    // Login user with email and passworde
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
                errorMessage = when {
                    e.message?.contains("no user") == true ->
                        "Email not found. Please register first."
                    e.message?.contains("password") == true ->
                        "Incorrect email or password"
                    else -> e.message ?: "Login failed"
                }
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
                errorMessage = e.message ?: "Failed to send reset email"
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
                errorMessage = e.message ?: "Failed to update name"
            )
        }
    }
}

// Data class to hold authentication results
data class AuthResult(
    val success: Boolean = false,
    val userId: String? = null,
    val errorMessage: String? = null,
    val message: String? = null
)
