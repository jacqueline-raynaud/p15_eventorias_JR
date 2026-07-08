package fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory

interface CreateEventAction {
    data class OnNameChange(val value: String) : CreateEventAction
    data class OnDescriptionChange(val value: String) : CreateEventAction
    data class OnCategoryChange(val value: EventCategory) : CreateEventAction
    data class OnDateSelected(val millis: Long) : CreateEventAction
    data class OnTimeSelected(val hour: Int, val minute: Int) : CreateEventAction
    data class OnAddressChange(val value: String) : CreateEventAction
    data object OnSaveClick : CreateEventAction
    data object OnBackClick : CreateEventAction
    data class OnImageSelected(val uri: Uri) : CreateEventAction
}