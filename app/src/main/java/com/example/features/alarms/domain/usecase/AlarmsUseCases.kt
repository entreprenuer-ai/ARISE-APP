package com.example.features.alarms.domain.usecase

import com.example.core.database.Alarm
import com.example.core.database.AriseRepository

class CreateAlarmUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(alarm: Alarm): Long = repository.insertAlarm(alarm)
}

class DeleteAlarmUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(alarm: Alarm) = repository.deleteAlarm(alarm)
}

class ToggleAlarmActiveUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(alarm: Alarm) {
        repository.updateAlarmStatus(alarm.id, !alarm.isActive)
    }
}

class UpdateAlarmUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(alarm: Alarm) = repository.insertAlarm(alarm)
}
