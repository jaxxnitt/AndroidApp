package com.jaxxnitt.myapplication.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Backend Configuration for custom email service
 *
 * If you have your own backend server that handles email sending,
 * configure the URL here. The backend should accept POST requests
 * to /api/send-alert-email with the AlertRequest body.
 *
 * Example backend implementations:
 * - Node.js with Nodemailer
 * - Python with Flask + SendGrid/SES
 * - Any serverless function (AWS Lambda, Vercel, etc.)
 */
object BackendConfig {
    // TODO: Replace with your actual backend URL
    // Example: "https://my-app-backend.vercel.app/"
    const val BASE_URL = "https://your-backend-api.com/"

    val isConfigured: Boolean
        get() = BASE_URL != "https://your-backend-api.com/" &&
                BASE_URL.startsWith("https://")
}

interface AlertApiService {

    @POST("api/send-alert-email")
    suspend fun sendAlertEmail(@Body request: AlertRequest): Response<AlertResponse>

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun create(): AlertApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BackendConfig.BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(AlertApiService::class.java)
        }
    }
}
