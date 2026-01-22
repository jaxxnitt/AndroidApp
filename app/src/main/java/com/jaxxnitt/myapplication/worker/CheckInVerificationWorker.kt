package com.jaxxnitt.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jaxxnitt.myapplication.AreYouDeadApplication
import java.util.concurrent.TimeUnit

class CheckInVerificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as AreYouDeadApplication
        val settings = app.settingsDataStore.getSettings()

        if (!settings.isEnabled) {
            Log.d(TAG, "Check-in monitoring is disabled")
            return Result.success()
        }

        val lastCheckIn = app.checkInRepository.getLastCheckInOnce()
        val now = System.currentTimeMillis()

        // Check if user has checked in within the required period
        val hasCheckedIn = if (lastCheckIn != null) {
            val daysSinceCheckIn = TimeUnit.MILLISECONDS.toDays(now - lastCheckIn.timestamp)
            daysSinceCheckIn < settings.checkInFrequencyDays
        } else {
            false
        }

        if (!hasCheckedIn) {
            Log.d(TAG, "User missed check-in, triggering alerts")
            // User missed check-in, send alerts
            AlertContactsWorker.enqueueNow(applicationContext)
        } else {
            Log.d(TAG, "User has checked in, no alert needed")
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "check_in_verification_worker"
        private const val TAG = "CheckInVerification"
    }
}
