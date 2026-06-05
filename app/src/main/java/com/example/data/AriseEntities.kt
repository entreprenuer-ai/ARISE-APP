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
    val challengeDifficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val soundPath: String? = null,
    val soundName: String? = null,
    val soundStartMs: Int = 0,
    val soundEndMs: Int = 30
)

@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Alarm::class,
            parentColumns = ["id"],
            childColumns = ["linkedAlarmId"],
            onDelete = androidx.room.ForeignKey.SET_NULL
        )
    ],
    indices = [androidx.room.Index("linkedAlarmId")]
)
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
    val linkedAlarmId: Int? = null, // Fusion feature: Event -> Auto alarm suggestion or link
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

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val frequency: String = "Daily", // "Daily", "Weekly", "Custom"
    val targetCount: Int = 1,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastCompletedTimestamp: Long = 0L,
    val isArchived: Boolean = false,
    val syncStatus: String = "PENDING" // "PENDING", "SYNCED", "FAILED"
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val completionTimestamp: Long,
    val notes: String = "",
    val syncStatus: String = "PENDING"
)

@Entity(tableName = "alarm_history")
data class AlarmHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alarmId: Int,
    val alarmLabel: String,
    val triggeredTime: Long,
    val dismissedTime: Long,
    val responseTimeSeconds: Int,
    val challengeCompleted: String = "None", // "Math", "Memory", "Shake", "None"
    val wakeMood: String = "Neutral",
    val syncStatus: String = "PENDING"
)

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val timeOfDay: String = "Morning", // "Morning", "Evening"
    val alarmId: Int? = null, // Linked alarm
    val stepsJson: String = "[]", // Serialized list of steps/subtasks
    val isActive: Boolean = true,
    val syncStatus: String = "PENDING"
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Math", "Shake", "Memory", "Rhythm"
    val difficulty: String = "Medium",
    val description: String = "",
    val configJson: String = "{}", // challenge parameters
    val isUnlocked: Boolean = true,
    val highHighScore: Int = 0,
    val syncStatus: String = "PENDING"
)
