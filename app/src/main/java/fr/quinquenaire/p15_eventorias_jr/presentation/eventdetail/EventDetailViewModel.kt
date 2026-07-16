package fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.DeleteEventUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.EventDetailMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.toDetailUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getEventDetailUseCase: GetEventDetailUseCase,
    getUserProfileUseCase: GetUserProfileUseCase,
    getCurrentUidUseCase: GetCurrentUidUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    private val currentUid = getCurrentUidUseCase()

    private val _showDeleteConfirmation = MutableStateFlow(false)

    val uiState: StateFlow<EventDetailMutableState> =
        combine(
            getEventDetailUseCase(eventId),
            _showDeleteConfirmation
        ) { event, showConfirmation ->
            event to showConfirmation
        }.flatMapLatest { (event, showConfirmation) ->
            if (event == null) {
                flowOf(EventDetailMutableState(error = "Événement introuvable"))
            } else {
                getUserProfileUseCase(event.organizerId)
                    .map { profile ->
                        EventDetailMutableState(
                            event = event.toDetailUi(organizerAvatarUrl = profile?.avatarUrl ?: ""),
                            isLoading = false,
                            isOrganizer = event.organizerId == currentUid,
                            showDeleteConfirmation = showConfirmation
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
            is EventDetailAction.OnBackClick -> emitEffect(EventDetailEffect.NavigateBack)
            is EventDetailAction.OnEditClick -> emitEffect(EventDetailEffect.NavigateToEdit(eventId))
            is EventDetailAction.OnDeleteClick -> _showDeleteConfirmation.value = true
            is EventDetailAction.OnDismissDeleteDialog -> _showDeleteConfirmation.value = false
            is EventDetailAction.OnConfirmDelete -> confirmDelete()
            is EventDetailAction.OnRetry -> Unit // stateIn relance à la re-souscription

        }

    }

    private fun confirmDelete() {
        val state = uiState.value
        val event = state.event ?: return

        if (!state.isOrganizer) {
            _showDeleteConfirmation.value = false
            emitEffect(EventDetailEffect.ShowSnackbar("Seul l'organisateur peut supprimer cet événement"))
            return
        }

        viewModelScope.launch {
            try {
                deleteEventUseCase(eventId, event.imageUrl)
                emitEffect(EventDetailEffect.NavigateBack)
            } catch (e: Exception) {
                _showDeleteConfirmation.value = false
                emitEffect(EventDetailEffect.ShowSnackbar("Erreur lors de la suppression"))
            }
        }
    }

    private fun emitEffect(effect: EventDetailEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}
