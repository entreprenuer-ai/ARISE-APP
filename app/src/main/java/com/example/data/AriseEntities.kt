package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    val repeatDays: String, // Comma-separated: "Mon,Tue,Wed,Thu,Fri" or "Daily" or "One-time"
    val isActive: Boolean = true,
    val intervalMinutes: Int = 0, // 0 if none, e.g. interval alarms every X minutes
    val gradualVolume: Boolean = false, // Crescendo wake
    val vibrationStyle: String = "Medium", // "Soft", "Medium", "Intense", "Custom"
    val category: String = "Personal", // "Work", "Personal", "Health", "Sleep", "Custom"
    val colorTagHex: String = "#3F51B5", // Color tag per alarm
    val emoji: String = "⏰", // Emoji label
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 5,
    val snoozeLimit: Int = 3,
    val snoozeCount: Int = 0,
    val isSmartSnooze: Boolean = false, // Snooze gets louder or shortens next time
    val bedsideMode: Boolean = false, // Screen dims, alarm stays active
    val flashlightStrobe: Boolean = false, // Strobe on alarm for hearing impaired
    val skipNextOccurrence: Boolean = false, // Skip next occurrence only
    val disabledUntilDate: Long = 0L, // Pause alarm for X days (timestamp)
    val expiryDate: Long = 0L, // Auto-delete after a date (timestamp)
    val backupAlarmId: Int = 0, // Backup if primary fails
    val passwordProtect: Boolean = false, // Password protect alarm settings
    val passwordPin: String = "",
    val challengeType: String = "None", // "None", "Math", "Memory", "Shake", "Type", "Rhythm", "Tap"
    val challengeDifficulty: String = "Medium" // "Easy", "Medium", "Hard"
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String = "",
    val startTime: Long, // Epoch millisecond
    val endTime: Long, // Epoch millisecond
    val location: String = "",
    val category: String = "Personal", // "Work", "Personal", "Health", "Family", "Finance"
    val colorHex: String = "#4CAF50",
    val priority: String = "Medium", // "High", "Medium", "Low"
    val reminders: String = "15", // Comma-separated: "15,30,60" minutes before
    val isAllDay: Boolean = false,
    val recurrence: String = "None", // "None", "Daily", "Weekly", "Monthly", "Yearly"
    val linkedAlarmId: Int = 0, // Fusion feature: Event -> Auto alarm suggestion or link
    val prepTimeMinutes: Int = 0, // Prep time alarm before event
    val travelTimeMinutes: Int = 0 // Travel buffer alarm (commute)
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal", // "Fitness", "Career", "Learning", "Health", "Finance"
    val targetProgress: Int = 10, // e.g. 10 days, 10 milestones
    val currentProgress: Int = 0,
    val streakCount: Int = 0,
    val lastCheckedDate: Long = 0L, // Day of last progress increment to prevent double check-ins per day
    val habitEnabled: Boolean = false, // Habit tracker integrated with alarms
    val deadlineDate: Long = 0L,
    val linkedAlarmId: Int = 0,
    val linkedEventId: Int = 0
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bedTime: Long, // Timestamp
    val wakeTime: Long, // Timestamp
    val isManualLog: Boolean = true,
    val wakeMood: String = "Neutral", // "Good", "Neutral", "Tired"
    val sleepDebtHours: Float = 0f, // Target - Actual sleep hours
    val targetHours: Float = 8f,
    val notes: String = ""
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
