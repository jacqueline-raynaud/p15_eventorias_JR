package fr.quinquenaire.p15_eventorias_jr.fake

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.SortOrder
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeEventRepository @Inject constructor() : EventRepository {

    private val _events = MutableStateFlow<List<Event>>(emptyList())

    /**
     * Permet d'injecter une liste de test depuis tes classes de test.
     */
    fun setEvents(list: List<Event>) {
        _events.value = list
    }

    override fun getEventsStream(params: EventQueryParams): Flow<List<Event>> =
        _events.map { list ->
            var filteredList = list

            // 1. Filtre par catégorie
            params.category?.let { cat ->
                if (cat.isNotEmpty()) {
                    filteredList = filteredList.filter { it.category == cat }
                }
            }

            // 2. Filtre par recherche (nom ou description)
            if (params.searchQuery.isNotEmpty()) {
                filteredList = filteredList.filter {
                    it.name.contains(params.searchQuery, ignoreCase = true) ||
                            it.description.contains(params.searchQuery, ignoreCase = true)
                }
            }

            // 3. Tri
            filteredList = when (params.sortOrder) {
                SortOrder.BY_DATE_ASC -> filteredList.sortedBy { it.date }
                SortOrder.BY_DATE_DESC -> filteredList.sortedByDescending { it.date }
                SortOrder.DEFAULT -> filteredList
            }

            // 4. Limite
            filteredList.take(params.limit)
        }

    override fun getEventDetail(eventId: String): Flow<Event?> =
        _events.map { list -> list.find { it.id == eventId } }

    override suspend fun createEvent(event: Event, imageUri: Uri?): String {
        val newId = event.id.ifEmpty { "fake_id_${System.currentTimeMillis()}" }
        val newEvent = event.copy(id = newId)
        _events.value = _events.value + newEvent
        return newId
    }

    override suspend fun updateEvent(event: Event, imageUri: Uri?) {
        _events.value = _events.value.map {
            if (it.id == event.id) event else it
        }
    }

    override suspend fun deleteEvent(eventId: String, imageUrl: String) {
        _events.value = _events.value.filter { it.id != eventId }
    }

    override suspend fun anonymizeOrganizerEvents(uid: String) {
        _events.value = _events.value.map {
            if (it.organizerId == uid) it.copy(organizerId = "utilisateur_supprime") else it
        }
    }
}