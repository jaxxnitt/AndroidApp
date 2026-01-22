package com.jaxxnitt.myapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.data.api.AlertApiService
import com.jaxxnitt.myapplication.data.api.AlertRequest
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

        var successCount = 0

        for (contact in contacts) {
            // Send SMS if phone is available
            if (contact.phone.isNotBlank()) {
                val smsMessage = buildSmsMessage(settings.userName, lastCheckInText)
                val smsSent = SmsHelper.sendSms(applicationContext, contact.phone, smsMessage)
                if (smsSent) successCount++
            }

            // Send Email if email is available
            if (contact.email.isNotBlank()) {
                val emailSent = sendAlertEmail(
                    toEmail = contact.email,
                    userName = settings.userName,
                    lastCheckInText = lastCheckInText
                )
                if (emailSent) successCount++
            }
        }

        // Show notification that alerts were sent
        NotificationHelper.showAlertSentNotification(applicationContext, contacts.size)

        Log.d(TAG, "Alerts sent to ${contacts.size} contacts, $successCount successful")

        return Result.success()
    }

    private fun buildSmsMessage(userName: String, lastCheckInText: String): String {
        return "SAFETY ALERT: $userName has not checked in on the \"Are You Dead?\" app. " +
                "Last check-in: $lastCheckInText. Please try to contact them."
    }

    private suspend fun sendAlertEmail(
        toEmail: String,
        userName: String,
        lastCheckInText: String
    ): Boolean {
        return try {
            val apiService = AlertApiService.create()
            val request = AlertRequest(
                to = toEmail,
                subject = "Safety Alert: $userName Missed Check-In",
                body = buildEmailBody(userName, lastCheckInText),
                userName = userName
            )
            val response = apiService.sendAlertEmail(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email to $toEmail", e)
            false
        }
    }

    private fun buildEmailBody(userName: String, lastCheckInText: String): String {
        return """
            SAFETY ALERT

            $userName has not checked in on the "Are You Dead?" safety check-in app.

            Last check-in: $lastCheckInText

            This could indicate that they may need assistance. Please try to contact them to ensure they are safe.

            ---
            This is an automated message from the "Are You Dead?" safety check-in app.
        """.trimIndent()
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
