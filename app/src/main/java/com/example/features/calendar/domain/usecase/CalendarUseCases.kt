package com.example.features.calendar.domain.usecase

import com.example.core.database.CalendarEvent
import com.example.core.database.AriseRepository

class CreateEventUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(event: CalendarEvent): Long = repository.insertEvent(event)
}

class DeleteEventUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(event: CalendarEvent) = repository.deleteEvent(event)
}

class DeleteEventByIdUseCase(private val repository: AriseRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteEventById(id)
}
