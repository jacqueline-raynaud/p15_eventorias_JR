package fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.model

import android.net.Uri
import com.google.firebase.firestore.GeoPoint
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EventEditMutableState(
    val name: String = "",
    val description: String = "",
    val category: EventCategory? = null,
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val address: String = "",
    val imageUri: Uri? = null,              // nouvelle image choisie (null = pas de changement)
    val existingImageUrl: String = "",      // image actuelle sur Storage

    // Champs invisibles à l'écran, à préserver tels quels
    val organizerId: String = "",
    val guests: List<String> = emptyList(),
    val initialAddress: String = "",
    val initialLocation: GeoPoint? = null,

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && category != null &&
                dateMillis != null && hour != null && address.isNotBlank()

    val dateLabel: String
        get() = dateMillis?.let {
            SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                .format(Date(it)).replaceFirstChar { c -> c.uppercase() }
        }.orEmpty()

    val timeLabel: String
        get() = if (hour != null && minute != null) "%02dh%02d".format(hour, minute) else ""

    // Ce que le sélecteur d'image doit prévisualiser : nouvelle image locale, sinon l'existante
    val imagePreview: Any?
        get() = imageUri ?: existingImageUrl.ifBlank { null }
}