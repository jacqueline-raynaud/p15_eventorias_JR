package fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract

import fr.quinquenaire.p15_eventorias_jr.domain.SortOrder

interface EventListAction {
    data object LoadEvents : EventListAction
    data class FilterByCategory(val category: String?) : EventListAction
    data class ChangeSortOrder(val sortOrder: SortOrder) : EventListAction
    data class OnEventClick(val eventId: String) : EventListAction
    data object OnCreateEventClick : EventListAction
    data class OnSearchQueryChanged (val query: String) : EventListAction
}