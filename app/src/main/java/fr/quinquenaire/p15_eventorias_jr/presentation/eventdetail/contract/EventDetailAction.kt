package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract

interface EventDetailAction {
    data object OnBackClick : EventDetailAction
    data object OnEditClick : EventDetailAction
    data object OnDeleteClick : EventDetailAction
    data object OnRetry : EventDetailAction
}
