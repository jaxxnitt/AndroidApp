package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.auth.AuthRepository
import com.jaxxnitt.myapplication.data.auth.AuthResult
import com.jaxxnitt.myapplication.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMethod {
    NONE, GOOGLE, PHONE
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val authMethod: AuthMethod = AuthMethod.NONE,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userPhoneNumber: String? = null,
    val userPhotoUrl: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val authRepository = app.authRepository
    private val settingsDataStore = app.settingsDataStore
    private val firestoreRepository = app.firestoreRepository
    private val syncManager = app.syncManager

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isLoggedIn,
            userName = authRepository.currentUser?.displayName,
            userEmail = authRepository.currentUser?.email,
            userPhoneNumber = authRepository.currentUser?.phoneNumber,
            userPhotoUrl = authRepository.currentUser?.photoUrl?.toString()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val credentialManager = CredentialManager.create(application)

    companion object {
        const val WEB_CLIENT_ID = "105120292015-0g6l79dja7km0k9hd9o985s7ud9v8dcs.apps.googleusercontent.com"
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                authMethod = AuthMethod.GOOGLE
            )

            try {
                // First try with authorized accounts only (fast path)
                val authorizedOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val authorizedRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(authorizedOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activity,
                    request = authorizedRequest
                )

                handleGoogleSignInResult(result.credential)
            } catch (e: NoCredentialException) {
                // No authorized accounts found, show full account picker
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(WEB_CLIENT_ID)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        context = activity,
                        request = request
                    )

                    handleGoogleSignInResult(result.credential)
                } catch (e2: GetCredentialCancellationException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                } catch (e2: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Sign in failed. Please try again."
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign in failed. Please try again."
                )
            }
        }
    }

    private suspend fun handleGoogleSignInResult(credential: androidx.credentials.Credential) {
        when (credential) {
            is androidx.credentials.CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    when (val authResult = authRepository.signInWithGoogle(idToken)) {
                        is AuthResult.Success -> {
                            settingsDataStore.updateAuthInfo(
                                userId = authResult.user.uid,
                                provider = "google"
                            )

                            // Create user data in Firestore
                            firestoreRepository.createOrUpdateUser(
                                UserData(
                                    id = authResult.user.uid,
                                    fullName = authResult.user.displayName ?: "",
                                    email = authResult.user.email,
                                    profilePictureUrl = authResult.user.photoUrl?.toString()
                                )
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                userName = authResult.user.displayName,
                                userEmail = authResult.user.email,
                                userPhotoUrl = authResult.user.photoUrl?.toString()
                            )

                            // Trigger data migration
                            triggerDataMigration()
                        }

                        is AuthResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = authResult.message
                            )
                        }
                    }
                }
            }

            else -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected credential type"
                )
            }
        }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            authMethod = AuthMethod.PHONE
        )

        authRepository.sendVerificationCode(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = { verificationId, token ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationId = verificationId,
                    resendToken = token
                )
            },
            onVerificationCompleted = { credential ->
                // Auto-verification (rare case)
                viewModelScope.launch {
                    signInWithPhoneCredential(credential)
                }
            },
            onVerificationFailed = { errorMessage ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        )
    }

    fun resendVerificationCode(phoneNumber: String, activity: Activity) {
        val token = _uiState.value.resendToken ?: return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        authRepository.resendVerificationCode(
            phoneNumber = phoneNumber,
            activity = activity,
            token = token,
            onCodeSent = { verificationId, newToken ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationId = verificationId,
                    resendToken = newToken
                )
            },
            onVerificationCompleted = { credential ->
                viewModelScope.launch {
                    signInWithPhoneCredential(credential)
                }
            },
            onVerificationFailed = { errorMessage ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        )
    }

    fun verifyOtp(verificationId: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val credential = authRepository.verifyOtpCode(verificationId, code)
            signInWithPhoneCredential(credential)
        }
    }

    private suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        when (val result = authRepository.signInWithPhoneCredential(credential)) {
            is AuthResult.Success -> {
                settingsDataStore.updateAuthInfo(
                    userId = result.user.uid,
                    provider = "phone"
                )

                // Create user data in Firestore
                firestoreRepository.createOrUpdateUser(
                    UserData(
                        id = result.user.uid,
                        phoneNumber = result.user.phoneNumber
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userName = result.user.phoneNumber,
                    userPhoneNumber = result.user.phoneNumber
                )

                triggerDataMigration()
            }

            is AuthResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun triggerDataMigration() {
        viewModelScope.launch {
            syncManager.performInitialMigration()
        }
    }

    fun signOut() {
        authRepository.signOut()
        viewModelScope.launch {
            settingsDataStore.clearAuthInfo()
        }
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearVerificationId() {
        _uiState.value = _uiState.value.copy(verificationId = null)
    }
}
