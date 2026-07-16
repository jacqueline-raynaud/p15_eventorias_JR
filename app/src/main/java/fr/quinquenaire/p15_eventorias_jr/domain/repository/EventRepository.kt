package fr.quinquenaire.p15_eventorias_jr.domain.repository

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEventsStream(params: EventQueryParams): Flow<List<Event>>

    //fun getEvents(): Flow<List<Event>>
    fun getEventDetail(eventId: String): Flow<Event?>
    //fun searchEvents(query: String): Flow<List<Event>>
    //fun getEventsByCategory(category: String): Flow<List<Event>>
    suspend fun createEvent(event: Event, imageUri: Uri?=null):String
    suspend fun updateEvent(event: Event, imageUri: Uri? = null): Unit
    suspend fun deleteEvent(eventId: String, imageUrl: String)
    suspend fun anonymizeOrganizerEvents(uid: String)
}
