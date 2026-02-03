package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FirstTimeSetupUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val setupComplete: Boolean = false
)

class FirstTimeSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val settingsDataStore = app.settingsDataStore
    private val authRepository = app.authRepository
    private val firestoreRepository = app.firestoreRepository

    private val _uiState = MutableStateFlow(FirstTimeSetupUiState())
    val uiState: StateFlow<FirstTimeSetupUiState> = _uiState.asStateFlow()

    init {
        // Prefill with Google account name if available
        val currentUser = authRepository.currentUser
        var prefillName = ""
        var prefillPhone = ""

        currentUser?.displayName?.let { googleName ->
            if (googleName.isNotBlank()) {
                prefillName = googleName
            }
        }
        currentUser?.phoneNumber?.let { phone ->
            if (phone.isNotBlank()) {
                prefillPhone = phone
            }
        }

        _uiState.value = _uiState.value.copy(
            fullName = prefillName,
            phoneNumber = prefillPhone
        )
    }

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, errorMessage = null)
    }

    fun completeSetup() {
        val fullName = _uiState.value.fullName.trim()
        val phoneNumber = _uiState.value.phoneNumber.trim()

        if (fullName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your full name")
            return
        }

        if (fullName.length < 2) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name is too short")
            return
        }

        if (phoneNumber.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your mobile number")
            return
        }

        if (phoneNumber.replace(Regex("[^0-9+]"), "").length < 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid phone number")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                settingsDataStore.updateFullName(fullName)
                settingsDataStore.updateUserName(fullName.split(" ").firstOrNull() ?: fullName)
                settingsDataStore.updatePhoneNumber(phoneNumber)
                settingsDataStore.setFirstTimeComplete()

                // Update phone number in Firestore user profile
                authRepository.currentUser?.uid?.let { uid ->
                    firestoreRepository.createOrUpdateUser(
                        UserData(
                            id = uid,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = authRepository.currentUser?.email,
                            profilePictureUrl = authRepository.currentUser?.photoUrl?.toString()
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(isSaving = false, setupComplete = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }
}
