package com.jaxxnitt.myapplication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.jaxxnitt.myapplication.data.database.CheckIn
import com.jaxxnitt.myapplication.data.database.Contact
import com.jaxxnitt.myapplication.data.model.CloudCheckIn
import com.jaxxnitt.myapplication.data.model.CloudContact
import com.jaxxnitt.myapplication.data.model.CloudSettings
import com.jaxxnitt.myapplication.data.model.UserData
import com.jaxxnitt.myapplication.data.preferences.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String?
        get() = auth.currentUser?.uid

    private fun userDoc() = userId?.let { firestore.collection("users").document(it) }
    private fun contactsCollection() = userDoc()?.collection("contacts")
    private fun checkInsCollection() = userDoc()?.collection("checkIns")
    private fun settingsCollection() = userDoc()?.collection("settings")

    // User profile operations
    suspend fun createOrUpdateUser(userData: UserData): Result<Unit> {
        return try {
            userDoc()?.set(userData, SetOptions.merge())?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserDataFlow(): Flow<UserData?> = callbackFlow {
        val listener = userDoc()?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserData::class.java))
        }
        awaitClose { listener?.remove() }
    }

    // Contact sync operations
    suspend fun syncContact(contact: Contact): Result<String> {
        return try {
            val cloudContact = CloudContact(
                localId = contact.id,
                name = contact.name,
                phone = contact.phone,
                email = contact.email
            )
            val docRef = contactsCollection()?.add(cloudContact)?.await()
            Result.success(docRef?.id ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCloudContact(cloudId: String, contact: Contact): Result<Unit> {
        return try {
            contactsCollection()?.document(cloudId)?.set(
                CloudContact(
                    id = cloudId,
                    localId = contact.id,
                    name = contact.name,
                    phone = contact.phone,
                    email = contact.email
                ),
                SetOptions.merge()
            )?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCloudContact(cloudId: String): Result<Unit> {
        return try {
            contactsCollection()?.document(cloudId)?.update("isDeleted", true)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getContactsFlow(): Flow<List<CloudContact>> = callbackFlow {
        val listener = contactsCollection()
            ?.whereEqualTo("isDeleted", false)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val contacts = snapshot?.documents?.mapNotNull {
                    it.toObject(CloudContact::class.java)
                } ?: emptyList()
                trySend(contacts)
            }
        awaitClose { listener?.remove() }
    }

    suspend fun getAllCloudContacts(): Result<List<CloudContact>> {
        return try {
            val snapshot = contactsCollection()
                ?.whereEqualTo("isDeleted", false)
                ?.get()
                ?.await()
            val contacts = snapshot?.documents?.mapNotNull {
                it.toObject(CloudContact::class.java)
            } ?: emptyList()
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CheckIn sync operations
    suspend fun syncCheckIn(checkIn: CheckIn): Result<String> {
        return try {
            val cloudCheckIn = CloudCheckIn(
                localId = checkIn.id,
                timestamp = checkIn.timestamp
            )
            val docRef = checkInsCollection()?.add(cloudCheckIn)?.await()
            Result.success(docRef?.id ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentCheckIns(limit: Int = 100): Result<List<CloudCheckIn>> {
        return try {
            val snapshot = checkInsCollection()
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.limit(limit.toLong())
                ?.get()
                ?.await()
            val checkIns = snapshot?.documents?.mapNotNull {
                it.toObject(CloudCheckIn::class.java)
            } ?: emptyList()
            Result.success(checkIns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Settings sync operations
    suspend fun syncSettings(settings: AppSettings): Result<Unit> {
        return try {
            val cloudSettings = CloudSettings(
                checkInHour = settings.checkInHour,
                checkInMinute = settings.checkInMinute,
                gracePeriodHours = settings.gracePeriodHours,
                checkInFrequencyDays = settings.checkInFrequencyDays,
                isEnabled = settings.isEnabled,
                userName = settings.userName,
                fullName = settings.fullName,
                profilePictureUri = settings.profilePictureUri
            )
            settingsCollection()?.document("app")
                ?.set(cloudSettings, SetOptions.merge())?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCloudSettings(): Result<CloudSettings?> {
        return try {
            val snapshot = settingsCollection()?.document("app")?.get()?.await()
            Result.success(snapshot?.toObject(CloudSettings::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSettingsFlow(): Flow<CloudSettings?> = callbackFlow {
        val listener = settingsCollection()?.document("app")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(CloudSettings::class.java))
            }
        awaitClose { listener?.remove() }
    }

    // Bulk sync for initial migration
    suspend fun migrateLocalData(
        contacts: List<Contact>,
        checkIns: List<CheckIn>,
        settings: AppSettings
    ): Result<Unit> {
        return try {
            firestore.runBatch { batch ->
                // Sync contacts
                contacts.forEach { contact ->
                    val docRef = contactsCollection()?.document()
                    docRef?.let {
                        batch.set(
                            it, CloudContact(
                                localId = contact.id,
                                name = contact.name,
                                phone = contact.phone,
                                email = contact.email
                            )
                        )
                    }
                }

                // Sync recent check-ins (last 30 days)
                val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                checkIns.filter { it.timestamp > thirtyDaysAgo }.forEach { checkIn ->
                    val docRef = checkInsCollection()?.document()
                    docRef?.let {
                        batch.set(
                            it, CloudCheckIn(
                                localId = checkIn.id,
                                timestamp = checkIn.timestamp
                            )
                        )
                    }
                }
            }.await()

            // Sync settings separately
            syncSettings(settings)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete all user data (for account deletion)
    suspend fun deleteAllUserData(): Result<Unit> {
        return try {
            // Delete subcollections first
            contactsCollection()?.get()?.await()?.documents?.forEach { doc ->
                doc.reference.delete().await()
            }
            checkInsCollection()?.get()?.await()?.documents?.forEach { doc ->
                doc.reference.delete().await()
            }
            settingsCollection()?.get()?.await()?.documents?.forEach { doc ->
                doc.reference.delete().await()
            }
            // Delete user document
            userDoc()?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
