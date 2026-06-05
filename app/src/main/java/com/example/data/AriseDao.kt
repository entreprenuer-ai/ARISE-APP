package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AriseDao {

    // --- ALARM ENGINE ---
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): Alarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("UPDATE alarms SET isActive = :active WHERE id = :alarmId")
    suspend fun updateAlarmStatus(alarmId: Int, active: Boolean)


    // --- BUILT-IN CALENDAR ---
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getEventById(id: Int): CalendarEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Int)


    // --- GOALS & MISSIONS ---
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Int): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Delete
    suspend fun deleteGoal(goal: Goal)


    // --- SLEEP SYSTEM ---
    @Query("SELECT * FROM sleep_logs ORDER BY wakeTime DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog): Long

    @Delete
    suspend fun deleteSleepLog(log: SleepLog)


    // --- APP SETTINGS ---
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSetting>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)


    // --- HABITS & TRACKING ---
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)


    // --- HABIT COMPLETIONS ---
    @Query("SELECT * FROM habit_completions ORDER BY completionTimestamp DESC")
    fun getAllHabitCompletions(): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletion): Long

    @Delete
    suspend fun deleteHabitCompletion(completion: HabitCompletion)


    // --- ALARM HISTORY ---
    @Query("SELECT * FROM alarm_history ORDER BY triggeredTime DESC")
    fun getAllAlarmHistory(): Flow<List<AlarmHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmHistoryItem(item: AlarmHistoryItem): Long


    // --- ROUTINES ---
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Delete
    suspend fun deleteRoutine(routine: Routine)


    // --- CHALLENGES ---
    @Query("SELECT * FROM challenges ORDER BY id DESC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge): Long
}
