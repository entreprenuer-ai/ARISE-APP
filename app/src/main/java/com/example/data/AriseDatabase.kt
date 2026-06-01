package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Alarm::class,
        CalendarEvent::class,
        Goal::class,
        SleepLog::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AriseDatabase : RoomDatabase() {

    abstract fun ariseDao(): AriseDao

    companion object {
        @Volatile
        private var INSTANCE: AriseDatabase? = null

        fun getDatabase(context: Context): AriseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AriseDatabase::class.java,
                    "arise_database"
                )
                    .fallbackToDestructiveMigration() // Simple on-device local database update handling
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
