package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract

interface EventDetailEffect {
    data object NavigateBack : EventDetailEffect
    data class NavigateToEdit(val eventId: String) : EventDetailEffect
    data class ShowSnackbar(val message: String) : EventDetailEffect }