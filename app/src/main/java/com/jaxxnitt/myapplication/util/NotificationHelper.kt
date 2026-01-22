package com.jaxxnitt.myapplication.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.MainActivity
import com.jaxxnitt.myapplication.R

object NotificationHelper {

    private const val REMINDER_NOTIFICATION_ID = 1001
    private const val ALERT_NOTIFICATION_ID = 1002

    fun showReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AreYouDeadApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to Check In!")
            .setContentText("Tap to confirm you're alive")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun showAlertSentNotification(context: Context, contactCount: Int) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, AreYouDeadApplication.ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alert Sent!")
            .setContentText("Notified $contactCount emergency contact(s)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ALERT_NOTIFICATION_ID, notification)
    }

    fun cancelReminderNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(REMINDER_NOTIFICATION_ID)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
