package com.example.features.sleep.domain.usecase

import com.example.core.database.SleepLog
import com.example.core.database.AriseRepository

class AddSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(bedTime: Long, wakeTime: Long, mood: String) {
        val targetStr = repository.getSetting("sleep_target_hours") ?: "8.0"
        val customTarget = targetStr.toFloatOrNull() ?: 8.0f
        val sleepHours = (wakeTime - bedTime) / 3600000f
        val debt = (customTarget - sleepHours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = customTarget
            )
        )
    }
}

class AddManualSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(bedTime: Long, wakeTime: Long, mood: String, notes: String) {
        val targetStr = repository.getSetting("sleep_target_hours") ?: "8.0"
        val customTarget = targetStr.toFloatOrNull() ?: 8.0f
        val hours = (wakeTime - bedTime) / 3600000f
        val debt = (customTarget - hours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = customTarget,
                notes = notes
            )
        )
    }
}

class DeleteSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(log: SleepLog) = repository.deleteSleepLog(log)
}
