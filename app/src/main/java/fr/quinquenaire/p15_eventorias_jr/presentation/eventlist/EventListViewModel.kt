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

    /*private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.BY_DATE_ASC)
    private val _error = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _reloadTrigger = MutableStateFlow(0)*/

    // --- États UI (Inputs) ---
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.BY_DATE_ASC)
    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    // Flow des événements mappés depuis Firestore
    /* private val _allEvents: StateFlow<List<EventListUiState>> = getEventsUseCase()
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
     ) {  events, category, sortOrder, error, query ->

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
                         SortOrder.BY_DATE_ASC  -> list.sortedBy { it.rawDate?.seconds ?:0 }
                         SortOrder.BY_DATE_DESC-> list.sortedByDescending { it.rawDate?.seconds ?:0  }
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
             is EventListAction.LoadEvents       -> {
                 _error.update { null }        // efface l'erreur
                 _reloadTrigger.update { it + 1 }  // relance flatMapLatest
             }
             is EventListAction.FilterByCategory -> _selectedCategory.update { action.category }
             is EventListAction.ChangeSortOrder  -> _sortOrder.update { action.sortOrder }
             is EventListAction.OnEventClick     -> onEventClick(action.eventId)
             is EventListAction.OnCreateEventClick -> OnCreateEventClick()
             is EventListAction.OnSearchQueryChanged -> _searchQuery.update { action.query }
         }
     }*/
// --- Combinaison des inputs pour créer les paramètres de requête ---
    // Dès que l'utilisateur change un filtre, ce flow émet un nouveau "params"
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

    // --- État Final (Output) ---

    val uiState: StateFlow<EventListMutableState> = combine(
        // 1. On appelle le UseCase avec le flow de paramètres > Ça relance la requête automatiquement
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

    // --- Effets & Actions (Inchangés, car purement UI) ---
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

    /*private fun onEventClick(eventId: String) {
        viewModelScope.launch {
            _effect.emit(EventListEffect.NavigateToEventDetail(eventId))
        }
    }

    private fun OnCreateEventClick() {
        viewModelScope.launch {
            _effect.emit(EventListEffect.NavigateToCreateEvent)
        }
    }*/
    private fun onEventClick(eventId: String) {
        viewModelScope.launch { _effect.emit(EventListEffect.NavigateToEventDetail(eventId)) }
    }

    private fun onNavigateToCreate() {
        viewModelScope.launch { _effect.emit(EventListEffect.NavigateToCreateEvent) }
    }
}
