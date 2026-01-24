package com.jaxxnitt.myapplication.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val checkInHour: Int = 9,
    val checkInMinute: Int = 0,
    val gracePeriodHours: Int = 4,
    val checkInFrequencyDays: Int = 1, // 1, 2, or 3 days
    val isEnabled: Boolean = true,
    val userName: String = "User",
    val fullName: String = "",
    val profilePictureUri: String = "",
    val isFirstTime: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val CHECK_IN_HOUR = intPreferencesKey("check_in_hour")
        val CHECK_IN_MINUTE = intPreferencesKey("check_in_minute")
        val GRACE_PERIOD_HOURS = intPreferencesKey("grace_period_hours")
        val CHECK_IN_FREQUENCY_DAYS = intPreferencesKey("check_in_frequency_days")
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val FULL_NAME = stringPreferencesKey("full_name")
        val PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
        val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            checkInHour = preferences[PreferencesKeys.CHECK_IN_HOUR] ?: 9,
            checkInMinute = preferences[PreferencesKeys.CHECK_IN_MINUTE] ?: 0,
            gracePeriodHours = preferences[PreferencesKeys.GRACE_PERIOD_HOURS] ?: 4,
            checkInFrequencyDays = preferences[PreferencesKeys.CHECK_IN_FREQUENCY_DAYS] ?: 1,
            isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "User",
            fullName = preferences[PreferencesKeys.FULL_NAME] ?: "",
            profilePictureUri = preferences[PreferencesKeys.PROFILE_PICTURE_URI] ?: "",
            isFirstTime = preferences[PreferencesKeys.IS_FIRST_TIME] ?: true
        )
    }

    suspend fun getSettings(): AppSettings {
        return settingsFlow.first()
    }

    suspend fun updateCheckInTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_IN_HOUR] = hour
            preferences[PreferencesKeys.CHECK_IN_MINUTE] = minute
        }
    }

    suspend fun updateGracePeriod(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRACE_PERIOD_HOURS] = hours
        }
    }

    suspend fun updateCheckInFrequency(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_IN_FREQUENCY_DAYS] = days.coerceIn(1, 3)
        }
    }

    suspend fun updateEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ENABLED] = enabled
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_IN_HOUR] = settings.checkInHour
            preferences[PreferencesKeys.CHECK_IN_MINUTE] = settings.checkInMinute
            preferences[PreferencesKeys.GRACE_PERIOD_HOURS] = settings.gracePeriodHours
            preferences[PreferencesKeys.CHECK_IN_FREQUENCY_DAYS] = settings.checkInFrequencyDays
            preferences[PreferencesKeys.IS_ENABLED] = settings.isEnabled
            preferences[PreferencesKeys.USER_NAME] = settings.userName
            preferences[PreferencesKeys.FULL_NAME] = settings.fullName
            preferences[PreferencesKeys.PROFILE_PICTURE_URI] = settings.profilePictureUri
            preferences[PreferencesKeys.IS_FIRST_TIME] = settings.isFirstTime
        }
    }

    suspend fun updateFullName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_NAME] = name
        }
    }

    suspend fun updateProfilePictureUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_PICTURE_URI] = uri
        }
    }

    suspend fun setFirstTimeComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_TIME] = false
        }
    }
}
