package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Alarm::class,
        CalendarEvent::class,
        Goal::class,
        SleepLog::class,
        AppSetting::class,
        Habit::class,
        HabitCompletion::class,
        AlarmHistoryItem::class,
        Routine::class,
        Challenge::class
    ],
    version = 4, // Increment version because of new sound fields on core Alarm!
    exportSchema = false
)
@TypeConverters(AriseTypeConverters::class)
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
                    .fallbackToDestructiveMigration() // Destructive migration is highly safe and appropriate here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
