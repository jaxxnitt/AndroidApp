package com.jaxxnitt.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.database.CheckIn
import com.jaxxnitt.myapplication.data.preferences.AppSettings
import com.jaxxnitt.myapplication.util.NotificationHelper
import com.jaxxnitt.myapplication.worker.WorkerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val lastCheckIn: CheckIn? = null,
    val settings: AppSettings = AppSettings(),
    val isCheckedInForPeriod: Boolean = false,
    val nextCheckInDue: String = "",
    val checkInStatus: CheckInStatus = CheckInStatus.PENDING
)

enum class CheckInStatus {
    PENDING,    // Need to check in
    CHECKED_IN, // Already checked in for this period
    OVERDUE     // Missed check-in window
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AreYouDeadApplication
    private val checkInRepository = app.checkInRepository
    private val settingsDataStore = app.settingsDataStore

    private val _checkInSuccess = MutableStateFlow(false)
    val checkInSuccess: StateFlow<Boolean> = _checkInSuccess.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        checkInRepository.lastCheckIn,
        settingsDataStore.settingsFlow
    ) { lastCheckIn, settings ->
        val status = calculateCheckInStatus(lastCheckIn, settings)
        val nextDue = calculateNextCheckInDue(lastCheckIn, settings)
        HomeUiState(
            lastCheckIn = lastCheckIn,
            settings = settings,
            isCheckedInForPeriod = status == CheckInStatus.CHECKED_IN,
            nextCheckInDue = nextDue,
            checkInStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun checkIn() {
        viewModelScope.launch {
            checkInRepository.checkIn()
            NotificationHelper.cancelReminderNotification(getApplication())
            _checkInSuccess.value = true

            // Reschedule workers with updated check-in
            val settings = settingsDataStore.getSettings()
            WorkerScheduler.scheduleWorkers(getApplication(), settings)
        }
    }

    fun resetCheckInSuccess() {
        _checkInSuccess.value = false
    }

    private fun calculateCheckInStatus(lastCheckIn: CheckIn?, settings: AppSettings): CheckInStatus {
        if (lastCheckIn == null) return CheckInStatus.PENDING

        val now = System.currentTimeMillis()
        val lastCheckInTime = lastCheckIn.timestamp
        val daysSinceCheckIn = TimeUnit.MILLISECONDS.toDays(now - lastCheckInTime)

        // If checked in within the frequency period, they're good
        if (daysSinceCheckIn < settings.checkInFrequencyDays) {
            return CheckInStatus.CHECKED_IN
        }

        // Check if we're past the grace period for today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, settings.checkInHour)
        calendar.set(Calendar.MINUTE, settings.checkInMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.HOUR_OF_DAY, settings.gracePeriodHours)

        val graceDeadline = calendar.timeInMillis

        return if (daysSinceCheckIn >= settings.checkInFrequencyDays && now > graceDeadline) {
            CheckInStatus.OVERDUE
        } else {
            CheckInStatus.PENDING
        }
    }

    private fun calculateNextCheckInDue(lastCheckIn: CheckIn?, settings: AppSettings): String {
        val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

        if (lastCheckIn == null) {
            return "Check in now!"
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastCheckIn.timestamp
        calendar.add(Calendar.DAY_OF_YEAR, settings.checkInFrequencyDays)
        calendar.set(Calendar.HOUR_OF_DAY, settings.checkInHour)
        calendar.set(Calendar.MINUTE, settings.checkInMinute)
        calendar.set(Calendar.SECOND, 0)

        // If next due is in the past, advance by frequency until it's in the future
        while (calendar.timeInMillis < now) {
            calendar.add(Calendar.DAY_OF_YEAR, settings.checkInFrequencyDays)
        }

        return dateFormat.format(Date(calendar.timeInMillis))
    }

    fun formatLastCheckIn(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}
