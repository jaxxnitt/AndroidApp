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

    suspend fun clearHistory() {
        checkInDao.deleteAll()
    }
}
