package fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.model

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateEventMutableState(
    val name: String = "",
    val description: String = "",
    val category: EventCategory? = null,
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val address: String = "",
    val imageUri: Uri? = null,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = name.trim().isNotBlank() &&
                category != null &&
                dateMillis != null
                && hour != null
                && minute != null
                && address.trim().isNotBlank()

    val dateLabel: String
        get() = dateMillis?.let {
            SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                .format(Date(it)).replaceFirstChar { c -> c.uppercase() }
        }.orEmpty()

    val timeLabel: String
        get() = if (hour != null && minute != null) "%02dh%02d".format(hour, minute) else ""
}