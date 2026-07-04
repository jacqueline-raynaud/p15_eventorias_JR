package fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract

interface UserProfileEffect {
    data object NavigateToLogin : UserProfileEffect
    data class ShowSnackbar(val message: String) : UserProfileEffect
}