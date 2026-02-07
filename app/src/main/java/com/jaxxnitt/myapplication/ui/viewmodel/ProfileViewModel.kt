package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.database.CheckIn
import com.jaxxnitt.myapplication.data.model.LifeguardRelationship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val profilePictureUri: String = "",
    val checkInHistory: List<CheckIn> = emptyList(),
    val lifeguardFor: List<LifeguardRelationship> = emptyList(),
    val isLoadingLifeguard: Boolean = true
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val settingsDataStore = app.settingsDataStore
    private val checkInRepository = app.checkInRepository
    private val firestoreRepository = app.firestoreRepository

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsDataStore.settingsFlow,
                checkInRepository.allCheckIns
            ) { settings: com.jaxxnitt.myapplication.data.preferences.AppSettings,
                checkIns: List<CheckIn> ->
                ProfileUiState(
                    fullName = settings.fullName,
                    userName = settings.userName,
                    phoneNumber = settings.phoneNumber,
                    profilePictureUri = settings.profilePictureUri,
                    checkInHistory = checkIns.sortedByDescending { checkIn -> checkIn.timestamp },
                    lifeguardFor = _uiState.value.lifeguardFor,
                    isLoadingLifeguard = _uiState.value.isLoadingLifeguard
                )
            }.collect { state ->
                _uiState.value = state
                // Load lifeguard relationships once we have the phone number
                if (state.isLoadingLifeguard) {
                    if (state.phoneNumber.isNotBlank()) {
                        loadLifeguardRelationships(state.phoneNumber)
                    } else {
                        // No phone number set, nothing to load
                        _uiState.value = _uiState.value.copy(isLoadingLifeguard = false)
                    }
                }
            }
        }
    }

    private fun loadLifeguardRelationships(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val relationships = firestoreRepository.getLifeguardFor(phoneNumber)
                _uiState.value = _uiState.value.copy(
                    lifeguardFor = relationships,
                    isLoadingLifeguard = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingLifeguard = false)
            }
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            settingsDataStore.updateProfilePictureUri(uri)
        }
    }
}
