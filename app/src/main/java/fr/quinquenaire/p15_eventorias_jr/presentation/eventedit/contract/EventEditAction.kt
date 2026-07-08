package fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory

interface EventEditAction {
    data class OnNameChange(val value: String) : EventEditAction
    data class OnDescriptionChange(val value: String) : EventEditAction
    data class OnCategoryChange(val value: EventCategory) : EventEditAction
    data class OnDateSelected(val millis: Long) : EventEditAction
    data class OnTimeSelected(val hour: Int, val minute: Int) : EventEditAction
    data class OnAddressChange(val value: String) : EventEditAction
    data class OnImageSelected(val uri: Uri) : EventEditAction
    data object OnSaveClick : EventEditAction
    data object OnBackClick : EventEditAction
    data object OnRetry : EventEditAction
}

// presentation/eventedit/contract/EventEditEffect.kt
interface EventEditEffect {
    data object NavigateBack : EventEditEffect
    data class ShowSnackbar(val message: String) : EventEditEffect
}