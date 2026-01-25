package com.jaxxnitt.myapplication.data.repository

import com.jaxxnitt.myapplication.data.database.CheckIn
import com.jaxxnitt.myapplication.data.database.CheckInDao
import kotlinx.coroutines.flow.Flow

class CheckInRepository(private val checkInDao: CheckInDao) {

    val lastCheckIn: Flow<CheckIn?> = checkInDao.getLastCheckIn()

    val allCheckIns: Flow<List<CheckIn>> = checkInDao.getAllCheckIns()

    suspend fun getLastCheckInOnce(): CheckIn? {
        return checkInDao.getLastCheckInOnce()
    }

    suspend fun checkIn(): Long {
        return checkInDao.insert(CheckIn())
    }

    suspend fun getRecentCheckIns(days: Int): List<CheckIn> {
        val sinceTimestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return checkInDao.getRecentCheckIns(sinceTimestamp)
    }

    suspend fun update(checkIn: CheckIn) {
        checkInDao.update(checkIn)
    }

    suspend fun clearHistory() {
        checkInDao.deleteAll()
    }
}
