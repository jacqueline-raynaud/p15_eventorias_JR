package fr.quinquenaire.p15_eventorias_jr.domain

data class EventQueryParams(
    val category: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.BY_DATE_ASC,
    val limit: Int = 50 // ne pas tout charger en même temps
)