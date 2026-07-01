package fr.quinquenaire.p15_eventorias_jr.presentation.event.viewmodel.contract

interface EventListEffect {
    data class NavigateToEventDetail(val eventId: String) : EventListEffect
    data class ShowSnackbar(val message: String): EventListEffect
}