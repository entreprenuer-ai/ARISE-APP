package com.example.features.alarms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.Alarm
import com.example.core.database.AriseRepository
import com.example.features.alarms.domain.usecase.CreateAlarmUseCase
import com.example.features.alarms.domain.usecase.DeleteAlarmUseCase
import com.example.features.alarms.domain.usecase.ToggleAlarmActiveUseCase
import com.example.features.alarms.domain.usecase.UpdateAlarmUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Random

class AlarmViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val createAlarmUseCase = CreateAlarmUseCase(repository)
    private val deleteAlarmUseCase = DeleteAlarmUseCase(repository)
    private val toggleAlarmActiveUseCase = ToggleAlarmActiveUseCase(repository)
    private val updateAlarmUseCase = UpdateAlarmUseCase(repository)

    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _memoryPattern = MutableStateFlow<List<Boolean>>(emptyList())
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

    private val _selectedWakeMood = MutableStateFlow("Neutral")
    val selectedWakeMood: StateFlow<String> = _selectedWakeMood.asStateFlow()

    fun setSelectedWakeMood(mood: String) {
        _selectedWakeMood.value = mood
    }

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

    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        createAlarmUseCase(alarm)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        deleteAlarmUseCase(alarm)
    }

    fun toggleAlarmActive(alarm: Alarm) = viewModelScope.launch {
        toggleAlarmActiveUseCase(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        updateAlarmUseCase(alarm)
    }

    // Set alarm trigger simulation
    fun simulateAlarmTrigger(alarm: Alarm) {
        _activeTriggeredAlarm.value = alarm
        _shakeCount.value = 0
        _currentCountBackward.value = 100
        _strobeActive.value = alarm.flashlightStrobe

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
            viewModelScope.launch {
                val now = System.currentTimeMillis()
                val wakeMood = _selectedWakeMood.value
                try {
                    val logs = repository.allSleepLogs.first()
                    val openLog = logs.firstOrNull { it.wakeTime == 0L }
                    if (openLog != null) {
                        val hours = (now - openLog.bedTime) / 3600000f
                        val debt = (openLog.targetHours - hours).coerceAtLeast(0f)
                        repository.insertSleepLog(
                            openLog.copy(
                                wakeTime = now,
                                wakeMood = wakeMood,
                                sleepDebtHours = debt
                            )
                        )
                    } else {
                        val eightHoursAgo = now - 8 * 3600 * 1000L
                        val targetStr = repository.getSetting("sleep_target_hours") ?: "8.0"
                        val customTarget = targetStr.toFloatOrNull() ?: 8.0f
                        val sleptHours = 8.0f
                        val debt = (customTarget - sleptHours).coerceAtLeast(0f)
                        repository.insertSleepLog(
                            com.example.core.database.SleepLog(
                                bedTime = eightHoursAgo,
                                wakeTime = now,
                                wakeMood = wakeMood,
                                sleepDebtHours = debt,
                                targetHours = customTarget,
                                notes = "Auto-logged on alarm dismiss"
                            )
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AlarmViewModel", "Failed to sync wake mood to sleep logs", e)
                }

                // If snooze limit reached or simple completion
                if (alarm.snoozeLimit > 0 && alarm.snoozeCount >= alarm.snoozeLimit) {
                    repository.insertAlarm(alarm.copy(snoozeCount = 0))
                }
            }
            _activeTriggeredAlarm.value = null
            _strobeActive.value = false
            _selectedWakeMood.value = "Neutral"
        }
    }

    fun snoozeActiveAlarm() {
        val alarm = _activeTriggeredAlarm.value
        if (alarm != null && alarm.snoozeEnabled) {
            if (alarm.snoozeLimit > 0 && alarm.snoozeCount >= alarm.snoozeLimit) {
                return // Locked! Snooze limit reached.
            }
            val updatedSnoozeCount = alarm.snoozeCount + 1
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
        _rhythmTarget.value = listOf(400L, 800L, 400L)
        _rhythmTaps.value = emptyList()
    }

    fun triggerRhythmTap() {
        val currentTaps = _rhythmTaps.value.toMutableList()
        currentTaps.add(System.currentTimeMillis())
        _rhythmTaps.value = currentTaps

        if (currentTaps.size >= 4) {
            dismissActiveAlarm()
        }
    }
}
