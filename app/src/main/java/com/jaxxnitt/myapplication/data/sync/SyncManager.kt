package com.jaxxnitt.myapplication.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.jaxxnitt.myapplication.data.database.CheckIn
import com.jaxxnitt.myapplication.data.database.Contact
import com.jaxxnitt.myapplication.data.preferences.SettingsDataStore
import com.jaxxnitt.myapplication.data.repository.CheckInRepository
import com.jaxxnitt.myapplication.data.repository.ContactRepository
import com.jaxxnitt.myapplication.data.repository.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    ERROR,
    OFFLINE
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val lastSyncTime: Long = 0,
    val errorMessage: String? = null,
    val isOnline: Boolean = true
)

class SyncManager(
    private val context: Context,
    private val firestoreRepository: FirestoreRepository,
    private val contactRepository: ContactRepository,
    private val checkInRepository: CheckInRepository,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        monitorConnectivity()
    }

    private fun monitorConnectivity() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _syncState.value = _syncState.value.copy(isOnline = true)
                // Auto-sync when coming online
                scope.launch {
                    val settings = settingsDataStore.settingsFlow.first()
                    if (settings.userId != null) {
                        syncAll()
                    }
                }
            }

            override fun onLost(network: Network) {
                _syncState.value = _syncState.value.copy(
                    isOnline = false,
                    status = SyncStatus.OFFLINE
                )
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            // Handle case where callback registration fails
        }
    }

    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    suspend fun syncAll() {
        if (!isOnline()) {
            _syncState.value = _syncState.value.copy(status = SyncStatus.OFFLINE)
            return
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING)

        try {
            // Sync settings
            val settings = settingsDataStore.getSettings()
            firestoreRepository.syncSettings(settings)

            // Update sync timestamp
            val now = System.currentTimeMillis()
            settingsDataStore.updateLastSyncTimestamp(now)

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SYNCED,
                lastSyncTime = now,
                errorMessage = null
            )
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                errorMessage = e.message
            )
        }
    }

    suspend fun syncContact(contact: Contact) {
        if (!isOnline()) return

        try {
            if (contact.cloudId != null) {
                firestoreRepository.updateCloudContact(contact.cloudId, contact)
            } else {
                val result = firestoreRepository.syncContact(contact)
                result.getOrNull()?.let { cloudId ->
                    // Update local contact with cloud ID
                    contactRepository.update(
                        contact.copy(
                            cloudId = cloudId,
                            lastSyncedAt = System.currentTimeMillis(),
                            pendingSync = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Mark contact as pending sync
            contactRepository.update(contact.copy(pendingSync = true))
        }
    }

    suspend fun syncCheckIn(checkIn: CheckIn) {
        if (!isOnline()) return

        try {
            val result = firestoreRepository.syncCheckIn(checkIn)
            result.getOrNull()?.let { cloudId ->
                checkInRepository.update(
                    checkIn.copy(
                        cloudId = cloudId,
                        syncedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            // Silently fail - check-in is stored locally
        }
    }

    suspend fun performInitialMigration() {
        if (!isOnline()) return

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING)

        try {
            val contacts = contactRepository.getAllContactsOnce()
            val checkIns = checkInRepository.getRecentCheckIns(30)
            val settings = settingsDataStore.getSettings()

            firestoreRepository.migrateLocalData(
                contacts = contacts,
                checkIns = checkIns,
                settings = settings
            )

            val now = System.currentTimeMillis()
            settingsDataStore.updateLastSyncTimestamp(now)

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SYNCED,
                lastSyncTime = now
            )
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                errorMessage = e.message
            )
        }
    }

    suspend fun syncPendingContacts() {
        if (!isOnline()) return

        val contacts = contactRepository.getAllContactsOnce()
        contacts.filter { it.pendingSync }.forEach { contact ->
            syncContact(contact)
        }
    }
}
