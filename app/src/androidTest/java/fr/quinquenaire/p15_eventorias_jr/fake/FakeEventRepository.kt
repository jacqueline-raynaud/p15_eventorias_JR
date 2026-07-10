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

    override suspend fun createEvent(event: Event, imageUri: Uri?): String {
        val newEvent = event.copy(id = "fake_id_${System.currentTimeMillis()}")
        events.value = events.value + newEvent
        return newEvent.id
    }


    override suspend fun updateEvent(event: Event, imageUri: Uri?) {
        // Dans notre fake, on ignore imageUri, on met juste à jour l'objet dans la liste
        events.value = events.value.map {
            if (it.id == event.id) event else it
        }
    }

    override suspend fun deleteEvent(eventId: String, imageUrl: String) {
        // On supprime l'événement de la liste en filtrant.
        // L'URL de l'image est ignorée car il n'y a pas de vrai stockage Firebase.
        events.value = events.value.filter { it.id != eventId }
    }

    override suspend fun anonymizeOrganizerEvents(uid: String) {
        // Pour les tests d'interface actuels, cette méthode peut rester vide.
        // L'essentiel est qu'elle existe pour que le compilateur soit satisfait !

        // Si plus tard tu as besoin de tester l'anonymisation, tu pourras faire :
        // events.value = events.value.map {
        //     if (it.organizerId == uid) it.copy(organizerId = "anonyme") else it
        // }
    }
}
