package com.jaxxnitt.myapplication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    // Cloud sync tracking fields
    val cloudId: String? = null,
    val syncedAt: Long? = null
)
