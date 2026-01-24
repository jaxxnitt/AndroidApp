package com.jaxxnitt.myapplication.util

import android.util.Log
import com.jaxxnitt.myapplication.data.api.AlertApiService
import com.jaxxnitt.myapplication.data.api.AlertRequest
import com.jaxxnitt.myapplication.data.api.BackendConfig
import com.jaxxnitt.myapplication.data.api.SendGridConfig
import com.jaxxnitt.myapplication.data.api.SendGridEmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EmailHelper {

    private const val TAG = "EmailHelper"

    /**
     * Send an alert email using the best available method:
     * 1. If SendGrid is configured, use SendGrid API directly
     * 2. If a custom backend is configured, use the backend API
     * 3. Otherwise, return false (no email service available)
     */
    suspend fun sendAlertEmail(
        toEmail: String,
        userName: String,
        lastCheckInText: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Try SendGrid first (direct integration, more reliable)
        if (SendGridConfig.isConfigured) {
            return@withContext sendViaSendGrid(toEmail, userName, lastCheckInText)
        }

        // Try custom backend API
        if (BackendConfig.isConfigured) {
            return@withContext sendViaBackendApi(toEmail, userName, lastCheckInText)
        }

        Log.w(TAG, "No email service configured. Configure SendGrid or a custom backend.")
        false
    }

    private suspend fun sendViaSendGrid(
        toEmail: String,
        userName: String,
        lastCheckInText: String
    ): Boolean {
        return try {
            val subject = "Safety Alert: $userName Missed Check-In"
            val htmlBody = SendGridEmailService.buildAlertEmailHtml(userName, lastCheckInText)
            val textBody = buildPlainTextBody(userName, lastCheckInText)

            val result = SendGridEmailService.sendEmail(
                toEmail = toEmail,
                subject = subject,
                htmlBody = htmlBody,
                textBody = textBody
            )

            result.isSuccess.also { success ->
                if (success) {
                    Log.d(TAG, "Email sent via SendGrid to $toEmail")
                } else {
                    Log.e(TAG, "SendGrid email failed: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email via SendGrid", e)
            false
        }
    }

    private suspend fun sendViaBackendApi(
        toEmail: String,
        userName: String,
        lastCheckInText: String
    ): Boolean {
        return try {
            val apiService = AlertApiService.create()
            val request = AlertRequest(
                to = toEmail,
                subject = "Safety Alert: $userName Missed Check-In",
                body = buildPlainTextBody(userName, lastCheckInText),
                userName = userName
            )
            val response = apiService.sendAlertEmail(request)
            response.isSuccessful.also { success ->
                if (success) {
                    Log.d(TAG, "Email sent via backend API to $toEmail")
                } else {
                    Log.e(TAG, "Backend API email failed: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email via backend API", e)
            false
        }
    }

    private fun buildPlainTextBody(userName: String, lastCheckInText: String): String {
        return """
            SAFETY ALERT

            $userName has not checked in on the "Are You Alive?" safety check-in app.

            Last check-in: $lastCheckInText

            This could indicate that they may need assistance. Please try to contact them to ensure they are safe.

            ---
            This is an automated message from the "Are You Alive?" safety check-in app.
        """.trimIndent()
    }

    /**
     * Check if any email service is available
     */
    fun isEmailAvailable(): Boolean {
        return SendGridConfig.isConfigured || BackendConfig.isConfigured
    }
}
