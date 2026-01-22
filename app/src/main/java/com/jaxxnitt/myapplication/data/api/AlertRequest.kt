package com.jaxxnitt.myapplication.data.api

import kotlinx.serialization.Serializable

@Serializable
data class AlertRequest(
    val to: String,
    val subject: String,
    val body: String,
    val userName: String
)

@Serializable
data class AlertResponse(
    val success: Boolean,
    val message: String? = null
)
