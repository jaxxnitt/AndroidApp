package com.jaxxnitt.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.util.NotificationHelper
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as AreYouDeadApplication
        val settings = app.settingsDataStore.getSettings()

        if (!settings.isEnabled) {
            return Result.success()
        }

        // Check if the user has already checked in for this period
        val lastCheckIn = app.checkInRepository.getLastCheckInOnce()
        val now = System.currentTimeMillis()

        if (lastCheckIn != null) {
            val daysSinceCheckIn = TimeUnit.MILLISECONDS.toDays(now - lastCheckIn.timestamp)
            if (daysSinceCheckIn < settings.checkInFrequencyDays) {
                // Already checked in for this period
                return Result.success()
            }
        }

        // Show reminder notification
        NotificationHelper.showReminderNotification(applicationContext)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_reminder_worker"
    }
}
