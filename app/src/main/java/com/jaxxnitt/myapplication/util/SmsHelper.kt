package com.jaxxnitt.myapplication.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.jaxxnitt.myapplication.data.api.TwilioConfig
import com.jaxxnitt.myapplication.data.api.TwilioSmsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsHelper {

    private const val TAG = "SmsHelper"

    /**
     * Send SMS using the best available method:
     * 1. If Twilio is configured, use Twilio API (cloud-based, more reliable)
     * 2. Otherwise, use native Android SMS Manager (requires SEND_SMS permission)
     */
    suspend fun sendSmsSmart(context: Context, phoneNumber: String, message: String): Boolean {
        // Try Twilio first if configured (more reliable for background delivery)
        if (TwilioConfig.isConfigured) {
            val twilioResult = sendSmsTwilio(phoneNumber, message)
            if (twilioResult) {
                Log.d(TAG, "SMS sent via Twilio to $phoneNumber")
                return true
            }
            Log.w(TAG, "Twilio SMS failed, falling back to native SMS")
        }

        // Fall back to native SMS
        return sendSmsNative(context, phoneNumber, message)
    }

    /**
     * Send SMS via Twilio API
     */
    suspend fun sendSmsTwilio(phoneNumber: String, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val result = TwilioSmsService.sendSms(phoneNumber, message)
                result.isSuccess.also { success ->
                    if (!success) {
                        Log.e(TAG, "Twilio SMS failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SMS via Twilio", e)
                false
            }
        }
    }

    /**
     * Send SMS using native Android SmsManager
     */
    fun sendSmsNative(context: Context, phoneNumber: String, message: String): Boolean {
        if (!hasSmsPermission(context)) {
            Log.w(TAG, "SMS permission not granted")
            return false
        }

        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "SMS sent via native SmsManager to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            false
        }
    }

    /**
     * Legacy method for backwards compatibility
     */
    fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        return sendSmsNative(context, phoneNumber, message)
    }

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if any SMS method is available
     */
    fun isSmsAvailable(context: Context): Boolean {
        return TwilioConfig.isConfigured || hasSmsPermission(context)
    }
}
