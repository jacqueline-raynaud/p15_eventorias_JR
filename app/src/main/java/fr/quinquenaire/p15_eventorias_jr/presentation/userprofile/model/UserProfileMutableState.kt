package fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model

import android.net.Uri

data class UserProfileMutableState(
    val profile: UserProfileUiState? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Brouillon d'édition — null tant que l'utilisateur n'a rien modifié
    val editedFirstName: String? = null,
    val editedLastName: String? = null,
    val editedAvatarUri: Uri? = null,
    val isSaving: Boolean = false
)
{

    /** Y a-t-il des modifications non enregistrées ? */
    val hasChanges: Boolean
        get() = editedFirstName != null || editedLastName != null || editedAvatarUri != null

    /** Valeurs à afficher : brouillon si présent, sinon profil Firestore */
    val displayedFirstName: String
        get() = editedFirstName ?: profile?.firstName ?: ""

    val displayedLastName: String
        get() = editedLastName ?: profile?.lastName ?: ""
}