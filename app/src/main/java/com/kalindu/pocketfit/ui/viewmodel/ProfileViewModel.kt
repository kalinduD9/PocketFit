package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.data.model.ProfileDetails
import com.kalindu.pocketfit.data.repository.ProfileDetailsRepository
import com.kalindu.pocketfit.data.repository.ProfilePhotoRepository
import com.kalindu.pocketfit.utils.ProfileValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val photoRepository = ProfilePhotoRepository(application.applicationContext)
    private val detailsRepository =
        ProfileDetailsRepository(application.applicationContext)

    private val _photoUri = MutableStateFlow(photoRepository.currentPhotoUri())
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _photoRevision = MutableStateFlow(0)
    val photoRevision: StateFlow<Int> = _photoRevision.asStateFlow()

    private val _photoState = MutableStateFlow<ProfilePhotoUiState>(ProfilePhotoUiState.Idle)
    val photoState: StateFlow<ProfilePhotoUiState> = _photoState.asStateFlow()

    private val _profileDetails = MutableStateFlow(detailsRepository.load())
    val profileDetails: StateFlow<ProfileDetails> = _profileDetails.asStateFlow()

    private val _detailsState =
        MutableStateFlow<ProfileDetailsUiState>(ProfileDetailsUiState.Idle)
    val detailsState: StateFlow<ProfileDetailsUiState> = _detailsState.asStateFlow()

    fun saveDetails(
        weight: String,
        height: String,
        age: String,
        fitnessGoal: String
    ): Boolean {
        val validation = ProfileValidation.validate(weight, height, age, fitnessGoal)
        val details = validation.details
        if (details == null) {
            _detailsState.value = ProfileDetailsUiState.Error(
                validation.message ?: "Profile details are invalid."
            )
            return false
        }

        detailsRepository.save(details)
        _profileDetails.value = details
        _detailsState.value = ProfileDetailsUiState.Success("Profile details saved.")
        return true
    }

    fun clearDetailsState() {
        _detailsState.value = ProfileDetailsUiState.Idle
    }

    fun captureCompleted(uri: Uri) {
        if (_photoState.value is ProfilePhotoUiState.Saving) return

        _photoState.value = ProfilePhotoUiState.Saving
        viewModelScope.launch {
            try {
                photoRepository.savePhoto(uri)
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
                photoRepository.deleteTemporaryPhoto(uri)
            }
        }
    }

    fun captureCancelled(uri: Uri?) {
        uri?.let(photoRepository::deleteTemporaryPhoto)
    }

    fun removePhoto() {
        if (_photoState.value is ProfilePhotoUiState.Saving) return

        _photoState.value = ProfilePhotoUiState.Saving
        viewModelScope.launch {
            photoRepository.removePhoto()
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

sealed class ProfileDetailsUiState {
    data object Idle : ProfileDetailsUiState()
    data class Success(val message: String) : ProfileDetailsUiState()
    data class Error(val message: String) : ProfileDetailsUiState()
}
