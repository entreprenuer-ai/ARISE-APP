package com.example.features.habits.domain.usecase

import com.example.core.database.Habit
import com.example.core.database.HabitCompletion
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.first

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
            val lastCompletedCal = java.util.Calendar.getInstance().apply {
                timeInMillis = habit.lastCompletedTimestamp
            }
            val todayCal = java.util.Calendar.getInstance().apply {
                timeInMillis = timestamp
            }
            
            val isSameDay = habit.lastCompletedTimestamp != 0L && 
                    lastCompletedCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                    lastCompletedCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
                    
            val yesterdayCal = java.util.Calendar.getInstance().apply {
                timeInMillis = timestamp
                add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
            val isYesterday = habit.lastCompletedTimestamp != 0L && 
                    lastCompletedCal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
                    lastCompletedCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR)

            val isSkip = notes.contains("SKIPPED")
            val newStreak = if (habit.lastCompletedTimestamp == 0L) {
                if (isSkip) 0 else 1
            } else if (isSameDay) {
                habit.currentStreak
            } else if (isYesterday) {
                if (isSkip) habit.currentStreak else habit.currentStreak + 1
            } else {
                if (isSkip) habit.currentStreak else 1
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

class DeleteHabitCompletionUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(completion: HabitCompletion) {
        repository.deleteHabitCompletion(completion)
        
        val habit = repository.getHabitById(completion.habitId)
        if (habit != null) {
            val remainingCompletions = repository.allHabitCompletions
                .first()
                .filter { it.habitId == completion.habitId }
                .sortedBy { it.completionTimestamp }
                
            if (remainingCompletions.isEmpty()) {
                repository.insertHabit(
                    habit.copy(
                        currentStreak = 0,
                        lastCompletedTimestamp = 0L
                    )
                )
            } else {
                var streak = 0
                var lastTime = 0L
                remainingCompletions.forEach { comp ->
                    val timestamp = comp.completionTimestamp
                    if (lastTime == 0L) {
                        streak = if (comp.notes.contains("SKIPPED")) 0 else 1
                    } else {
                        val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastTime }
                        val currCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
                        
                        val isSameDay = lastCal.get(java.util.Calendar.YEAR) == currCal.get(java.util.Calendar.YEAR) &&
                                        lastCal.get(java.util.Calendar.DAY_OF_YEAR) == currCal.get(java.util.Calendar.DAY_OF_YEAR)
                                        
                        val lastCalYesterday = java.util.Calendar.getInstance().apply {
                            timeInMillis = timestamp
                            add(java.util.Calendar.DAY_OF_YEAR, -1)
                        }
                        val isYesterday = lastCal.get(java.util.Calendar.YEAR) == lastCalYesterday.get(java.util.Calendar.YEAR) &&
                                          lastCal.get(java.util.Calendar.DAY_OF_YEAR) == lastCalYesterday.get(java.util.Calendar.DAY_OF_YEAR)
                        
                        val currIsSkip = comp.notes.contains("SKIPPED")
                        if (isSameDay) {
                            // No change in streak count
                        } else if (isYesterday) {
                            if (!currIsSkip) streak++
                        } else {
                            streak = if (currIsSkip) streak else 1
                        }
                    }
                    lastTime = timestamp
                }
                
                repository.insertHabit(
                    habit.copy(
                        currentStreak = streak,
                        lastCompletedTimestamp = lastTime
                    )
                )
            }
        }
    }
}
