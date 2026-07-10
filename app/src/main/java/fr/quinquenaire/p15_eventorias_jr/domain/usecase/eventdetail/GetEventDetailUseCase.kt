package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail

import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventDetailUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(eventId: String): Flow<Event?> {
        return eventRepository.getEventDetail(eventId)
    }

}