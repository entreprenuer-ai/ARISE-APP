package com.example.data

import kotlinx.coroutines.flow.Flow

class AriseRepository(private val ariseDao: AriseDao) {

    // --- ALARM ENGINE ---
    val allAlarms: Flow<List<Alarm>> = ariseDao.getAllAlarms()

    suspend fun getAlarmById(id: Int): Alarm? = ariseDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: Alarm): Long = ariseDao.insertAlarm(alarm)

    suspend fun deleteAlarm(alarm: Alarm) = ariseDao.deleteAlarm(alarm)

    suspend fun updateAlarmStatus(alarmId: Int, active: Boolean) = 
        ariseDao.updateAlarmStatus(alarmId, active)


    // --- BUILT-IN CALENDAR ---
    val allEvents: Flow<List<CalendarEvent>> = ariseDao.getAllEvents()

    suspend fun getEventById(id: Int): CalendarEvent? = ariseDao.getEventById(id)

    suspend fun insertEvent(event: CalendarEvent): Long = ariseDao.insertEvent(event)

    suspend fun deleteEvent(event: CalendarEvent) = ariseDao.deleteEvent(event)

    suspend fun deleteEventById(id: Int) = ariseDao.deleteEventById(id)


    // --- GOALS & MISSIONS ---
    val allGoals: Flow<List<Goal>> = ariseDao.getAllGoals()

    suspend fun getGoalById(id: Int): Goal? = ariseDao.getGoalById(id)

    suspend fun insertGoal(goal: Goal): Long = ariseDao.insertGoal(goal)

    suspend fun deleteGoal(goal: Goal) = ariseDao.deleteGoal(goal)


    // --- SLEEP SYSTEM ---
    val allSleepLogs: Flow<List<SleepLog>> = ariseDao.getAllSleepLogs()

    suspend fun insertSleepLog(log: SleepLog): Long = ariseDao.insertSleepLog(log)

    suspend fun deleteSleepLog(log: SleepLog) = ariseDao.deleteSleepLog(log)


    // --- APP SETTINGS ---
    val allSettings: Flow<List<AppSetting>> = ariseDao.getAllSettings()

    suspend fun getSetting(key: String): String? = ariseDao.getSettingValue(key)

    suspend fun saveSetting(key: String, value: String) = 
        ariseDao.insertSetting(AppSetting(key, value))

    suspend fun deleteSetting(key: String) = ariseDao.deleteSetting(key)


    // --- HABITS & TRACKING ---
    val allHabits: Flow<List<Habit>> = ariseDao.getAllHabits()

    suspend fun getHabitById(id: Int): Habit? = ariseDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = ariseDao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) = ariseDao.deleteHabit(habit)


    // --- HABIT COMPLETIONS ---
    val allHabitCompletions: Flow<List<HabitCompletion>> = ariseDao.getAllHabitCompletions()

    suspend fun insertHabitCompletion(completion: HabitCompletion): Long = 
        ariseDao.insertHabitCompletion(completion)

    suspend fun deleteHabitCompletion(completion: HabitCompletion) = 
        ariseDao.deleteHabitCompletion(completion)


    // --- ALARM HISTORY ---
    val allAlarmHistory: Flow<List<AlarmHistoryItem>> = ariseDao.getAllAlarmHistory()

    suspend fun insertAlarmHistoryItem(item: AlarmHistoryItem): Long = 
        ariseDao.insertAlarmHistoryItem(item)


    // --- ROUTINES ---
    val allRoutines: Flow<List<Routine>> = ariseDao.getAllRoutines()

    suspend fun insertRoutine(routine: Routine): Long = ariseDao.insertRoutine(routine)

    suspend fun deleteRoutine(routine: Routine) = ariseDao.deleteRoutine(routine)


    // --- CHALLENGES ---
    val allChallenges: Flow<List<Challenge>> = ariseDao.getAllChallenges()

    suspend fun insertChallenge(challenge: Challenge): Long = ariseDao.insertChallenge(challenge)
}
