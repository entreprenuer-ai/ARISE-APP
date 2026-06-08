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
        Challenge::class,
        SleepSession::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(AriseTypeConverters::class)
abstract class AriseDatabase : RoomDatabase() {

    abstract fun ariseDao(): AriseDao
    abstract fun sleepSessionDao(): SleepSessionDao

    fun checkpoint() {
        try {
            val query = androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")
            val cursor = openHelper.writableDatabase.query(query)
            try {
                if (cursor.moveToFirst()) {
                    val busy = cursor.getInt(0)
                    val log = cursor.getInt(1)
                    val checkpointed = cursor.getInt(2)
                    android.util.Log.d("AriseDatabase", "WAL checkpoint completed: busy=$busy, log=$log, checkpointed=$checkpointed")
                }
            } finally {
                cursor.close()
            }
        } catch (e: Exception) {
            android.util.Log.e("AriseDatabase", "Error executing wal_checkpoint", e)
        }
    }

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
