package com.jaxxnitt.myapplication.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 1")
    fun getLastCheckIn(): Flow<CheckIn?>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastCheckInOnce(): CheckIn?

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE timestamp > :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentCheckIns(sinceTimestamp: Long): List<CheckIn>

    @Insert
    suspend fun insert(checkIn: CheckIn): Long

    @Update
    suspend fun update(checkIn: CheckIn)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()
}
