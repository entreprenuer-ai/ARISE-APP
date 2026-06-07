package com.example.features.calendar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.Alarm
import com.example.core.database.CalendarEvent
import com.example.core.database.AriseRepository
import com.example.core.network.GoogleCalendarClient
import com.example.features.calendar.domain.usecase.CreateEventUseCase
import com.example.features.calendar.domain.usecase.DeleteEventUseCase
import com.example.features.calendar.domain.usecase.DeleteEventByIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val createEventUseCase = CreateEventUseCase(repository)
    private val deleteEventUseCase = DeleteEventUseCase(repository)
    private val deleteEventByIdUseCase = DeleteEventByIdUseCase(repository)

    val events: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _googleSyncStatus = MutableStateFlow("")
    val googleSyncStatus: StateFlow<String> = _googleSyncStatus.asStateFlow()

    fun insertEvent(event: CalendarEvent) = viewModelScope.launch {
        if (event.linkedAlarmId == 999 && event.prepTimeMinutes > 0) {
            val totalBufferMin = event.prepTimeMinutes + event.travelTimeMinutes
            val calendar = Calendar.getInstance().apply {
                timeInMillis = event.startTime
            }
            calendar.add(Calendar.MINUTE, -totalBufferMin)

            val alarmHour = calendar.get(Calendar.HOUR_OF_DAY)
            val alarmMin = calendar.get(Calendar.MINUTE)

            val dayOfWeekNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val alarmDay = if (calendar.get(Calendar.DAY_OF_WEEK) in 1..7) {
                dayOfWeekNames[calendar.get(Calendar.DAY_OF_WEEK)]
            } else {
                "One-time"
            }

            // Save the real Alarm
            val generatedAlarmId = repository.insertAlarm(
                Alarm(
                    label = "Prep: ${event.title}",
                    description = "Commute/prep for event starting at ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}",
                    hour = alarmHour,
                    minute = alarmMin,
                    repeatDays = alarmDay,
                    gradualVolume = true,
                    challengeType = "Math",
                    challengeDifficulty = "Medium"
                )
            )

            // Save the Event with the correct linked target Alarm reference
            createEventUseCase(event.copy(linkedAlarmId = generatedAlarmId.toInt()))
        } else {
            createEventUseCase(event)
        }
    }

    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
    }

    fun linkAlarmToEvent(event: CalendarEvent, alarm: Alarm) = viewModelScope.launch {
        val alarmId = repository.insertAlarm(alarm)
        createEventUseCase(event.copy(linkedAlarmId = alarmId.toInt()))
    }

    fun syncWithGoogleCalendar(token: String?) = viewModelScope.launch {
        _isSyncing.value = true
        _googleSyncStatus.value = "Initiating Google Calendar Sync..."
        
        try {
            if (!token.isNullOrBlank()) {
                repository.saveSetting("google_calendar_token", token)
            }
            
            val currentToken = token?.trim() ?: repository.getSetting("google_calendar_token")?.trim()
            val fetched = GoogleCalendarClient.fetchGoogleCalendarEvents(currentToken)
            
            if (fetched.isNotEmpty()) {
                var newCount = 0
                val currentEvents = events.value
                fetched.forEach { googleEv ->
                    val alreadyImported = currentEvents.any { local ->
                        local.title == googleEv.summary && local.startTime == googleEv.startTime
                    }
                    if (!alreadyImported) {
                        repository.insertEvent(
                            CalendarEvent(
                                title = googleEv.summary ?: "Google Sync Event",
                                notes = googleEv.notes,
                                startTime = googleEv.startTime,
                                endTime = googleEv.endTime,
                                location = googleEv.location ?: "Google Workspace",
                                category = "Work (Sync)",
                                colorHex = "#4285F4", // Google Blue
                                priority = "High",
                                linkedAlarmId = null
                            )
                        )
                        newCount++
                    }
                }
                
                if (currentToken.isNullOrBlank() || currentToken == "demo_mode" || currentToken == "google_sandbox") {
                    _googleSyncStatus.value = "Loaded $newCount fallback sandbox Google Calendar events. Connect a live OAuth token to sync real events!"
                } else {
                    _googleSyncStatus.value = "Synced $newCount new events from Google Calendar successfully!"
                }
            } else {
                _googleSyncStatus.value = "Sync complete. No new upcoming events found on Google Calendar."
            }
        } catch (e: Exception) {
            _googleSyncStatus.value = "Sync failed: ${e.localizedMessage}. Please specify a valid OAuth token."
        } finally {
            _isSyncing.value = false
        }
    }

    fun deleteEvent(event: CalendarEvent) = viewModelScope.launch {
        val alarmId = event.linkedAlarmId
        if (alarmId != null && alarmId > 0) {
            repository.getAlarmById(alarmId)?.let { alarm ->
                repository.deleteAlarm(alarm)
            }
        }
        deleteEventUseCase(event)
    }

    fun deleteEventById(id: Int) = viewModelScope.launch {
        deleteEventByIdUseCase(id)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
    }
}
