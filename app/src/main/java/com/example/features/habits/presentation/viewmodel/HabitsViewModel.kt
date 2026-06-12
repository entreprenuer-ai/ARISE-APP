package com.example.features.habits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.Habit
import com.example.core.database.HabitCompletion
import com.example.core.database.AriseRepository
import com.example.features.habits.domain.usecase.CreateHabitUseCase
import com.example.features.habits.domain.usecase.DeleteHabitUseCase
import com.example.features.habits.domain.usecase.CompleteHabitUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val createHabitUseCase = CreateHabitUseCase(repository)
    private val deleteHabitUseCase = DeleteHabitUseCase(repository)
    private val completeHabitUseCase = CompleteHabitUseCase(repository)
    private val deleteHabitCompletionUseCase = com.example.features.habits.domain.usecase.DeleteHabitCompletionUseCase(repository)

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitCompletions: StateFlow<List<HabitCompletion>> = repository.allHabitCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertHabit(habit: Habit) = viewModelScope.launch {
        createHabitUseCase(habit)
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        deleteHabitUseCase(habit)
    }

    fun completeHabit(habitId: Int, notes: String = "") = viewModelScope.launch {
        completeHabitUseCase(habitId, notes)
    }

    fun deleteHabitCompletion(completion: HabitCompletion) = viewModelScope.launch {
        deleteHabitCompletionUseCase(completion)
    }
}
