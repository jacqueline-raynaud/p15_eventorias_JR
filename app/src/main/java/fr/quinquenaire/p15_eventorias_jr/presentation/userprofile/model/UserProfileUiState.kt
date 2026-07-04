package fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model

import androidx.compose.runtime.Immutable
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile

@Immutable
data class UserProfileUiState(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val notificationEnabled: Boolean = false
)

fun UserProfile.toUi(): UserProfileUiState = UserProfileUiState(
    uid = uid,
    firstName = firstName,
    lastName = lastName,
    email = email,
    avatarUrl = avatarUrl,
    notificationEnabled = notificationEnabled
)

