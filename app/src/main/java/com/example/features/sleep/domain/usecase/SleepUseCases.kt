package com.example.features.sleep.domain.usecase

import com.example.core.database.SleepLog
import com.example.core.database.AriseRepository

class AddSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(bedTime: Long, wakeTime: Long, mood: String) {
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
}

class AddManualSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(bedTime: Long, wakeTime: Long, mood: String, notes: String) {
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
}

class DeleteSleepLogUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(log: SleepLog) = repository.deleteSleepLog(log)
}
