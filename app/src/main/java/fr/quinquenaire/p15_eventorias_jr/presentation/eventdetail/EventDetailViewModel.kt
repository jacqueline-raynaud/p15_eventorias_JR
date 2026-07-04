package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.EventDetailMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.toDetailUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class) @HiltViewModel class EventDetailViewModel @Inject constructor( savedStateHandle: SavedStateHandle, getEventDetailUseCase: GetEventDetailUseCase, getUserProfileUseCase: GetUserProfileUseCase ) : ViewModel() {

    // L'eventId arrive automatiquement depuis la route de navigation
// "event_detail/{eventId}" via le SavedStateHandle — pas besoin de le
// passer manuellement au ViewModel.
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    val uiState: StateFlow<EventDetailMutableState> =
        getEventDetailUseCase(eventId)
            .flatMapLatest { event ->
                if (event == null) {

                    // événement introuvable (supprimé ?)
                    flowOf(EventDetailMutableState(error = "Événement introuvable"))
                } else {
                    // on enchaîne : événement reçu → on va chercher le profil
                    // de l'organisateur, et on combine les deux dans l'état
                    getUserProfileUseCase(event.organizerId)
                        .map { profile ->
                            EventDetailMutableState(
                                event = event.toDetailUi(
                                    organizerAvatarUrl = profile?.avatarUrl ?: ""
                                ),
                                isLoading = false
                            )
                        }
                }
            }

            .catch { e ->
                emit(EventDetailMutableState(error = e.message))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EventDetailMutableState(isLoading = true)
            )

    private val _effect = MutableSharedFlow<EventDetailEffect>()
    val effect: SharedFlow<EventDetailEffect> = _effect.asSharedFlow()
    fun handleAction(action: EventDetailAction) {
        when (action) {
            is EventDetailAction.OnBackClick   -> emitEffect(EventDetailEffect.NavigateBack)
            is EventDetailAction.OnEditClick   -> emitEffect(EventDetailEffect.NavigateToEdit(eventId))
            is EventDetailAction.OnDeleteClick -> onDeleteClick()
            is EventDetailAction.OnRetry       -> Unit // stateIn relance à la re-souscription

        }

    }

    private fun onDeleteClick() {
        // Suppression non implémentée pour l'instant — juste un feedback
        emitEffect(EventDetailEffect.ShowSnackbar("Suppression à venir"))
    }

    private fun emitEffect(effect: EventDetailEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}
