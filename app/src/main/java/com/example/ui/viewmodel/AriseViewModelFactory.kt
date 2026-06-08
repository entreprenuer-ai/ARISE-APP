package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.database.AriseRepository
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import com.example.features.backup.presentation.viewmodel.BackupViewModel
import com.example.features.calendar.presentation.viewmodel.CalendarViewModel
import com.example.features.calendar.presentation.viewmodel.SmartAlarmViewModel
import com.example.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.features.goals.presentation.viewmodel.GoalsViewModel
import com.example.features.habits.presentation.viewmodel.HabitsViewModel
import com.example.features.security.presentation.viewmodel.SecurityViewModel
import com.example.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import com.example.features.sleep.presentation.viewmodel.SleepTrackingViewModel
import com.example.features.statistics.presentation.viewmodel.StatisticsViewModel

import com.example.core.database.Alarm

object SimulatedAlarmRegistry {
    var onSimulatedAlarmTriggered: ((Alarm) -> Unit)? = null
}

class AriseViewModelFactory(
    private val application: Application,
    private val repository: AriseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AriseViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AriseViewModel(application, repository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                DashboardViewModel() as T
            }
            modelClass.isAssignableFrom(AlarmViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                val alarmViewModel = AlarmViewModel(repository)
                SimulatedAlarmRegistry.onSimulatedAlarmTriggered = { alarm ->
                    alarmViewModel.simulateAlarmTrigger(alarm)
                }
                alarmViewModel as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CalendarViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SmartAlarmViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SmartAlarmViewModel(repository) as T
            }
            modelClass.isAssignableFrom(GoalsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                GoalsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HabitsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HabitsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SleepViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SleepViewModel(repository) { alarm ->
                    SimulatedAlarmRegistry.onSimulatedAlarmTriggered?.invoke(alarm)
                } as T
            }
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                StatisticsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SettingsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SecurityViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SecurityViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                BackupViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SleepTrackingViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SleepTrackingViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
