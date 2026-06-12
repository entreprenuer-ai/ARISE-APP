package com.example.features.sleep.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.alarm.SmartAlarmCalculator
import com.example.core.database.AriseRepository
import com.example.core.database.SleepSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class SleepInsightWarning(
    val session: SleepSession,
    val actualHours: Float,
    val targetHours: Float,
    val sleepDebtHours: Float
)

class SleepTrackingViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _trackingStartTime = MutableStateFlow<Long?>(null)
    val trackingStartTime: StateFlow<Long?> = _trackingStartTime.asStateFlow()

    val allSleepSessions: StateFlow<List<SleepSession>> = repository.allSleepSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _sleepTargetHours = MutableStateFlow(8.0f)
    val sleepTargetHours: StateFlow<Float> = _sleepTargetHours.asStateFlow()

    private val _insightWarning = MutableStateFlow<SleepInsightWarning?>(null)
    val insightWarning: StateFlow<SleepInsightWarning?> = _insightWarning.asStateFlow()

    init {
        loadActiveSession()
        loadSleepTargetAndObserve()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            try {
                val startTimeStr = repository.getSetting("active_sleep_tracking_start_time")
                if (!startTimeStr.isNullOrEmpty()) {
                    val startTime = startTimeStr.toLongOrNull()
                    if (startTime != null && startTime > 0) {
                        _trackingStartTime.value = startTime
                        _isTracking.value = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSleepTargetAndObserve() {
        viewModelScope.launch {
            try {
                // Initialize target hours
                val savedTarget = repository.getSetting("sleep_target_hours")?.toFloatOrNull() ?: 8.0f
                _sleepTargetHours.value = savedTarget

                // Combine sessions and target to dynamically evaluate the latest session
                combine(allSleepSessions, _sleepTargetHours) { sessions, target ->
                    Pair(sessions, target)
                }.collectLatest { (sessions, target) ->
                    val latest = sessions.firstOrNull()
                    if (latest == null) {
                        _insightWarning.value = null
                        return@collectLatest
                    }

                    val actionTakenId = repository.getSetting("sleep_last_session_action_taken_id")?.toIntOrNull()
                    if (latest.id == actionTakenId) {
                        _insightWarning.value = null
                        return@collectLatest
                    }

                    val durationHours = (latest.endTimeMillis - latest.startTimeMillis) / 3600000.0f
                    if (durationHours < target) {
                        _insightWarning.value = SleepInsightWarning(
                            session = latest,
                            actualHours = durationHours,
                            targetHours = target,
                            sleepDebtHours = target - durationHours
                        )
                    } else {
                        _insightWarning.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSleepTargetHours(hours: Float) {
        _sleepTargetHours.value = hours
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveSetting("sleep_target_hours", hours.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startTracking() {
        val now = System.currentTimeMillis()
        _trackingStartTime.value = now
        _isTracking.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveSetting("active_sleep_tracking_start_time", now.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelTracking() {
        _isTracking.value = false
        _trackingStartTime.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteSetting("active_sleep_tracking_start_time")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAndSaveTracking(rating: Float, notes: String) {
        val startTime = _trackingStartTime.value ?: return
        val endTime = System.currentTimeMillis()
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = SleepSession(
                    startTimeMillis = startTime,
                    endTimeMillis = endTime,
                    sleepQualityRating = rating,
                    notes = notes
                )
                repository.insertSleepSession(session)
                
                // Clear tracking state
                _isTracking.value = false
                _trackingStartTime.value = null
                repository.deleteSetting("active_sleep_tracking_start_time")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSession(session: SleepSession) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteSleepSession(session)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Local Telemetry Export Engine ---
    suspend fun exportSleepSessionsToJson(): String = withContext(Dispatchers.IO) {
        try {
            // WAL Checkpointing Safety
            repository.runCheckpoint()
            
            val sessions = allSleepSessions.value
            val array = JSONArray()
            for (session in sessions) {
                val obj = JSONObject().apply {
                    put("id", session.id)
                    put("startTimeMillis", session.startTimeMillis)
                    put("endTimeMillis", session.endTimeMillis)
                    put("sleepQualityRating", session.sleepQualityRating.toDouble())
                    put("notes", session.notes)
                }
                array.put(obj)
            }
            array.toString(2)
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    suspend fun importSleepSessionsFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val session = SleepSession(
                    id = obj.optInt("id", 0),
                    startTimeMillis = obj.getLong("startTimeMillis"),
                    endTimeMillis = obj.getLong("endTimeMillis"),
                    sleepQualityRating = obj.getDouble("sleepQualityRating").toFloat(),
                    notes = obj.optString("notes", "")
                )
                repository.insertSleepSession(session)
            }
            // WAL Checkpointing Safety
            repository.runCheckpoint()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun dismissInsight() {
        val latest = allSleepSessions.value.firstOrNull() ?: return
        viewModelScope.launch {
            try {
                repository.saveSetting("sleep_last_session_action_taken_id", latest.id.toString())
                _insightWarning.value = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delayAlarm(context: Context, delayMinutes: Int, onSuccess: (String) -> Unit) {
        val latest = allSleepSessions.value.firstOrNull()
        viewModelScope.launch {
            try {
                // 1. Fetch current master default alarm key
                val masterDefault = repository.getSetting("master_default_alarm_time") ?: "07:00"
                val parts = masterDefault.split(":")
                val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 7
                val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    add(Calendar.MINUTE, delayMinutes)
                }

                val delayedHour = calendar.get(Calendar.HOUR_OF_DAY)
                val delayedMinute = calendar.get(Calendar.MINUTE)
                val delayedTimeStr = "%02d:%02d".format(delayedHour, delayedMinute)

                // Save delayed time back to configuration
                repository.saveSetting("master_default_alarm_time", delayedTimeStr)

                // 2. Trigger calculation and update the scheduled intent alarm
                val event = SmartAlarmCalculator.findFirstEventOfNextDay(context, repository)
                val savedBuffer = repository.getSetting("smart_alarm_buffer_minutes")?.toIntOrNull() ?: 90
                val result = SmartAlarmCalculator.calculateSmartWakeTime(
                    event = event,
                    bufferMinutes = savedBuffer,
                    fallbackTimeStr = delayedTimeStr
                )
                
                val updatedAlarm = SmartAlarmCalculator.updateScheduledSmartAlarm(context, repository, result)
                if (updatedAlarm != null) {
                    onSuccess("Tomorrow's fallback alarm delayed by $delayMinutes minutes (now set to $delayedTimeStr)")
                } else {
                    onSuccess("Alarm setting updated to $delayedTimeStr successfully!")
                }

                // Mark action taken
                if (latest != null) {
                    repository.saveSetting("sleep_last_session_action_taken_id", latest.id.toString())
                }
                _insightWarning.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                onSuccess("Action failed: ${e.localizedMessage}")
            }
        }
    }

    fun logManualSleepSession(startTimeMillis: Long, endTimeMillis: Long, rating: Float, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = SleepSession(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    sleepQualityRating = rating,
                    notes = notes
                )
                repository.insertSleepSession(session)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateSleepSession(session: SleepSession) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertSleepSession(session)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
