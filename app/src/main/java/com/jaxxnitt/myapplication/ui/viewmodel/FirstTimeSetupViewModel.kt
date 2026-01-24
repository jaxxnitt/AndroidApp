package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FirstTimeSetupUiState(
    val fullName: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val setupComplete: Boolean = false
)

class FirstTimeSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = (application as AreYouDeadApplication).settingsDataStore

    private val _uiState = MutableStateFlow(FirstTimeSetupUiState())
    val uiState: StateFlow<FirstTimeSetupUiState> = _uiState.asStateFlow()

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
    }

    fun completeSetup() {
        val fullName = _uiState.value.fullName.trim()

        if (fullName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your full name")
            return
        }

        if (fullName.length < 2) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name is too short")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                settingsDataStore.updateFullName(fullName)
                settingsDataStore.updateUserName(fullName.split(" ").firstOrNull() ?: fullName)
                settingsDataStore.setFirstTimeComplete()
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
