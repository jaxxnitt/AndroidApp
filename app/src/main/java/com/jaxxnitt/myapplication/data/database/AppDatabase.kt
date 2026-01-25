package com.jaxxnitt.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Contact::class, CheckIn::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to contacts table for cloud sync
                database.execSQL("ALTER TABLE contacts ADD COLUMN cloudId TEXT")
                database.execSQL("ALTER TABLE contacts ADD COLUMN lastSyncedAt INTEGER")
                database.execSQL("ALTER TABLE contacts ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")

                // Add new columns to check_ins table for cloud sync
                database.execSQL("ALTER TABLE check_ins ADD COLUMN cloudId TEXT")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN syncedAt INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "are_you_dead_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
