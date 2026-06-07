package com.kalindu.pocketfit.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.auth.FirebaseAuthService
import kotlinx.coroutines.launch

// ViewModel that handles all authentication-related state and logic
// This manages login/register/logout operations and keeps track of auth state
class AuthViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    // Private mutable state - only this ViewModel can modify it
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)

    // Public immutable state - UI can read but not modify
    val authState: State<AuthState> = _authState

    private val _nameUpdateState = mutableStateOf<NameUpdateState>(NameUpdateState.Idle)
    val nameUpdateState: State<NameUpdateState> = _nameUpdateState

    private val _currentUserName = mutableStateOf(
        authService.getCurrentUser()?.displayName ?: "User"
    )

    // Handle user registration
    // @param email User's email
    // @param password User's password
    // @param name User's full name
    fun register(email: String, password: String, name: String) {
        // Show loading state
        _authState.value = AuthState.Loading

        // Launch coroutine to handle async Firebase operation
        viewModelScope.launch {
            val result = authService.registerWithEmail(email, password, name)

            _authState.value = if (result.success) {
                _currentUserName.value =
                    authService.getCurrentUser()?.displayName ?: name.trim()
                AuthState.Authenticated("Registration successful! Welcome to PocketFit")
            } else {
                AuthState.Error(result.errorMessage ?: "Registration failed")
            }
        }
    }

    // Handle user login
    // @param email User's email
    // @param password User's password
    fun login(email: String, password: String) {
        // Show loading state
        _authState.value = AuthState.Loading

        // Launch coroutine to handle async Firebase operation
        viewModelScope.launch {
            val result = authService.loginWithEmail(email, password)

            _authState.value = if (result.success) {
                _currentUserName.value =
                    authService.getCurrentUser()?.displayName ?: "User"
                AuthState.Authenticated("Login successful!")
            } else {
                AuthState.Error(result.errorMessage ?: "Login failed")
            }
        }
    }

    // Handle user logout
    fun logout() {
        authService.signOut()
        _currentUserName.value = "User"
        _nameUpdateState.value = NameUpdateState.Idle
        _authState.value = AuthState.Idle
    }

    // Check if user is already logged in
    // @return true if user is logged in
    fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    // Send password reset email
    // @param email User's email
    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authService.sendPasswordResetEmail(email)

            _authState.value = if (result.success) {
                AuthState.Success(result.message ?: "Password reset email sent")
            } else {
                AuthState.Error(result.errorMessage ?: "Failed to send reset email")
            }
        }
    }

    fun updateName(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.length < 2) {
            _nameUpdateState.value =
                NameUpdateState.Error("Name must contain at least 2 characters.")
            return
        }

        _nameUpdateState.value = NameUpdateState.Loading
        viewModelScope.launch {
            val result = authService.updateDisplayName(trimmedName)
            if (result.success) {
                _currentUserName.value = trimmedName
                _nameUpdateState.value =
                    NameUpdateState.Success(result.message ?: "Name updated successfully")
            } else {
                _nameUpdateState.value =
                    NameUpdateState.Error(result.errorMessage ?: "Failed to update name")
            }
        }
    }

    fun clearNameUpdateState() {
        _nameUpdateState.value = NameUpdateState.Idle
    }

    // Clear error messages so they don't persist
    fun clearError() {
        _authState.value = AuthState.Idle
    }

    // Get the display name of the currently logged-in user.
    // This is set during registration via UserProfileChangeRequest.
    // @return The user's display name, or "User" if not available
    val currentUserName: String
        get() = _currentUserName.value

    // Get the email of the currently logged-in user.
    // @return The user's email, or an empty string if not available
    val currentUserEmail: String
        get() = authService.getCurrentUser()?.email ?: ""
}

// Sealed class representing different authentication states
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NameUpdateState {
    data object Idle : NameUpdateState()
    data object Loading : NameUpdateState()
    data class Success(val message: String) : NameUpdateState()
    data class Error(val message: String) : NameUpdateState()
}
