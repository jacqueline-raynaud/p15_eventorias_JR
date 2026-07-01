package fr.quinquenaire.p15_eventorias_jr.presentation.navigation

sealed class EventoriasDestinations(val route: String) {
    data object EventList : EventoriasDestinations("event_list")
    data object EventDetail : EventoriasDestinations("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    data object EventCreate : EventoriasDestinations("event_create")
    data object EventEdit : EventoriasDestinations("event_edit/{eventId}") {
        fun createRoute(eventId: String) = "event_edit/$eventId"
    }
    data object UserProfile : EventoriasDestinations("user_profile")
}