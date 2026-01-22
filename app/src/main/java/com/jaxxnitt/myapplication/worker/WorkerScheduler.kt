package com.jaxxnitt.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jaxxnitt.myapplication.data.preferences.AppSettings
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    private const val TAG = "WorkerScheduler"

    fun scheduleWorkers(context: Context, settings: AppSettings) {
        if (!settings.isEnabled) {
            cancelAllWorkers(context)
            return
        }

        scheduleReminderWorker(context, settings)
        scheduleVerificationWorker(context, settings)
    }

    private fun scheduleReminderWorker(context: Context, settings: AppSettings) {
        val initialDelay = calculateInitialDelay(
            targetHour = settings.checkInHour,
            targetMinute = settings.checkInMinute
        )

        // Schedule to repeat based on check-in frequency
        val repeatInterval = settings.checkInFrequencyDays.toLong()

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            repeatInterval, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(TAG, "Scheduled reminder worker: initial delay ${initialDelay / 1000 / 60} minutes, " +
                "repeat every $repeatInterval days")
    }

    private fun scheduleVerificationWorker(context: Context, settings: AppSettings) {
        // Verification runs at check-in time + grace period
        val verificationHour = settings.checkInHour + settings.gracePeriodHours
        val initialDelay = calculateInitialDelay(
            targetHour = verificationHour,
            targetMinute = settings.checkInMinute
        )

        // Schedule to repeat based on check-in frequency
        val repeatInterval = settings.checkInFrequencyDays.toLong()

        val workRequest = PeriodicWorkRequestBuilder<CheckInVerificationWorker>(
            repeatInterval, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CheckInVerificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(TAG, "Scheduled verification worker: initial delay ${initialDelay / 1000 / 60} minutes, " +
                "repeat every $repeatInterval days")
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour % 24)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Handle hour overflow (e.g., 9 AM + 16 hours = 1 AM next day)
            if (targetHour >= 24) {
                add(Calendar.DAY_OF_YEAR, targetHour / 24)
            }
        }

        // If the target time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }

    fun cancelAllWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(DailyReminderWorker.WORK_NAME)
        workManager.cancelUniqueWork(CheckInVerificationWorker.WORK_NAME)
        Log.d(TAG, "Cancelled all workers")
    }

    fun scheduleWorkersOnBoot(context: Context, settings: AppSettings) {
        if (settings.isEnabled) {
            scheduleWorkers(context, settings)
        }
    }
}
