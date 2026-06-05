package com.example.features.routines.domain.usecase

import com.example.core.database.Routine
import com.example.core.database.AriseRepository

class CreateRoutineUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(routine: Routine): Long = repository.insertRoutine(routine)
}

class DeleteRoutineUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(routine: Routine) = repository.deleteRoutine(routine)
}
