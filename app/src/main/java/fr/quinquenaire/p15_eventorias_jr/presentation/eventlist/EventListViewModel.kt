package fr.quinquenaire.p15_eventorias_jr.presentation.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.GetEventsUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.EventListMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.EventListUiState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.toUi
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListEffect
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
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.BY_DATE_ASC)
    private val _error = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")


    // Flow des événements mappés depuis Firestore
    private val _allEvents: StateFlow<List<EventListUiState>> = getEventsUseCase()
        .map { events -> events.map { it.toUi() } }
        .catch { e -> _error.update { e.message } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // État final exposé à la vue — combine les 4 sources
    val uiState: StateFlow<EventListMutableState> = combine(
        _allEvents,
        _selectedCategory,
        _sortOrder,
        _error,
        _searchQuery
    ) {  array ->
        // combine avec 5 sources utilise un tableau
        val events = array[0] as List<EventListUiState>
        val category = array[1] as String?
        val sortOrder = array[2] as SortOrder
        val error = array[3] as String?
        val query = array[4] as String

        EventListMutableState(
            events = events
                .filter { event ->
                    category == null || event.category == category }
                .filter { event ->
                    query.isBlank()
                            || event.name.contains(query, ignoreCase = true)
                            || event.locationName.contains(query, ignoreCase = true)}
                .let { list ->
                    when (sortOrder) {
                        SortOrder.DEFAULT -> list
                        SortOrder.BY_DATE_ASC  -> list.sortedBy { it.date }
                        SortOrder.BY_DATE_DESC-> list.sortedByDescending { it.date }
                        //SortOrder.BY_CATEGORY -> list.sortedBy { it.category }

                    }
                },
            isLoading = false,
            error = error,
            selectedCategory = category,
            sortOrder = sortOrder,
            searchQuery = query
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EventListMutableState(isLoading = true)
        )

    // Effets ponctuels
    private val _effect = MutableSharedFlow<EventListEffect>()
    val effect: SharedFlow<EventListEffect> = _effect.asSharedFlow()

    fun handleAction(action: EventListAction) {
        when (action) {
            is EventListAction.LoadEvents       -> Unit // géré par stateIn au démarrage
            is EventListAction.FilterByCategory -> _selectedCategory.update { action.category }
            is EventListAction.ChangeSortOrder  -> _sortOrder.update { action.sortOrder }
            is EventListAction.OnEventClick     -> onEventClick(action.eventId)
            is EventListAction.OnCreateEventClick -> OnCreateEventClick()
            is EventListAction.OnSearchQueryChanged -> _searchQuery.update { action.query }
        }
    }

    private fun onEventClick(eventId: String) {
        viewModelScope.launch {
            _effect.emit(EventListEffect.NavigateToEventDetail(eventId))
        }
    }

    private fun OnCreateEventClick() {
        viewModelScope.launch {
            _effect.emit(EventListEffect.NavigateToCreateEvent)
        }
    }
}