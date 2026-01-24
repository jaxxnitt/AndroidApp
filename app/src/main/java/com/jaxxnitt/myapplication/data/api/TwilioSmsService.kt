package com.jaxxnitt.myapplication.data.api

import kotlinx.serialization.Serializable
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Twilio SMS Service for sending SMS messages via Twilio API
 *
 * Setup instructions:
 * 1. Create a Twilio account at https://www.twilio.com/
 * 2. Get your Account SID and Auth Token from the Twilio Console
 * 3. Purchase a phone number from Twilio
 * 4. Update the configuration in TwilioConfig object below
 */
object TwilioConfig {
    // TODO: Replace these with your actual Twilio credentials
    // For production, store these securely (e.g., in BuildConfig or encrypted storage)
    const val ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID"
    const val AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN"
    const val FROM_PHONE_NUMBER = "+1234567890" // Your Twilio phone number

    val isConfigured: Boolean
        get() = ACCOUNT_SID != "YOUR_TWILIO_ACCOUNT_SID" &&
                AUTH_TOKEN != "YOUR_TWILIO_AUTH_TOKEN" &&
                FROM_PHONE_NUMBER != "+1234567890"
}

@Serializable
data class TwilioSmsRequest(
    val to: String,
    val from: String,
    val body: String
)

@Serializable
data class TwilioSmsResponse(
    val sid: String? = null,
    val status: String? = null,
    val error_code: Int? = null,
    val error_message: String? = null
)

object TwilioSmsService {

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
     * Send an SMS message via Twilio API
     *
     * @param toPhoneNumber The recipient's phone number in E.164 format (e.g., +1234567890)
     * @param message The SMS message content
     * @return True if the message was sent successfully, false otherwise
     */
    suspend fun sendSms(toPhoneNumber: String, message: String): Result<TwilioSmsResponse> {
        if (!TwilioConfig.isConfigured) {
            return Result.failure(IllegalStateException("Twilio is not configured. Please update TwilioConfig with your credentials."))
        }

        return try {
            val url = "https://api.twilio.com/2010-04-01/Accounts/${TwilioConfig.ACCOUNT_SID}/Messages.json"

            val formBody = "To=${formatPhoneNumber(toPhoneNumber)}&From=${TwilioConfig.FROM_PHONE_NUMBER}&Body=${message}"

            val request = Request.Builder()
                .url(url)
                .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .header("Authorization", Credentials.basic(TwilioConfig.ACCOUNT_SID, TwilioConfig.AUTH_TOKEN))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(TwilioSmsResponse(status = "sent"))
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Result.failure(Exception("Twilio API error: ${response.code} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Format phone number to E.164 format if needed
     */
    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        return if (cleaned.startsWith("+")) cleaned else "+1$cleaned"
    }
}
