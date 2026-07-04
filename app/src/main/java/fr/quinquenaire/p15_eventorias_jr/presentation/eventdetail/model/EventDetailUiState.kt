package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model

import androidx.compose.runtime.Immutable
import fr.quinquenaire.p15_eventorias_jr.BuildConfig
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event

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
    staticMapUrl = buildStaticMapUrl()
)

// Construction de l'URL Google Maps Static API private
fun Event.buildStaticMapUrl(): String {
    val lat = location?.latitude ?: return ""
    val lng = location?.longitude  ?: return ""
    return "https://maps.googleapis.com/maps/api/staticmap" + "?center=$lat,$lng" + "&zoom=15" + "&size=600x300" + "&markers=color:red%7C$lat,$lng" + "&key=${BuildConfig.GOOGLE_MAPS_API_KEY}"
}
