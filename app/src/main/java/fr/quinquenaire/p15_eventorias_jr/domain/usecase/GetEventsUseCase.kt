package fr.quinquenaire.p15_eventorias_jr.domain.usecase

import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor (private val eventRepository: EventRepository)  {
    operator fun invoke() : Flow<List<Event>> {
        return eventRepository.getEvents()
    }

    fun searchEvents(query: String): Flow<List<Event>> {
        return eventRepository.searchEvents(query)
    }

    fun getEventsByCategory(category: String): Flow<List<Event>> {
        return eventRepository.getEventsByCategory(category)
    }
}