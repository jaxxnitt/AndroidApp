package com.jaxxnitt.myapplication.data.api

import com.jaxxnitt.myapplication.BuildConfig
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
 * 4. Add credentials to local.properties (not checked into version control)
 */
object TwilioConfig {
    // Credentials loaded from local.properties via BuildConfig
    val ACCOUNT_SID: String = BuildConfig.TWILIO_ACCOUNT_SID
    val AUTH_TOKEN: String = BuildConfig.TWILIO_AUTH_TOKEN
    val FROM_PHONE_NUMBER: String = BuildConfig.TWILIO_FROM_PHONE_NUMBER
    val WHATSAPP_FROM_NUMBER: String = BuildConfig.TWILIO_WHATSAPP_FROM_NUMBER

    val isConfigured: Boolean
        get() = ACCOUNT_SID.isNotBlank() &&
                AUTH_TOKEN.isNotBlank() &&
                FROM_PHONE_NUMBER.isNotBlank()

    val isWhatsAppConfigured: Boolean
        get() = ACCOUNT_SID.isNotBlank() &&
                AUTH_TOKEN.isNotBlank() &&
                WHATSAPP_FROM_NUMBER.isNotBlank()
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

/**
 * Twilio WhatsApp Service for sending WhatsApp messages via Twilio API
 *
 * Setup instructions:
 * 1. Join the Twilio WhatsApp Sandbox by sending "join <your-sandbox-keyword>"
 *    to +14155238886 on WhatsApp
 * 2. Each recipient must also join the sandbox before they can receive messages
 * 3. For production, apply for a WhatsApp Business Profile via Twilio Console
 */
object TwilioWhatsAppService {

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
     * Send a WhatsApp message via Twilio API
     *
     * @param toPhoneNumber The recipient's phone number in E.164 format (e.g., +919150287153)
     * @param message The WhatsApp message content
     * @return Result with TwilioSmsResponse on success
     */
    suspend fun sendWhatsApp(toPhoneNumber: String, message: String): Result<TwilioSmsResponse> {
        if (!TwilioConfig.isWhatsAppConfigured) {
            return Result.failure(IllegalStateException("Twilio WhatsApp is not configured. Please update TwilioConfig with your credentials."))
        }

        return try {
            val url = "https://api.twilio.com/2010-04-01/Accounts/${TwilioConfig.ACCOUNT_SID}/Messages.json"

            val formattedTo = formatWhatsAppNumber(toPhoneNumber)
            val formattedFrom = "whatsapp:${TwilioConfig.WHATSAPP_FROM_NUMBER}"

            val formBody = "To=$formattedTo&From=$formattedFrom&Body=$message"

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
                Result.failure(Exception("Twilio WhatsApp API error: ${response.code} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Format phone number to WhatsApp format: whatsapp:+E.164
     */
    private fun formatWhatsAppNumber(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        val e164 = if (cleaned.startsWith("+")) cleaned else "+$cleaned"
        return "whatsapp:$e164"
    }
}
