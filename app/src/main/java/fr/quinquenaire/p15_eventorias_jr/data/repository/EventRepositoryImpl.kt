package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.android.data.remote.firebase.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestoreManager: FirebaseFirestoreManager,
    private val storageManager: FirebaseStorageManager
) : EventRepository {

    override fun getEvents(): Flow<List<Event>> {
        return firestoreManager.getEvents()
    }

    override fun getEventDetail(eventId: String): Flow<Event?> {
        return firestoreManager.getEventDetail(eventId)
    }

    override fun searchEvents(query: String): Flow<List<Event>> {
        return firestoreManager.searchEvents(query)
    }

    override fun getEventsByCategory(category: String): Flow<List<Event>> {
        return firestoreManager.getEventsByCategory(category)
    }

    override suspend fun createEvent(event: Event, imageUri: Uri?): String {
        var eventToCreate = event
        if (imageUri != null) {
            val tempEventId = "temp_${System.currentTimeMillis()}"
            val imageUrl = storageManager.uploadEventImage(tempEventId, imageUri)
            eventToCreate = event.copy(imageUrl = imageUrl)
        }
        return firestoreManager.createEvent(eventToCreate)
    }

    override suspend fun updateEvent(event: Event) {
        firestoreManager.updateEvent(event)
    }

    override suspend fun deleteEvent(eventId: String) {
        firestoreManager.deleteEvent(eventId)
    }
}
