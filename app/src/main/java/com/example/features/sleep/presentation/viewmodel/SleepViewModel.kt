package com.example.features.sleep.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.Alarm
import com.example.core.database.SleepLog
import com.example.core.database.AriseRepository
import com.example.features.sleep.domain.usecase.AddSleepLogUseCase
import com.example.features.sleep.domain.usecase.AddManualSleepLogUseCase
import com.example.features.sleep.domain.usecase.DeleteSleepLogUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SleepViewModel(
    private val repository: AriseRepository,
    private val onNapCompletedSimulatedAlarm: (Alarm) -> Unit // callback to trigger a simulated alarm when nap is finished!
) : ViewModel() {

    private val addSleepLogUseCase = AddSleepLogUseCase(repository)
    private val addManualSleepLogUseCase = AddManualSleepLogUseCase(repository)
    private val deleteSleepLogUseCase = DeleteSleepLogUseCase(repository)

    val sleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _napTimerRemainingSeconds = MutableStateFlow(0)
    val napTimerRemainingSeconds: StateFlow<Int> = _napTimerRemainingSeconds.asStateFlow()

    private val _isNapTimerRunning = MutableStateFlow(false)
    val isNapTimerRunning: StateFlow<Boolean> = _isNapTimerRunning.asStateFlow()

    private var napTimerJob: java.util.Timer? = null

    fun insertSleepLog(bedTime: Long, wakeTime: Long, mood: String) = viewModelScope.launch {
        addSleepLogUseCase(bedTime, wakeTime, mood)
    }

    fun addManualSleepLog(bedTime: Long, wakeTime: Long, mood: String, notes: String) = viewModelScope.launch {
        addManualSleepLogUseCase(bedTime, wakeTime, mood, notes)
    }

    fun deleteSleepLog(log: SleepLog) = viewModelScope.launch {
        deleteSleepLogUseCase(log)
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
                        val alarm = Alarm(
                            label = "Nap Timer Wakeup!",
                            description = "Your recovery snooze is completed.",
                            hour = 0,
                            minute = 0,
                            repeatDays = "One-time",
                            emoji = "😴",
                            challengeType = "Math",
                            challengeDifficulty = "Easy"
                        )
                        onNapCompletedSimulatedAlarm(alarm)
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

    override fun onCleared() {
        super.onCleared()
        napTimerJob?.cancel()
    }
}
