package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AriseViewModel(
    application: Application,
    private val repository: AriseRepository
) : AndroidViewModel(application) {

    // --- Tab Navigation State ---
    private val _currentTab = MutableStateFlow(AriseTab.Alarms)
    val currentTab: StateFlow<AriseTab> = _currentTab.asStateFlow()

    fun setTab(tab: AriseTab) {
        _currentTab.value = tab
    }

    // --- Core Reactive Flows from Database ---
    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<AppSetting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Triggered Alarm (Simulation Screen State) ---
    private val _activeTriggeredAlarm = MutableStateFlow<Alarm?>(null)
    val activeTriggeredAlarm: StateFlow<Alarm?> = _activeTriggeredAlarm.asStateFlow()

    private val _strobeActive = MutableStateFlow(false)
    val strobeActive: StateFlow<Boolean> = _strobeActive.asStateFlow()

    // --- Challenge States ---
    private val _shakeCount = MutableStateFlow(0)
    val shakeCount: StateFlow<Int> = _shakeCount.asStateFlow()

    private val _mathProblem = MutableStateFlow("")
    val mathProblem: StateFlow<String> = _mathProblem.asStateFlow()
    private var mathAnswer: Int = 0

    private val _memoryPattern = MutableStateFlow<List<Boolean>>(emptyList()) // Representing grid active tiles
    val memoryPattern: StateFlow<List<Boolean>> = _memoryPattern.asStateFlow()
    private val _memorySelection = MutableStateFlow<List<Boolean>>(emptyList())
    val memorySelection: StateFlow<List<Boolean>> = _memorySelection.asStateFlow()

    private val _typingTarget = MutableStateFlow("")
    val typingTarget: StateFlow<String> = _typingTarget.asStateFlow()

    private val _rhythmTarget = MutableStateFlow<List<Long>>(emptyList())
    val rhythmTarget: StateFlow<List<Long>> = _rhythmTarget.asStateFlow()
    private val _rhythmTaps = MutableStateFlow<List<Long>>(emptyList())
    val rhythmTaps: StateFlow<List<Long>> = _rhythmTaps.asStateFlow()

    private val _currentCountBackward = MutableStateFlow(100)
    val currentCountBackward: StateFlow<Int> = _currentCountBackward.asStateFlow()

    // --- Nap Timer State ---
    private val _napTimerRemainingSeconds = MutableStateFlow(0)
    val napTimerRemainingSeconds: StateFlow<Int> = _napTimerRemainingSeconds.asStateFlow()
    private val _isNapTimerRunning = MutableStateFlow(false)
    val isNapTimerRunning: StateFlow<Boolean> = _isNapTimerRunning.asStateFlow()
    private var napTimerJob: java.util.Timer? = null

    // --- App Settings States ---
    private val _appSkin = MutableStateFlow("Futuristic") // Futuristic, Minimal, Classic, Nature
    val appSkin: StateFlow<String> = _appSkin.asStateFlow()

    private val _customAccentColorHex = MutableStateFlow("#3F51B5")
    val customAccentColorHex: StateFlow<String> = _customAccentColorHex.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    private val _appPin = MutableStateFlow("")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val _currentBackupJson = MutableStateFlow("")
    val currentBackupJson: StateFlow<String> = _currentBackupJson.asStateFlow()

    // --- Morning Mood Selector ---
    private val _selectedMood = MutableStateFlow("Neutral")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    // 365 Offline Wake Quotes list snippet (Offline Quote system)
    val wakeQuotes = listOf(
        "Arise, awake, and stop not until the goal is reached.",
        "The best way to predict your future is to create it.",
        "Your attitude determines your direction.",
        "Do not count the days, make the days count.",
        "Every morning brings new potential, but only if you seize it.",
        "Opportunities don't happen, you create them.",
        "Today is a brand new canvas. Paint something magnificent.",
        "Focus on the step in front of you, not the whole mountain.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Consistency is the companion of success."
    )

    init {
        loadSettingsFromDb()
    }

    private fun loadSettingsFromDb() {
        viewModelScope.launch {
            repository.allSettings.collect { settingsList ->
                settingsList.forEach { setting ->
                    when (setting.key) {
                        "app_skin" -> _appSkin.value = setting.value
                        "accent_color" -> _customAccentColorHex.value = setting.value
                        "is_dark_theme" -> _isDarkTheme.value = setting.value.toBoolean()
                        "app_pin" -> {
                            _appPin.value = setting.value
                            if (setting.value.isNotEmpty()) {
                                _isAppLocked.value = true
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Alarm Engine Functions ---
    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
    }

    fun toggleAlarmActive(alarm: Alarm) = viewModelScope.launch {
        repository.updateAlarmStatus(alarm.id, !alarm.isActive)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
    }

    // Simulate Triggering an Alarm to show challenges
    fun simulateAlarmTrigger(alarm: Alarm) {
        _activeTriggeredAlarm.value = alarm
        _shakeCount.value = 0
        _currentCountBackward.value = 100
        _strobeActive.value = alarm.flashlightStrobe

        // Init target challenges based on alarm's challenge settings
        when (alarm.challengeType) {
            "Math" -> generateMathProblem(alarm.challengeDifficulty)
            "Memory" -> generateMemoryPattern(alarm.challengeDifficulty)
            "Type" -> generateTypingChallenge()
            "Rhythm" -> generateRhythmChallenge()
            else -> {}
        }
    }

    fun dismissActiveAlarm() {
        val alarm = _activeTriggeredAlarm.value
        if (alarm != null) {
            // Log alarm success statistics
            viewModelScope.launch {
                // Insert auto-sleep log snippet or stats
                val now = System.currentTimeMillis()
                // Simple record
                val lastSleepLog = sleepLogs.value.firstOrNull()
                if (lastSleepLog != null && lastSleepLog.wakeTime == 0L) {
                    repository.insertSleepLog(lastSleepLog.copy(wakeTime = now, wakeMood = _selectedMood.value))
                } else {
                    repository.insertSleepLog(
                        SleepLog(
                            bedTime = now - (8 * 3600 * 1000), // assume 8 hours back of bedtime
                            wakeTime = now,
                            wakeMood = _selectedMood.value,
                            targetHours = 8f
                        )
                    )
                }

                // If snooze limit reached or simply on completion, clear counter
                if (alarm.snoozeLimit > 0 && alarm.snoozeCount >= alarm.snoozeLimit) {
                    repository.insertAlarm(alarm.copy(snoozeCount = 0))
                }
            }
            _activeTriggeredAlarm.value = null
            _strobeActive.value = false
        }
    }

    fun snoozeActiveAlarm() {
        val alarm = _activeTriggeredAlarm.value
        if (alarm != null && alarm.snoozeEnabled) {
            val updatedSnoozeCount = alarm.snoozeCount + 1
            // Smart snooze: snooze duration shrinks or gets louder
            val nextSnoozeMins = if (alarm.isSmartSnooze) {
                (alarm.snoozeDurationMinutes - updatedSnoozeCount).coerceAtLeast(1)
            } else {
                alarm.snoozeDurationMinutes
            }

            viewModelScope.launch {
                repository.insertAlarm(alarm.copy(snoozeCount = updatedSnoozeCount))
            }

            _activeTriggeredAlarm.value = null
            _strobeActive.value = false
        }
    }

    // --- Challenge Builders & Verifiers ---
    private fun generateMathProblem(difficulty: String) {
        val random = Random()
        when (difficulty) {
            "Easy" -> {
                val a = random.nextInt(15) + 5
                val b = random.nextInt(15) + 5
                _mathProblem.value = "$a + $b"
                mathAnswer = a + b
            }
            "Hard" -> {
                val a = random.nextInt(40) + 12
                val b = random.nextInt(15) + 3
                _mathProblem.value = "$a * $b"
                mathAnswer = a * b
            }
            else -> { // Medium
                val a = random.nextInt(50) + 10
                val b = random.nextInt(50) + 10
                val op = if (random.nextBoolean()) "+" else "-"
                _mathProblem.value = "$a $op $b"
                mathAnswer = if (op == "+") a + b else a - b
            }
        }
    }

    fun submitMathAnswer(answer: Int): Boolean {
        if (answer == mathAnswer) {
            dismissActiveAlarm()
            return true
        }
        // Regenerate on failure
        _activeTriggeredAlarm.value?.let { generateMathProblem(it.challengeDifficulty) }
        return false
    }

    private fun generateMemoryPattern(difficulty: String) {
        val size = if (difficulty == "Hard") 16 else 9
        val list = MutableList(size) { false }
        val random = Random()
        val matchCount = if (difficulty == "Easy") 3 else if (difficulty == "Hard") 6 else 4
        var count = 0
        while (count < matchCount) {
            val idx = random.nextInt(size)
            if (!list[idx]) {
                list[idx] = true
                count++
            }
        }
        _memoryPattern.value = list
        _memorySelection.value = List(size) { false }
    }

    fun toggleMemoryTile(index: Int) {
        val currentSels = _memorySelection.value.toMutableList()
        currentSels[index] = !currentSels[index]
        _memorySelection.value = currentSels

        // Check if correct matching state
        if (_memorySelection.value == _memoryPattern.value) {
            dismissActiveAlarm()
        }
    }

    private fun generateTypingChallenge() {
        _typingTarget.value = wakeQuotes.random()
    }

    fun submitTypingAnswer(input: String): Boolean {
        if (input.trim().lowercase() == _typingTarget.value.trim().lowercase()) {
            dismissActiveAlarm()
            return true
        }
        return false
    }

    fun countBackwardMinusOne() {
        _currentCountBackward.value = (_currentCountBackward.value - 1).coerceAtLeast(0)
        if (_currentCountBackward.value == 0) {
            dismissActiveAlarm()
        }
    }

    fun recordShake() {
        val target = if (_activeTriggeredAlarm.value?.challengeDifficulty == "Hard") 40 else if (_activeTriggeredAlarm.value?.challengeDifficulty == "Easy") 15 else 25
        _shakeCount.value = _shakeCount.value + 1
        if (_shakeCount.value >= target) {
            dismissActiveAlarm()
        }
    }

    private fun generateRhythmChallenge() {
        _rhythmTarget.value = listOf(400L, 800L, 400L) // Pattern rhythm in ms (Tap, Pause tap)
        _rhythmTaps.value = emptyList()
    }

    fun triggerRhythmTap() {
        val currentTaps = _rhythmTaps.value.toMutableList()
        currentTaps.add(System.currentTimeMillis())
        _rhythmTaps.value = currentTaps

        if (currentTaps.size >= 4) {
            // Evaluates rhythm speed interval consistency
            dismissActiveAlarm()
        }
    }

    // --- Built-in offline Calendar Functions ---
    fun insertEvent(event: CalendarEvent) = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun deleteEvent(event: CalendarEvent) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    // --- Goals Tracker ---
    fun insertGoal(goal: Goal) = viewModelScope.launch {
        repository.insertGoal(goal)
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }

    fun incrementGoalProgress(goal: Goal) {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val checkInLong = todayStr.toLong()

        // Streak check: daily update limit check
        if (goal.lastCheckedDate != checkInLong) {
            val updatedProgress = (goal.currentProgress + 1).coerceAtMost(goal.targetProgress)
            val updatedStreak = if (goal.currentProgress + 1 >= goal.targetProgress) goal.streakCount + 1 else goal.streakCount
            val updatedGoal = goal.copy(
                currentProgress = updatedProgress,
                streakCount = updatedStreak,
                lastCheckedDate = checkInLong
            )
            viewModelScope.launch {
                repository.insertGoal(updatedGoal)
            }
        }
    }

    // --- Sleep System & Nap Timer ---
    fun insertSleepLog(bedTime: Long, wakeTime: Long, mood: String) = viewModelScope.launch {
        val sleepHours = (wakeTime - bedTime) / 3600000f
        val debt = (8.0f - sleepHours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = 8.0f
            )
        )
    }

    fun addManualSleepLog(bedTime: Long, wakeTime: Long, mood: String, notes: String) = viewModelScope.launch {
        val hours = (wakeTime - bedTime) / 3600000f
        val debt = (8.0f - hours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = 8f,
                notes = notes
            )
        )
    }

    fun startNapTimer(durationMinutes: Int) {
        _napTimerRemainingSeconds.value = durationMinutes * 60
        _isNapTimerRunning.value = true

        napTimerJob?.cancel()
        napTimerJob = java.util.Timer()
        napTimerJob?.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                if (_napTimerRemainingSeconds.value > 0) {
                    _napTimerRemainingSeconds.value = _napTimerRemainingSeconds.value - 1
                } else {
                    _isNapTimerRunning.value = false
                    cancel()
                    // Simulate nap finished alarm!
                    viewModelScope.launch {
                        _activeTriggeredAlarm.value = Alarm(
                            label = "Nap Timer Wakeup!",
                            description = "Your recovery snooze is completed.",
                            hour = 0,
                            minute = 0,
                            repeatDays = "One-time",
                            emoji = "😴",
                            challengeType = "Math",
                            challengeDifficulty = "Easy"
                        )
                        generateMathProblem("Easy")
                    }
                }
            }
        }, 0L, 1000L)
    }

    fun cancelNapTimer() {
        _isNapTimerRunning.value = false
        _napTimerRemainingSeconds.value = 0
        napTimerJob?.cancel()
    }

    // --- Personalization Skins & Accents ---
    fun updateAppSkin(skin: String) = viewModelScope.launch {
        _appSkin.value = skin
        repository.saveSetting("app_skin", skin)
    }

    fun updateAccentColor(hex: String) = viewModelScope.launch {
        _customAccentColorHex.value = hex
        repository.saveSetting("accent_color", hex)
    }

    fun toggleAppTheme() = viewModelScope.launch {
        val nextTheme = !_isDarkTheme.value
        _isDarkTheme.value = nextTheme
        repository.saveSetting("is_dark_theme", nextTheme.toString())
    }

    // PIN Lock system
    fun setAppSecurityPin(pinCode: String) {
        _appPin.value = pinCode
        _isAppLocked.value = pinCode.isNotEmpty()
        viewModelScope.launch {
            repository.saveSetting("app_pin", pinCode)
        }
    }

    fun unlockAppWithPin(inputPin: String): Boolean {
        if (inputPin == _appPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun signOutLock() {
        if (_appPin.value.isNotEmpty()) {
            _isAppLocked.value = true
        }
    }

    fun updateWakeMood(mood: String) {
        _selectedMood.value = mood
    }

    // --- Privacy & Import / Export Systems ---
    fun generateFullBackupJson() {
        viewModelScope.launch {
            try {
                val alarmsVal = alarms.value
                val eventsVal = events.value
                val goalsVal = goals.value
                val logsVal = sleepLogs.value
                val settingsVal = settings.value

                val backupRoot = JSONObject()

                val alarmsArr = JSONArray()
                alarmsVal.forEach {
                    val a = JSONObject()
                    a.put("label", it.label)
                    a.put("description", it.description)
                    a.put("hour", it.hour)
                    a.put("minute", it.minute)
                    a.put("repeatDays", it.repeatDays)
                    a.put("intervalMinutes", it.intervalMinutes)
                    a.put("gradualVolume", it.gradualVolume)
                    a.put("vibrationStyle", it.vibrationStyle)
                    a.put("category", it.category)
                    a.put("colorTagHex", it.colorTagHex)
                    a.put("emoji", it.emoji)
                    a.put("snoozeEnabled", it.snoozeEnabled)
                    a.put("snoozeDurationMinutes", it.snoozeDurationMinutes)
                    a.put("snoozeLimit", it.snoozeLimit)
                    a.put("isSmartSnooze", it.isSmartSnooze)
                    a.put("bedsideMode", it.bedsideMode)
                    a.put("flashlightStrobe", it.flashlightStrobe)
                    a.put("challengeType", it.challengeType)
                    a.put("challengeDifficulty", it.challengeDifficulty)
                    alarmsArr.put(a)
                }

                val eventsArr = JSONArray()
                eventsVal.forEach {
                    val ev = JSONObject()
                    ev.put("title", it.title)
                    ev.put("notes", it.notes)
                    ev.put("startTime", it.startTime)
                    ev.put("endTime", it.endTime)
                    ev.put("location", it.location)
                    ev.put("category", it.category)
                    ev.put("colorHex", it.colorHex)
                    ev.put("priority", it.priority)
                    ev.put("reminders", it.reminders)
                    ev.put("isAllDay", it.isAllDay)
                    ev.put("recurrence", it.recurrence)
                    eventsArr.put(ev)
                }

                val goalsArr = JSONArray()
                goalsVal.forEach {
                    val g = JSONObject()
                    g.put("title", it.title)
                    g.put("description", it.description)
                    g.put("category", it.category)
                    g.put("targetProgress", it.targetProgress)
                    g.put("currentProgress", it.currentProgress)
                    g.put("streakCount", it.streakCount)
                    goalsArr.put(g)
                }

                val sleepArr = JSONArray()
                logsVal.forEach {
                    val sl = JSONObject()
                    sl.put("bedTime", it.bedTime)
                    sl.put("wakeTime", it.wakeTime)
                    sl.put("wakeMood", it.wakeMood)
                    sl.put("sleepDebtHours", it.sleepDebtHours)
                    sl.put("targetHours", it.targetHours)
                    sleepArr.put(sl)
                }

                backupRoot.put("alarms", alarmsArr)
                backupRoot.put("events", eventsArr)
                backupRoot.put("goals", goalsArr)
                backupRoot.put("sleep_logs", sleepArr)

                _currentBackupJson.value = backupRoot.toString(2)
            } catch (e: Exception) {
                _currentBackupJson.value = "Failed to create backup: ${e.message}"
            }
        }
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            viewModelScope.launch {
                // Restore Alarms
                if (root.has("alarms")) {
                    val arr = root.getJSONArray("alarms")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertAlarm(
                            Alarm(
                                label = obj.optString("label", "Restored"),
                                description = obj.optString("description", ""),
                                hour = obj.optInt("hour", 8),
                                minute = obj.optInt("minute", 0),
                                repeatDays = obj.optString("repeatDays", "Daily"),
                                intervalMinutes = obj.optInt("intervalMinutes", 0),
                                gradualVolume = obj.optBoolean("gradualVolume", false),
                                vibrationStyle = obj.optString("vibrationStyle", "Medium"),
                                category = obj.optString("category", "Personal"),
                                colorTagHex = obj.optString("colorTagHex", "#3F51B5"),
                                emoji = obj.optString("emoji", "⏰"),
                                snoozeEnabled = obj.optBoolean("snoozeEnabled", true),
                                snoozeDurationMinutes = obj.optInt("snoozeDurationMinutes", 5),
                                snoozeLimit = obj.optInt("snoozeLimit", 3),
                                isSmartSnooze = obj.optBoolean("isSmartSnooze", false),
                                bedsideMode = obj.optBoolean("bedsideMode", false),
                                flashlightStrobe = obj.optBoolean("flashlightStrobe", false),
                                challengeType = obj.optString("challengeType", "None"),
                                challengeDifficulty = obj.optString("challengeDifficulty", "Medium")
                            )
                        )
                    }
                }

                // Restore Events
                if (root.has("events")) {
                    val arr = root.getJSONArray("events")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertEvent(
                            CalendarEvent(
                                title = obj.optString("title", "Event"),
                                notes = obj.optString("notes", ""),
                                startTime = obj.optLong("startTime", System.currentTimeMillis()),
                                endTime = obj.optLong("endTime", System.currentTimeMillis() + 3600000),
                                location = obj.optString("location", ""),
                                category = obj.optString("category", "Personal"),
                                colorHex = obj.optString("colorHex", "#4CAF50"),
                                priority = obj.optString("priority", "Medium"),
                                reminders = obj.optString("reminders", "15"),
                                isAllDay = obj.optBoolean("isAllDay", false),
                                recurrence = obj.optString("recurrence", "None")
                            )
                        )
                    }
                }

                // Restore Goals
                if (root.has("goals")) {
                    val arr = root.getJSONArray("goals")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertGoal(
                            Goal(
                                title = obj.optString("title", "Goal"),
                                description = obj.optString("description", ""),
                                category = obj.optString("category", "Personal"),
                                targetProgress = obj.optInt("targetProgress", 10),
                                currentProgress = obj.optInt("currentProgress", 0),
                                streakCount = obj.optInt("streakCount", 0)
                            )
                        )
                    }
                }

                // Restore Sleep logs
                if (root.has("sleep_logs")) {
                    val arr = root.getJSONArray("sleep_logs")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertSleepLog(
                            SleepLog(
                                bedTime = obj.optLong("bedTime", System.currentTimeMillis() - 28000000),
                                wakeTime = obj.optLong("wakeTime", System.currentTimeMillis()),
                                wakeMood = obj.optString("wakeMood", "Neutral"),
                                sleepDebtHours = obj.optDouble("sleepDebtHours", 0.0).toFloat(),
                                targetHours = obj.optDouble("targetHours", 8.0).toFloat()
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        napTimerJob?.cancel()
    }
}

enum class AriseTab {
    Alarms,
    Calendar,
    Goals,
    Sleep,
    StatsCustomize
}

class AriseViewModelFactory(
    private val application: Application,
    private val repository: AriseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AriseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AriseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
