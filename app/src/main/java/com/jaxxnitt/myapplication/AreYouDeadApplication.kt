package com.jaxxnitt.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.jaxxnitt.myapplication.data.auth.AuthRepository
import com.jaxxnitt.myapplication.data.database.AppDatabase
import com.jaxxnitt.myapplication.data.preferences.SettingsDataStore
import com.jaxxnitt.myapplication.data.repository.CheckInRepository
import com.jaxxnitt.myapplication.data.repository.ContactRepository
import com.jaxxnitt.myapplication.data.repository.FirestoreRepository
import com.jaxxnitt.myapplication.data.sync.SyncManager

class AreYouDeadApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val contactRepository by lazy { ContactRepository(database.contactDao()) }
    val checkInRepository by lazy { CheckInRepository(database.checkInDao()) }
    val settingsDataStore by lazy { SettingsDataStore(this) }

    // Auth and sync repositories
    val authRepository by lazy { AuthRepository() }
    val firestoreRepository by lazy { FirestoreRepository() }
    val syncManager by lazy {
        SyncManager(
            context = this,
            firestoreRepository = firestoreRepository,
            contactRepository = contactRepository,
            checkInRepository = checkInRepository,
            settingsDataStore = settingsDataStore
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Reminder channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Check-in Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to check in"
            }

            // Alert channel
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Missed Check-in Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when check-in is missed and contacts are notified"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val ALERT_CHANNEL_ID = "alert_channel"
    }
}
