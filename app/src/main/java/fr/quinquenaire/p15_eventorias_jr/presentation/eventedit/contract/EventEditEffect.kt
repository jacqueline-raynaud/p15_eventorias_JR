package fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract

interface EventEditEffect {
    data object NavigateBack : EventEditEffect
    data class ShowSnackbar(val message: String) : EventEditEffect
}