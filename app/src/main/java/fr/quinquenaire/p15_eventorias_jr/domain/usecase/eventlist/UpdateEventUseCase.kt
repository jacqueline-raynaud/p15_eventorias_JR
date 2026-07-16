package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import fr.quinquenaire.p15_eventorias_jr.data.location.GeocoderManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val geocoderManager: GeocoderManager
) {
    suspend operator fun invoke(
        eventId: String,
        updatedEvent: Event,
        imageUri: Uri?
    ): Result<Unit> = try {

        // 1 récupère l'évent
        val currentEvent = eventRepository.getEventDetail(eventId).first()
            ?: return Result.failure(Exception("Event not found"))

        // 2. logique de geocodage si adresse changée
        val finalLocation: GeoPoint? = if (updatedEvent.locationName != currentEvent.locationName) {
            geocoderManager.geocode(updatedEvent.locationName)
            // Si  géocodage  null, garde ancienne adresse.
                ?: currentEvent.location
        } else {
            // L'adresse n'a pas changé : on conserve les coordonnées existantes
            currentEvent.location
        }

        // 3. objet à sauvegarder
        val eventToSave = updatedEvent.copy(location = finalLocation)

        // 4. Appel au Repository pour la persistance (et upload image si besoin)
        eventRepository.updateEvent(eventToSave, imageUri)

        Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }
}