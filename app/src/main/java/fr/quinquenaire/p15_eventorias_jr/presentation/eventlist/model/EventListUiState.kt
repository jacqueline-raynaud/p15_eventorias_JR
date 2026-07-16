package fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model

import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.presentation.util.DateFormatters

@Immutable
data class EventListUiState(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val rawDate: Timestamp?,
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
    date = DateFormatters.formatDate(date),
    time = DateFormatters.formatTime(date),
    rawDate=date, //for test
    category = this.category,
    imageUrl = imageUrl,
    locationName = locationName,
    organizerId = organizerId,
    latitude = location?.latitude,
    longitude = location?.longitude
)
