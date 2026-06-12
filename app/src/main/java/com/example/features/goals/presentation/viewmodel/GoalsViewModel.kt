package com.example.features.goals.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.Goal
import com.example.core.database.AriseRepository
import com.example.features.goals.domain.usecase.CreateGoalUseCase
import com.example.features.goals.domain.usecase.DeleteGoalUseCase
import com.example.features.goals.domain.usecase.IncrementGoalProgressUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val createGoalUseCase = CreateGoalUseCase(repository)
    private val deleteGoalUseCase = DeleteGoalUseCase(repository)
    private val incrementGoalProgressUseCase = IncrementGoalProgressUseCase(repository)

    val goals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertGoal(goal: Goal) = viewModelScope.launch {
        createGoalUseCase(goal)
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch {
        deleteGoalUseCase(goal)
    }

    fun incrementGoalProgress(goal: Goal) = viewModelScope.launch {
        incrementGoalProgressUseCase(goal)
    }

    fun incrementGoalDirectly(goal: Goal) = viewModelScope.launch {
        val updatedProgress = (goal.currentProgress + 1).coerceAtMost(goal.targetProgress)
        val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val checkInLong = todayStr.toLong()
        val updatedStreak = if (updatedProgress >= goal.targetProgress && goal.lastCheckedDate != checkInLong) goal.streakCount + 1 else goal.streakCount
        val updated = goal.copy(
            currentProgress = updatedProgress,
            streakCount = updatedStreak,
            lastCheckedDate = if (updatedProgress >= goal.targetProgress) checkInLong else goal.lastCheckedDate
        )
        repository.insertGoal(updated)
    }

    fun resetGoalProgress(goal: Goal) = viewModelScope.launch {
        val updated = goal.copy(currentProgress = 0)
        repository.insertGoal(updated)
    }

    fun decrementGoalProgress(goal: Goal) = viewModelScope.launch {
        if (goal.currentProgress > 0) {
            val updated = goal.copy(currentProgress = goal.currentProgress - 1)
            repository.insertGoal(updated)
        }
    }
}
