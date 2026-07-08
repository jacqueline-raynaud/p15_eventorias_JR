package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist

import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, imageUrl: String) =
        eventRepository.deleteEvent(eventId, imageUrl)
}