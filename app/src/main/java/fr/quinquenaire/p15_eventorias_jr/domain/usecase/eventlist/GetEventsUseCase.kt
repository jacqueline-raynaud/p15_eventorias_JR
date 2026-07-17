package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist

import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor (private val repository: EventRepository)  {

    operator fun invoke(params: EventQueryParams): Flow<List<Event>> {
        return repository.getEventsStream(params)
    }
}