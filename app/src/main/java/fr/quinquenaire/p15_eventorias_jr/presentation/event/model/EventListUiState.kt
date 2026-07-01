package fr.quinquenaire.p15_eventorias_jr.presentation.event.model

import androidx.compose.runtime.Immutable
import fr.quinquenaire.p15_eventorias_jr.domain.Event

@Immutable
class EventListUiState(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val category: String,
    val imageUrl: String,
    val locationName: String,
    val organizerId: String,
    val latitude: Double?,    // extrait du GeoPoint pour l'UI
    val longitude: Double?
)

// Mapper domain -> UI
fun Event.toUi(): EventListUiState = EventListUiState(
    id = id,
    name = name,
    date = date,
    time = time,
    category = category,
    imageUrl = imageUrl,
    locationName = locationName,
    organizerId = organizerId,
    latitude = location?.latitude,
    longitude = location?.longitude
)
