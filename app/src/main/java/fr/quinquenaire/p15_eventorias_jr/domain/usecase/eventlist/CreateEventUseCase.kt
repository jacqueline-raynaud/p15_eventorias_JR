package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event, imageUri: Uri? = null): String =
        eventRepository.createEvent(event, imageUri)
}