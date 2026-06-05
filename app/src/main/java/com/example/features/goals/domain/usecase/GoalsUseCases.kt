package com.example.features.goals.domain.usecase

import com.example.core.database.Goal
import com.example.core.database.AriseRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class CreateGoalUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(goal: Goal): Long = repository.insertGoal(goal)
}

class DeleteGoalUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(goal: Goal) = repository.deleteGoal(goal)
}

class IncrementGoalProgressUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(goal: Goal) {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val checkInLong = todayStr.toLong()

        if (goal.lastCheckedDate != checkInLong) {
            val updatedProgress = (goal.currentProgress + 1).coerceAtMost(goal.targetProgress)
            val updatedStreak = if (goal.currentProgress + 1 >= goal.targetProgress) goal.streakCount + 1 else goal.streakCount
            val updatedGoal = goal.copy(
                currentProgress = updatedProgress,
                streakCount = updatedStreak,
                lastCheckedDate = checkInLong
            )
            repository.insertGoal(updatedGoal)
        }
    }
}
