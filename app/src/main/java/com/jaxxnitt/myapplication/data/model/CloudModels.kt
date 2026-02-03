package com.jaxxnitt.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserData(
    @DocumentId
    val id: String = "",
    val fullName: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

data class CloudContact(
    @DocumentId
    val id: String = "",
    val localId: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val isDeleted: Boolean = false
)

data class CloudCheckIn(
    @DocumentId
    val id: String = "",
    val localId: Long = 0,
    val timestamp: Long = 0,
    @ServerTimestamp
    val syncedAt: Timestamp? = null
)

data class LifeguardRelationship(
    @DocumentId
    val id: String = "",
    val guardianPhone: String = "",       // Phone of the lifeguard (emergency contact)
    val protectedUserId: String = "",     // UID of the person who added this emergency contact
    val protectedUserName: String = "",   // Name of the person who added this emergency contact
    val protectedUserPhone: String = "",  // Phone of the person who added this emergency contact
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

data class CloudSettings(
    val checkInHour: Int = 9,
    val checkInMinute: Int = 0,
    val gracePeriodHours: Int = 4,
    val checkInFrequencyDays: Int = 1,
    val isEnabled: Boolean = true,
    val userName: String = "User",
    val fullName: String = "",
    val profilePictureUri: String = "",
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
