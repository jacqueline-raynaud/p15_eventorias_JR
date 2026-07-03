package fr.quinquenaire.p15_eventorias_jr.fake

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeEventRepository @Inject constructor() : EventRepository {

    private val events = MutableStateFlow<List<Event>>(emptyList())

    fun setEvents(list: List<Event>) {
        events.value = list
    }

    override fun getEvents(): Flow<List<Event>> = events

    override fun getEventDetail(eventId: String): Flow<Event?> =
        events.map { list -> list.find { it.id == eventId } }

    override fun searchEvents(query: String): Flow<List<Event>> =
        events.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) }
        }

    override fun getEventsByCategory(category: String): Flow<List<Event>> =
        events.map { list ->
            list.filter { it.category == category }
        }

    override suspend fun createEvent(event: Event, imageUrl: Uri?): String {
        val newEvent = event.copy(id = "fake_id_${System.currentTimeMillis()}")
        events.value = events.value + newEvent
        return newEvent.id
    }

    override suspend fun updateEvent(event: Event) {
        events.value = events.value.map {
            if (it.id == event.id) event else it
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        events.value = events.value.filter { it.id != eventId }
    }
}
