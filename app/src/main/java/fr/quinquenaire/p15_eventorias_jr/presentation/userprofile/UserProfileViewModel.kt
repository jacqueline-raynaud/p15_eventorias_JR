package fr.quinquenaire.p15_eventorias_jr.presentation.userprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateNotificationSettingUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract.UserProfileAction
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract.UserProfileEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model.UserProfileMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model.toUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
    private val updateNotificationSettingUseCase: UpdateNotificationSettingUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {
    // brouillon d'édition — null tant que l'utilisateur n'a rien modifié
    private val _editedFirstName = MutableStateFlow<String?>(null)
    private val _editedLastName = MutableStateFlow<String?>(null)
    private val _editedAvatarUri = MutableStateFlow<Uri?>(null)
    private val _isSaving = MutableStateFlow(false)

    val uiState: StateFlow<UserProfileMutableState> = combine(
        getCurrentUserProfileUseCase(),
        _editedFirstName,
        _editedLastName,
        _editedAvatarUri,
        _isSaving
    ) { array ->
        // combine à 5 sources → tableau (comme dans EventListViewModel)
        @Suppress("UNCHECKED_CAST")
        val profile = array[0] as UserProfile?
        val editedFirstName = array[1] as String?
        val editedLastName = array[2] as String?
        val editedAvatarUri = array[3] as Uri?
        val isSaving = array[4] as Boolean

        if (profile == null) {
            UserProfileMutableState(error = "Profil introuvable")
        } else {
            UserProfileMutableState(
                profile = profile.toUi(),
                editedFirstName = editedFirstName,
                editedLastName = editedLastName,
                editedAvatarUri = editedAvatarUri,
                isSaving = isSaving
            )
        }
    }
        .catch { e ->
            emit(UserProfileMutableState(error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserProfileMutableState(isLoading = true)
        )

    private val _effect = MutableSharedFlow<UserProfileEffect>()
    val effect: SharedFlow<UserProfileEffect> = _effect.asSharedFlow()

    fun handleAction(action: UserProfileAction) {
        when (action) {
            is UserProfileAction.OnNotificationToggle -> onNotificationToggle(action.enabled)
            is UserProfileAction.OnSignOutClick       -> onSignOutClick()
            is UserProfileAction.OnFirstNameChanged   -> _editedFirstName.update { action.firstName }
            is UserProfileAction.OnLastNameChanged    -> _editedLastName.update { action.lastName }
            is UserProfileAction.OnAvatarSelected     -> _editedAvatarUri.update { action.uri }
            is UserProfileAction.OnSaveClick          -> onSaveClick()
            is UserProfileAction.OnCancelEdit         -> clearDraft()
        }
    }

    private fun onSaveClick() {
        val current = uiState.value
        val profile = current.profile ?: return
        if (!current.hasChanges) return

        viewModelScope.launch {
            _isSaving.update { true }
            try {
                updateUserProfileUseCase(
                    profile = UserProfile(
                        uid = profile.uid,
                        firstName = current.displayedFirstName,
                        lastName = current.displayedLastName,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,   // remplacé par le use case si nouvel avatar
                        notificationEnabled = profile.notificationEnabled
                    ),
                    newAvatarUri = current.editedAvatarUri
                )
                clearDraft()
                _effect.emit(UserProfileEffect.ShowSnackbar("Profil mis à jour"))
            } catch (e: Exception) {
                _effect.emit(UserProfileEffect.ShowSnackbar("Erreur : ${e.message}"))
            } finally {
                _isSaving.update { false }
            }
        }
    }

    private fun clearDraft() {
        _editedFirstName.update { null }
        _editedLastName.update { null }
        _editedAvatarUri.update { null }
    }


    private fun onNotificationToggle(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updateNotificationSettingUseCase(enabled)
                // pas besoin de mettre à jour l'état à la main :
                // le snapshotListener Firestore re-émet le profil modifié
                // et le stateIn recalcule automatiquement
            } catch (e: Exception) {
                _effect.emit(
                    UserProfileEffect.ShowSnackbar("Erreur : ${e.message}")
                )
            }
        }
    }

    private fun onSignOutClick() {
        viewModelScope.launch {
            _effect.emit(UserProfileEffect.NavigateToLogin)
        }
    }
}