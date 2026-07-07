package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import android.util.Log
import fr.quinquenaire.p15_eventorias_jr.data.location.GeocoderManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestoreManager: FirebaseFirestoreManager,
    private val storageManager: FirebaseStorageManager,
    private val geocoderManager: GeocoderManager
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
        // Géocodage de l'adresse
        val location = try {
            geocoderManager.geocode(event.locationName)
                .also { Log.d("EventoriasApp", "Geocoded '${event.locationName}' -> $it") }
        } /*catch (e: IOException) {
            Log.e("EventoriasApp", "Geocoding failed", e)
            null    // on crée l'événement sans coordonnées plutôt que d'échouer*/
            catch (e: Exception) {   // élargi à Exception le temps du debug
                Log.e("EventoriasApp", "Geocoding failed", e)
                null
        }
        //Création du document puis upoload image
        val eventId = firestoreManager.createEvent(event.copy(location = location))
        //val eventId = firestoreManager.createEvent(event)
        if (imageUri != null) {
            val imageUrl = storageManager.uploadEventImage(eventId, imageUri)
            firestoreManager.updateEventImageUrl(eventId, imageUrl)
        }
        return eventId
    }
    /*var eventToCreate = event
    if (imageUri != null) {
        val tempEventId = "temp_${System.currentTimeMillis()}"
        val imageUrl = storageManager.uploadEventImage(tempEventId, imageUri)
        eventToCreate = event.copy(imageUrl = imageUrl)
    }
    return firestoreManager.createEvent(eventToCreate)*/


    override suspend fun updateEvent(event: Event) {
        firestoreManager.updateEvent(event)
    }

    override suspend fun deleteEvent(eventId: String) {
        firestoreManager.deleteEvent(eventId)
    }
}
