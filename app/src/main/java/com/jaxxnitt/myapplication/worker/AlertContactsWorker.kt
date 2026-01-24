package com.jaxxnitt.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.util.EmailHelper
import com.jaxxnitt.myapplication.util.NotificationHelper
import com.jaxxnitt.myapplication.util.SmsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertContactsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as AreYouDeadApplication
        val settings = app.settingsDataStore.getSettings()
        val contacts = app.contactRepository.getAllContactsOnce()

        if (contacts.isEmpty()) {
            Log.w(TAG, "No emergency contacts configured")
            return Result.success()
        }

        val lastCheckIn = app.checkInRepository.getLastCheckInOnce()
        val lastCheckInText = if (lastCheckIn != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            dateFormat.format(Date(lastCheckIn.timestamp))
        } else {
            "Never"
        }

        var smsSuccessCount = 0
        var emailSuccessCount = 0

        for (contact in contacts) {
            // Send SMS if phone is available (using smart method with Twilio fallback)
            if (contact.phone.isNotBlank()) {
                val smsMessage = buildSmsMessage(settings.userName, lastCheckInText)
                val smsSent = SmsHelper.sendSmsSmart(applicationContext, contact.phone, smsMessage)
                if (smsSent) smsSuccessCount++
            }

            // Send Email if email is available (using smart method with SendGrid/backend)
            if (contact.email.isNotBlank()) {
                val emailSent = EmailHelper.sendAlertEmail(
                    toEmail = contact.email,
                    userName = settings.userName,
                    lastCheckInText = lastCheckInText
                )
                if (emailSent) emailSuccessCount++
            }
        }

        val totalSuccess = smsSuccessCount + emailSuccessCount

        // Show notification that alerts were sent
        NotificationHelper.showAlertSentNotification(applicationContext, contacts.size)

        Log.d(TAG, "Alerts sent to ${contacts.size} contacts: $smsSuccessCount SMS, $emailSuccessCount emails successful")

        return Result.success()
    }

    private fun buildSmsMessage(userName: String, lastCheckInText: String): String {
        return "SAFETY ALERT: $userName has not checked in on the \"Are You Alive?\" app. " +
                "Last check-in: $lastCheckInText. Please try to contact them."
    }

    companion object {
        const val WORK_NAME = "alert_contacts_worker"
        private const val TAG = "AlertContactsWorker"

        fun enqueueNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<AlertContactsWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
