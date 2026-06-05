package com.example.features.habits.domain.usecase

import com.example.core.database.Habit
import com.example.core.database.HabitCompletion
import com.example.core.database.AriseRepository

class CreateHabitUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(habit: Habit): Long = repository.insertHabit(habit)
}

class DeleteHabitUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(habit: Habit) = repository.deleteHabit(habit)
}

class CompleteHabitUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(habitId: Int, notes: String = "") {
        val timestamp = System.currentTimeMillis()
        repository.insertHabitCompletion(
            HabitCompletion(
                habitId = habitId,
                completionTimestamp = timestamp,
                notes = notes
            )
        )
        val habit = repository.getHabitById(habitId)
        if (habit != null) {
            val newStreak = if (habit.lastCompletedTimestamp == 0L) {
                1
            } else if (timestamp - habit.lastCompletedTimestamp <= 36 * 3600 * 1000) { // completed within 36 hours of last check
                habit.currentStreak + 1
            } else {
                1
            }
            val maxStr = if (newStreak > habit.maxStreak) newStreak else habit.maxStreak
            repository.insertHabit(
                habit.copy(
                    currentStreak = newStreak,
                    maxStreak = maxStr,
                    lastCompletedTimestamp = timestamp
                )
            )
        }
    }
}
