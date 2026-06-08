package com.example.features.calendar.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.alarm.SmartAlarmCalculator
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartAlarmViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val _bufferMinutes = MutableStateFlow(90)
    val bufferMinutes: StateFlow<Int> = _bufferMinutes.asStateFlow()

    private val _autoAdjustEnabled = MutableStateFlow(false)
    val autoAdjustEnabled: StateFlow<Boolean> = _autoAdjustEnabled.asStateFlow()

    private val _masterDefaultAlarmTime = MutableStateFlow("07:00")
    val masterDefaultAlarmTime: StateFlow<String> = _masterDefaultAlarmTime.asStateFlow()

    private val _calculationResult = MutableStateFlow<SmartAlarmCalculator.SmartCalculationResult?>(null)
    val calculationResult: StateFlow<SmartAlarmCalculator.SmartCalculationResult?> = _calculationResult.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _smartAlarmStatusText = MutableStateFlow("")
    val smartAlarmStatusText: StateFlow<String> = _smartAlarmStatusText.asStateFlow()

    init {
        // Load initial settings
        viewModelScope.launch {
            try {
                val savedBuffer = repository.getSetting("smart_alarm_buffer_minutes")?.toIntOrNull() ?: 90
                _bufferMinutes.value = savedBuffer

                val savedAuto = repository.getSetting("auto_adjust_smart_alarm") == "true"
                _autoAdjustEnabled.value = savedAuto

                val savedMasterDefault = repository.getSetting("master_default_alarm_time") ?: "07:00"
                _masterDefaultAlarmTime.value = savedMasterDefault
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setMasterDefaultAlarmTime(time: String) {
        _masterDefaultAlarmTime.value = time
        viewModelScope.launch {
            repository.saveSetting("master_default_alarm_time", time)
        }
    }

    fun setBufferMinutes(minutes: Int) {
        _bufferMinutes.value = minutes
        viewModelScope.launch {
            repository.saveSetting("smart_alarm_buffer_minutes", minutes.toString())
        }
    }

    fun toggleAutoAdjust(enabled: Boolean) {
        _autoAdjustEnabled.value = enabled
        viewModelScope.launch {
            repository.saveSetting("auto_adjust_smart_alarm", enabled.toString())
        }
    }

    /**
     * Finds next event tomorrow and calculates target wake-up time.
     */
    fun calculateSmartAlarm(context: Context) {
        viewModelScope.launch {
            _isCalculating.value = true
            _smartAlarmStatusText.value = "Scanning schedules..."
            try {
                val event = SmartAlarmCalculator.findFirstEventOfNextDay(context, repository)
                val result = SmartAlarmCalculator.calculateSmartWakeTime(
                    event = event,
                    bufferMinutes = _bufferMinutes.value,
                    fallbackTimeStr = _masterDefaultAlarmTime.value
                )
                _calculationResult.value = result

                if (event != null) {
                    _smartAlarmStatusText.value = "Identified tomorrow's event: '${event.title}' starting at ${result.originalEventTimeStr}"
                } else {
                    _smartAlarmStatusText.value = "Fallback active: Wake up at ${_masterDefaultAlarmTime.value} (No events tomorrow)"
                }
            } catch (e: Exception) {
                _smartAlarmStatusText.value = "Calculation failed: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isCalculating.value = false
            }
        }
    }

    /**
     * Registers & updates the Alarm with standard AlarmManager framework.
     */
    fun saveAndScheduleSmartAlarm(context: Context) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val result = _calculationResult.value
                if (result?.calculatedHour != null) {
                    val alarms = SmartAlarmCalculator.updateScheduledSmartAlarm(context, repository, result)
                    if (alarms != null) {
                        _smartAlarmStatusText.value = "Smart Wake Alarm successfully scheduled for ${result.calculatedWakeTimeStr} (${result.bufferMinutes}m buffer)"
                    } else {
                        _smartAlarmStatusText.value = "Could not schedule alarm. Check system configurations."
                    }
                } else {
                    _smartAlarmStatusText.value = "Please run 'Analyze' first to calculate target wake times."
                }
            } catch (e: Exception) {
                _smartAlarmStatusText.value = "Scheduling failed: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
