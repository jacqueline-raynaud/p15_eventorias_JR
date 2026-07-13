package fr.quinquenaire.p15_eventorias_jr.presentation.eventedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.UpdateEventUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract.EventEditAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract.EventEditEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.model.EventEditMutableState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EventEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventDetailUseCase: GetEventDetailUseCase,
    private val updateEventUseCase: UpdateEventUseCase
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventEditMutableState())
    val uiState: StateFlow<EventEditMutableState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EventEditEffect>()
    val effect: SharedFlow<EventEditEffect> = _effect.asSharedFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val event = getEventDetailUseCase(eventId).first()   // instantané, pas d'écoute continue
                if (event == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Événement introuvable") }
                    return@launch
                }

                val calendar = event.date?.toDate()?.let {
                    Calendar.getInstance().apply { time = it }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = event.name,
                        description = event.description,
                        category = EventCategory.entries.find { c -> c.name == event.category },
                        dateMillis = event.date?.toDate()?.time,
                        hour = calendar?.get(Calendar.HOUR_OF_DAY),
                        minute = calendar?.get(Calendar.MINUTE),
                        address = event.locationName,
                        existingImageUrl = event.imageUrl,
                        organizerId = event.organizerId,
                        guests = event.guests,
                        initialAddress = event.locationName,
                        initialLocation = event.location
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erreur de chargement") }
            }
        }
    }

    fun handleAction(action: EventEditAction) {
        when (action) {
            is EventEditAction.OnNameChange ->
                _uiState.update { it.copy(name = action.value) }

            is EventEditAction.OnDescriptionChange ->
                _uiState.update { it.copy(description = action.value) }

            is EventEditAction.OnCategoryChange ->
                _uiState.update { it.copy(category = action.value) }

            is EventEditAction.OnDateSelected ->
                _uiState.update { it.copy(dateMillis = action.millis) }

            is EventEditAction.OnTimeSelected ->
                _uiState.update { it.copy(hour = action.hour, minute = action.minute) }

            is EventEditAction.OnAddressChange ->
                _uiState.update { it.copy(address = action.value) }

            is EventEditAction.OnImageSelected ->
                _uiState.update { it.copy(imageUri = action.uri) }

            is EventEditAction.OnSaveClick -> saveEvent()

            is EventEditAction.OnBackClick -> emitEffect(EventEditEffect.NavigateBack)

            is EventEditAction.OnRetry -> loadEvent()
        }
    }

    private fun saveEvent() {
        val state = _uiState.value
        if (!state.isFormValid || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val addressChanged = state.address.trim() != state.initialAddress

                val event = Event(
                    id = eventId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    date = buildTimestamp(state.dateMillis!!, state.hour!!, state.minute!!),
                    locationName = state.address.trim(),
                    // null = signal au repository "regéocoder" ; sinon on garde la localisation actuelle
                    location = if (addressChanged) null else state.initialLocation,
                    category = state.category!!.name,
                    imageUrl = state.existingImageUrl,
                    organizerId = state.organizerId,
                    guests = state.guests
                )

                updateEventUseCase(event, state.imageUri)
                emitEffect(EventEditEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                emitEffect(EventEditEffect.ShowSnackbar("Erreur lors de la modification de l'événement"))
            }
        }
    }

    private fun buildTimestamp(dateMillis: Long, hour: Int, minute: Int): Timestamp {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    private fun emitEffect(effect: EventEditEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}