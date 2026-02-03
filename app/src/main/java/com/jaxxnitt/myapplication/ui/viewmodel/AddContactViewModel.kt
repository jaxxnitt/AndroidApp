package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.database.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddContactUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddContactViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val contactRepository = app.contactRepository
    private val firestoreRepository = app.firestoreRepository
    private val settingsDataStore = app.settingsDataStore
    private val authRepository = app.authRepository

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    private var editingContactId: Long? = null

    fun loadContact(contactId: Long) {
        viewModelScope.launch {
            val contact = contactRepository.getContactById(contactId)
            if (contact != null) {
                editingContactId = contactId
                _uiState.value = _uiState.value.copy(
                    name = contact.name,
                    phone = contact.phone,
                    email = contact.email,
                    isEditing = true
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun setContactFromPicker(name: String, phone: String, email: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            phone = phone,
            email = email,
            errorMessage = null
        )
    }

    fun saveContact() {
        val state = _uiState.value

        // Validation
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Name is required")
            return
        }

        if (state.phone.isBlank() && state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "At least phone or email is required")
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val contact = Contact(
                    id = editingContactId ?: 0,
                    name = state.name.trim(),
                    phone = state.phone.trim(),
                    email = state.email.trim()
                )

                if (editingContactId != null) {
                    contactRepository.update(contact)
                } else {
                    contactRepository.insert(contact)
                }

                // Create lifeguard relationship if contact has a phone number
                if (contact.phone.isNotBlank()) {
                    try {
                        val currentUserId = authRepository.currentUser?.uid
                        val settings = settingsDataStore.getSettings()
                        if (currentUserId != null) {
                            firestoreRepository.createLifeguardRelationship(
                                guardianPhone = contact.phone,
                                protectedUserId = currentUserId,
                                protectedUserName = settings.fullName,
                                protectedUserPhone = settings.phoneNumber
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("AddContactVM", "Failed to create lifeguard relationship", e)
                    }
                }

                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save contact: ${e.message}"
                )
            }
        }
    }
}
