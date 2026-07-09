package fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract

import android.net.Uri

interface UserProfileAction {
    data class OnNotificationToggle(val enabled: Boolean) : UserProfileAction
    data object OnSignOutClick : UserProfileAction
    //modification du profile
    data class OnFirstNameChanged(val firstName: String) : UserProfileAction
    data class OnLastNameChanged(val lastName: String) : UserProfileAction
    data class OnAvatarSelected(val uri: Uri) : UserProfileAction
    data object OnSaveClick : UserProfileAction
    data object OnCancelEdit : UserProfileAction
    // suppression de compte
    data object OnDeleteAccountClick : UserProfileAction
    data object OnConfirmDeleteAccount : UserProfileAction
    data object OnDismissDeleteAccountDialog : UserProfileAction
}