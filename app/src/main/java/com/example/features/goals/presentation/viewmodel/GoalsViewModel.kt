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
}
