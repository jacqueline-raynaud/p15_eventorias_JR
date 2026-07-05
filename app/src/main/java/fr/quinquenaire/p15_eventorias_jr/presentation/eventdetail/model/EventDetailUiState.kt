package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model

import androidx.compose.runtime.Immutable
import fr.quinquenaire.p15_eventorias_jr.BuildConfig
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.presentation.util.MapUrlBuilder

@Immutable
data class EventDetailUiState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val locationName: String = "",
    val imageUrl: String = "",
    val organizerId: String = "",
    val organizerAvatarUrl: String = "",   // rempli via GetUserProfileUseCase
    val staticMapUrl: String = ""          // URL Google Maps Static construite au mapping )
)
// Mapper domain → UI

fun Event.toDetailUi(organizerAvatarUrl: String = ""): EventDetailUiState = EventDetailUiState(
    id = id,
    name = name,
    description = description,
    date = date,
    time = time,
    locationName = locationName,
    imageUrl = imageUrl,
    organizerId = organizerId,
    organizerAvatarUrl = organizerAvatarUrl,
    staticMapUrl = location?.let { MapUrlBuilder.build(it.latitude, it.longitude) }.orEmpty()
)

