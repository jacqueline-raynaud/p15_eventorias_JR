package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model

data class EventDetailMutableState(
    val event: EventDetailUiState? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)