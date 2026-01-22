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

interface AlertApiService {

    @POST("api/send-alert-email")
    suspend fun sendAlertEmail(@Body request: AlertRequest): Response<AlertResponse>

    companion object {
        // TODO: Replace with your actual backend URL
        private const val BASE_URL = "https://your-backend-api.com/"

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
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(AlertApiService::class.java)
        }
    }
}
