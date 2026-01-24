package com.jaxxnitt.myapplication.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * SendGrid Email Service for sending emails directly via SendGrid API
 *
 * Setup instructions:
 * 1. Create a SendGrid account at https://sendgrid.com/
 * 2. Generate an API key in Settings > API Keys
 * 3. Verify a sender identity (email address or domain)
 * 4. Update the configuration in SendGridConfig object below
 */
object SendGridConfig {
    // TODO: Replace with your actual SendGrid API key
    // For production, store this securely (e.g., in BuildConfig or encrypted storage)
    const val API_KEY = "YOUR_SENDGRID_API_KEY"
    const val FROM_EMAIL = "alerts@yourdomain.com"
    const val FROM_NAME = "Are You Alive? Safety Alert"

    val isConfigured: Boolean
        get() = API_KEY != "YOUR_SENDGRID_API_KEY" &&
                FROM_EMAIL != "alerts@yourdomain.com"
}

@Serializable
data class SendGridMailRequest(
    val personalizations: List<SendGridPersonalization>,
    val from: SendGridEmailAddress,
    val subject: String,
    val content: List<SendGridContent>
)

@Serializable
data class SendGridPersonalization(
    val to: List<SendGridEmailAddress>
)

@Serializable
data class SendGridEmailAddress(
    val email: String,
    val name: String? = null
)

@Serializable
data class SendGridContent(
    val type: String,
    val value: String
)

object SendGridEmailService {

    private const val SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Send an email via SendGrid API
     *
     * @param toEmail Recipient email address
     * @param toName Recipient name (optional)
     * @param subject Email subject
     * @param htmlBody HTML content of the email
     * @param textBody Plain text content (optional)
     * @return Result indicating success or failure
     */
    suspend fun sendEmail(
        toEmail: String,
        toName: String? = null,
        subject: String,
        htmlBody: String,
        textBody: String? = null
    ): Result<Boolean> {
        if (!SendGridConfig.isConfigured) {
            return Result.failure(IllegalStateException("SendGrid is not configured. Please update SendGridConfig with your API key."))
        }

        return try {
            val content = mutableListOf<SendGridContent>()
            textBody?.let { content.add(SendGridContent("text/plain", it)) }
            content.add(SendGridContent("text/html", htmlBody))

            val mailRequest = SendGridMailRequest(
                personalizations = listOf(
                    SendGridPersonalization(
                        to = listOf(SendGridEmailAddress(email = toEmail, name = toName))
                    )
                ),
                from = SendGridEmailAddress(
                    email = SendGridConfig.FROM_EMAIL,
                    name = SendGridConfig.FROM_NAME
                ),
                subject = subject,
                content = content
            )

            val requestBody = json.encodeToString(mailRequest)

            val request = Request.Builder()
                .url(SENDGRID_API_URL)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Bearer ${SendGridConfig.API_KEY}")
                .header("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            // SendGrid returns 202 for successful acceptance
            if (response.code == 202 || response.isSuccessful) {
                Result.success(true)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Result.failure(Exception("SendGrid API error: ${response.code} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Build a nicely formatted HTML email for safety alerts
     */
    fun buildAlertEmailHtml(userName: String, lastCheckInText: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .alert-box {
                        background: linear-gradient(135deg, #FF6B6B 0%, #EE5A5A 100%);
                        color: white;
                        padding: 30px;
                        border-radius: 16px;
                        text-align: center;
                        margin-bottom: 24px;
                    }
                    .alert-icon {
                        font-size: 48px;
                        margin-bottom: 16px;
                    }
                    .alert-title {
                        font-size: 24px;
                        font-weight: bold;
                        margin: 0 0 8px 0;
                    }
                    .alert-subtitle {
                        font-size: 16px;
                        opacity: 0.9;
                        margin: 0;
                    }
                    .info-card {
                        background: #f8f9fa;
                        border-radius: 12px;
                        padding: 24px;
                        margin-bottom: 24px;
                    }
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        padding: 12px 0;
                        border-bottom: 1px solid #e9ecef;
                    }
                    .info-row:last-child {
                        border-bottom: none;
                    }
                    .info-label {
                        color: #6c757d;
                        font-size: 14px;
                    }
                    .info-value {
                        font-weight: 600;
                        color: #212529;
                    }
                    .action-text {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 16px;
                        border-radius: 8px;
                        margin-bottom: 24px;
                    }
                    .action-text p {
                        margin: 0;
                        color: #856404;
                    }
                    .footer {
                        text-align: center;
                        color: #6c757d;
                        font-size: 12px;
                        padding-top: 24px;
                        border-top: 1px solid #e9ecef;
                    }
                </style>
            </head>
            <body>
                <div class="alert-box">
                    <div class="alert-icon">⚠️</div>
                    <h1 class="alert-title">Safety Alert</h1>
                    <p class="alert-subtitle">Missed Check-In Notification</p>
                </div>

                <div class="info-card">
                    <div class="info-row">
                        <span class="info-label">Person</span>
                        <span class="info-value">$userName</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Last Check-In</span>
                        <span class="info-value">$lastCheckInText</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Status</span>
                        <span class="info-value" style="color: #dc3545;">Overdue</span>
                    </div>
                </div>

                <div class="action-text">
                    <p><strong>Action Required:</strong> $userName has not checked in on the "Are You Alive?" safety app.
                    This could indicate they may need assistance. Please try to contact them to ensure they are safe.</p>
                </div>

                <div class="footer">
                    <p>This is an automated message from the "Are You Alive?" safety check-in app.</p>
                    <p>You are receiving this because you were listed as an emergency contact.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
