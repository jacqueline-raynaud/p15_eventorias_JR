package fr.quinquenaire.p15_eventorias_jr.presentation.event.model

import fr.quinquenaire.p15_eventorias_jr.presentation.event.SortOrder

class EventListMutableState (
    val events: List<EventListUiState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val sortOrder: SortOrder = SortOrder.BY_DATE
)