package com.example.features.statistics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AlarmHistoryItem
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    val alarmHistory: StateFlow<List<AlarmHistoryItem>> = repository.allAlarmHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun injectMockupAnalyticsData() = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 3600 * 1000L
        val list = listOf(
            AlarmHistoryItem(alarmId = 1, alarmLabel = "Morning Strobe Check", triggeredTime = now - 5 * dayInMs, dismissedTime = now - 5 * dayInMs + 23000, responseTimeSeconds = 23, challengeCompleted = "Math", wakeMood = "Good"),
            AlarmHistoryItem(alarmId = 2, alarmLabel = "Evening Habit Spark", triggeredTime = now - 4 * dayInMs, dismissedTime = now - 4 * dayInMs + 45000, responseTimeSeconds = 45, challengeCompleted = "Memory", wakeMood = "Tired"),
            AlarmHistoryItem(alarmId = 3, alarmLabel = "Rise & Shine", triggeredTime = now - 3 * dayInMs, dismissedTime = now - 3 * dayInMs + 12000, responseTimeSeconds = 12, challengeCompleted = "Shake", wakeMood = "Good"),
            AlarmHistoryItem(alarmId = 4, alarmLabel = "Workout Blitz", triggeredTime = now - 2 * dayInMs, dismissedTime = now - 2 * dayInMs + 18000, responseTimeSeconds = 18, challengeCompleted = "Math", wakeMood = "Neutral"),
            AlarmHistoryItem(alarmId = 5, alarmLabel = "Meditation Hour", triggeredTime = now - 1 * dayInMs, dismissedTime = now - 1 * dayInMs + 5000, responseTimeSeconds = 5, challengeCompleted = "None", wakeMood = "Good")
        )
        list.forEach { item ->
            repository.insertAlarmHistoryItem(item)
        }
    }
}
