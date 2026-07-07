package fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract

interface CreateEventEffect {
    data class ShowSnackbar(val message: String) : CreateEventEffect
    data object NavigateBack : CreateEventEffect
}
