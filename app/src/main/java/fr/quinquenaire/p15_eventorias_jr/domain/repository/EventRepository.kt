package fr.quinquenaire.p15_eventorias_jr.domain.repository

import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    fun getEventDetail(eventId: String): Flow<Event?>
    fun searchEvents(query: String): Flow<List<Event>>
    fun getEventsByCategory(category: String): Flow<List<Event>>
    suspend fun createEvent(event: Event, imageUri: Uri?=null):String
    suspend fun updateEvent(event: Event, imageUri: Uri? = null)
    suspend fun deleteEvent(eventId: String, imageUrl: String)
    suspend fun anonymizeOrganizerEvents(uid: String)
}
