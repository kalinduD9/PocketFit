package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.repository.ProfilePhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfilePhotoRepository(application.applicationContext)

    private val _photoUri = MutableStateFlow(repository.currentPhotoUri())
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _photoRevision = MutableStateFlow(0)
    val photoRevision: StateFlow<Int> = _photoRevision.asStateFlow()

    private val _photoState = MutableStateFlow<ProfilePhotoUiState>(ProfilePhotoUiState.Idle)
    val photoState: StateFlow<ProfilePhotoUiState> = _photoState.asStateFlow()

    fun captureCompleted(uri: Uri) {
        if (_photoState.value is ProfilePhotoUiState.Saving) return

        _photoState.value = ProfilePhotoUiState.Saving
        viewModelScope.launch {
            try {
                repository.savePhoto(uri)
                    .onSuccess { savedUri ->
                        _photoUri.value = savedUri
                        _photoRevision.value += 1
                        _photoState.value = ProfilePhotoUiState.Success("Profile picture updated.")
                    }
                    .onFailure { error ->
                        _photoState.value = ProfilePhotoUiState.Error(
                            error.message ?: "Profile picture could not be saved."
                        )
                    }
            } finally {
                repository.deleteTemporaryPhoto(uri)
            }
        }
    }

    fun captureCancelled(uri: Uri?) {
        uri?.let(repository::deleteTemporaryPhoto)
    }

    fun removePhoto() {
        if (_photoState.value is ProfilePhotoUiState.Saving) return

        _photoState.value = ProfilePhotoUiState.Saving
        viewModelScope.launch {
            repository.removePhoto()
                .onSuccess {
                    _photoUri.value = null
                    _photoRevision.value += 1
                    _photoState.value = ProfilePhotoUiState.Success("Profile picture removed.")
                }
                .onFailure { error ->
                    _photoState.value = ProfilePhotoUiState.Error(
                        error.message ?: "Profile picture could not be removed."
                    )
                }
        }
    }

    fun reportError(message: String) {
        _photoState.value = ProfilePhotoUiState.Error(message)
    }

    fun clearPhotoState() {
        _photoState.value = ProfilePhotoUiState.Idle
    }
}

sealed class ProfilePhotoUiState {
    data object Idle : ProfilePhotoUiState()
    data object Saving : ProfilePhotoUiState()
    data class Success(val message: String) : ProfilePhotoUiState()
    data class Error(val message: String) : ProfilePhotoUiState()
}
