package fr.quinquenaire.p15_eventorias_jr.presentation.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import fr.quinquenaire.p15_eventorias_jr.R
import fr.quinquenaire.p15_eventorias_jr.presentation.event.model.EventListUiState
import fr.quinquenaire.p15_eventorias_jr.presentation.event.viewmodel.contract.EventListAction
import fr.quinquenaire.p15_eventorias_jr.presentation.event.viewmodel.contract.EventListEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.event.SortOrder
import kotlinx.coroutines.flow.collectLatest

// ---------------------------------------------------------------------------
// Screen entry point
// ---------------------------------------------------------------------------

@Composable
fun EventListScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Effets ponctuels (navigation, snackbar)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EventListEffect.NavigateToEventDetail ->
                    onNavigateToDetail(effect.eventId)

                is EventListEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EventListTopBar(
                sortOrder = uiState.sortOrder,
                onSortOrderChanged = { order ->
                    viewModel.handleAction(EventListAction.ChangeSortOrder(order))
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            // Barre de filtres catégories
            CategoryFilterBar(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.handleAction(EventListAction.FilterByCategory(category))
                }
            )

            // Zone principale : loading / error / liste
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> LoadingContent()
                    uiState.error != null -> ErrorContent(
                        message = uiState.error!!,
                        onRetry = { viewModel.handleAction(EventListAction.LoadEvents) }
                    )
                    uiState.events.isEmpty() -> EmptyContent()
                    else -> EventList(
                        events = uiState.events,
                        onEventClick = { eventId ->
                            viewModel.handleAction(EventListAction.OnEventClick(eventId))
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TopBar avec tri
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListTopBar(
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.event_list_title)) },
        actions = {
            // Bouton tri par date
            IconButton(
                onClick = { onSortOrderChanged(SortOrder.BY_DATE) }
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.sort_by_date),
                    tint = if (sortOrder == SortOrder.BY_DATE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Bouton tri par catégorie
            IconButton(
                onClick = { onSortOrderChanged(SortOrder.BY_CATEGORY) }
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = stringResource(R.string.sort_by_category),
                    tint = if (sortOrder == SortOrder.BY_CATEGORY)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Filtre catégories (chips horizontaux)
// ---------------------------------------------------------------------------

private val CATEGORIES = listOf("Musique", "Sport", "Canicule", "Art", "Famille")

@Composable
private fun CategoryFilterBar(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Chip "Tous"
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(text = stringResource(R.string.filter_all)) }
            )
        }
        // Chips catégories
        items(CATEGORIES) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(
                        if (selectedCategory == category) null else category
                    )
                },
                label = { Text(text = category) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// État : chargement
// ---------------------------------------------------------------------------

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
}

// ---------------------------------------------------------------------------
// État : erreur
// ---------------------------------------------------------------------------

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

// ---------------------------------------------------------------------------
// État : liste vide
// ---------------------------------------------------------------------------

@Composable
private fun EmptyContent() {
    Text(
        text = stringResource(R.string.event_list_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ---------------------------------------------------------------------------
// Liste des événements
// ---------------------------------------------------------------------------

@Composable
private fun EventList(
    events: List<EventListUiState>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = events,
            key = { it.id }           // stabilise les recompositions
        ) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event.id) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Carte événement
// ---------------------------------------------------------------------------

@Composable
private fun EventCard(
    event: EventListUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // Image événement
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )

            // Informations
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge catégorie
                CategoryBadge(category = event.category)

                // Nom
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Date & heure
                Text(
                    text = "${event.date} · ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Lieu
                Text(
                    text = event.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Badge catégorie
// ---------------------------------------------------------------------------

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}