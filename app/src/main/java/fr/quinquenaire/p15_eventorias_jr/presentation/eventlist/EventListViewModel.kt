package fr.quinquenaire.p15_eventorias_jr.presentation.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.SortOrder
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.GetEventsUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.EventListMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.toUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {


    // --- États UI (Inputs) ---
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.BY_DATE_ASC)
    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)


    // --- Combinaison des inputs pour créer les paramètres de requête ---
    private val queryParamsFlow: StateFlow<EventQueryParams> = combine(
        _selectedCategory,
        _sortOrder,
        _searchQuery
    ) { category, sort, query ->
        EventQueryParams(
            category = category,
            sortOrder = sort,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventQueryParams()
    )

    // --- État Final ---

    val uiState: StateFlow<EventListMutableState> = combine(
        // 1. Appel UseCase avec le flow de paramètres
        queryParamsFlow.flatMapLatest { params ->
            getEventsUseCase(params)
                .catch { e ->
                    _error.update { e.message }
                    emit(emptyList())
                }
        },
        _error,
        _selectedCategory,
        _sortOrder,
        _searchQuery
    ) { events, error, category, sort, query ->
        EventListMutableState(
            events = events.map { it.toUi() },
            isLoading = false,
            error = error,
            selectedCategory = category,
            sortOrder = sort,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventListMutableState(isLoading = true)
    )

    // --- Effets & Actions  ---
    private val _effect = MutableSharedFlow<EventListEffect>()
    val effect: SharedFlow<EventListEffect> = _effect.asSharedFlow()

    fun handleAction(action: EventListAction) {
        when (action) {
            is EventListAction.FilterByCategory -> _selectedCategory.update { action.category }
            is EventListAction.ChangeSortOrder -> _sortOrder.update { action.sortOrder }
            is EventListAction.OnSearchQueryChanged -> _searchQuery.update { action.query }
            is EventListAction.OnEventClick -> onEventClick(action.eventId)
            is EventListAction.OnCreateEventClick -> onNavigateToCreate()
            is EventListAction.LoadEvents -> {
                _error.update { null }
            }
        }
    }

    private fun onEventClick(eventId: String) {
        viewModelScope.launch { _effect.emit(EventListEffect.NavigateToEventDetail(eventId)) }
    }

    private fun onNavigateToCreate() {
        viewModelScope.launch { _effect.emit(EventListEffect.NavigateToCreateEvent) }
    }
}
