package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.database.CheckIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val userName: String = "",
    val profilePictureUri: String = "",
    val checkInHistory: List<CheckIn> = emptyList()
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val settingsDataStore = app.settingsDataStore
    private val checkInRepository = app.checkInRepository

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
                    profilePictureUri = settings.profilePictureUri,
                    checkInHistory = checkIns.sortedByDescending { checkIn -> checkIn.timestamp }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            settingsDataStore.updateProfilePictureUri(uri)
        }
    }
}
