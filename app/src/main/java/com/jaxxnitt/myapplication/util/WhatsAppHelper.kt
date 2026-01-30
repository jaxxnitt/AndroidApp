package com.jaxxnitt.myapplication.util

import android.util.Log
import com.jaxxnitt.myapplication.data.api.TwilioConfig
import com.jaxxnitt.myapplication.data.api.TwilioWhatsAppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WhatsAppHelper {

    private const val TAG = "WhatsAppHelper"

    /**
     * Send a WhatsApp message via Twilio WhatsApp API
     *
     * @param phoneNumber The recipient's phone number in E.164 format
     * @param message The message content
     * @return True if the message was sent successfully
     */
    suspend fun sendWhatsApp(phoneNumber: String, message: String): Boolean {
        if (!TwilioConfig.isWhatsAppConfigured) {
            Log.w(TAG, "Twilio WhatsApp is not configured")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val result = TwilioWhatsAppService.sendWhatsApp(phoneNumber, message)
                result.isSuccess.also { success ->
                    if (success) {
                        Log.d(TAG, "WhatsApp message sent to $phoneNumber")
                    } else {
                        Log.e(TAG, "WhatsApp message failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send WhatsApp message to $phoneNumber", e)
                false
            }
        }
    }

    /**
     * Check if WhatsApp messaging is available
     */
    fun isWhatsAppAvailable(): Boolean {
        return TwilioConfig.isWhatsAppConfigured
    }
}
