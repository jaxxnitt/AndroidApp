package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.preferences.AppSettings
import com.jaxxnitt.myapplication.worker.WorkerScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val settingsDataStore = app.settingsDataStore

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateCheckInTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsDataStore.updateCheckInTime(hour, minute)
            rescheduleWorkers()
        }
    }

    fun updateGracePeriod(hours: Int) {
        viewModelScope.launch {
            settingsDataStore.updateGracePeriod(hours)
            rescheduleWorkers()
        }
    }

    fun updateCheckInFrequency(days: Int) {
        viewModelScope.launch {
            settingsDataStore.updateCheckInFrequency(days)
            rescheduleWorkers()
        }
    }

    fun updateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateEnabled(enabled)
            if (enabled) {
                rescheduleWorkers()
            } else {
                WorkerScheduler.cancelAllWorkers(getApplication())
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            settingsDataStore.updateUserName(name)
        }
    }

    fun updateMessagingMethod(method: String) {
        viewModelScope.launch {
            settingsDataStore.updateMessagingMethod(method)
        }
    }

    private suspend fun rescheduleWorkers() {
        val currentSettings = settingsDataStore.getSettings()
        if (currentSettings.isEnabled) {
            WorkerScheduler.scheduleWorkers(getApplication(), currentSettings)
        }
    }
}
