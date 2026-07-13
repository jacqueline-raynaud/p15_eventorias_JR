package fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.quinquenaire.p15_eventorias_jr.R
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.presentation.common.eventcomponent.CategoryField
import fr.quinquenaire.p15_eventorias_jr.presentation.common.eventcomponent.DateField
import fr.quinquenaire.p15_eventorias_jr.presentation.common.eventcomponent.ImagePickerField
import fr.quinquenaire.p15_eventorias_jr.presentation.common.eventcomponent.TimeField
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventEffect
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.model.CreateEventMutableState
import kotlinx.coroutines.flow.collectLatest

// CreateEventScreen.kt

// ---------------------------------------------------------------------------
// Screen entry point — STATEFUL
// ---------------------------------------------------------------------------

@Composable
fun CreateEventScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateEventEffect.NavigateBack ->
                    onNavigateBack()

                is CreateEventEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CreateEventContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Content — STATELESS
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventContent(
    uiState: CreateEventMutableState,
    snackbarHostState: SnackbarHostState,
    onAction: (CreateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_event_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(CreateEventAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.Back_to_the_list)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onAction(CreateEventAction.OnNameChange(it)) },
                label = { Text(stringResource(R.string.event_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // image
            ImagePickerField(
                previewModel = uiState.imageUri,
                onImageSelected = { onAction(CreateEventAction.OnImageSelected(it)) }
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { onAction(CreateEventAction.OnDescriptionChange(it)) },
                label = { Text(stringResource(R.string.event_description)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Catégorie
            CategoryField(
                selectedCategory = uiState.category?.label.orEmpty(),  // adapte
                categories = EventCategory.entries,                     // adapte
                onCategorySelected = { onAction(CreateEventAction.OnCategoryChange(it)) }
            )

            // Date + heure côte à côte
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DateField(
                    dateLabel = uiState.dateLabel,
                    onDateSelected = { onAction(CreateEventAction.OnDateSelected(it)) },
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    timeLabel = uiState.timeLabel,
                    onTimeSelected = { h, m ->
                        onAction(CreateEventAction.OnTimeSelected(h, m))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Adresse
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { onAction(CreateEventAction.OnAddressChange(it)) },
                label = { Text(stringResource(R.string.event_address)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Bouton de validation
            Button(
                onClick = { onAction(CreateEventAction.OnSaveClick) },
                enabled = uiState.isFormValid && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.create_event_button))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Formulaire vide")
@Composable
private fun CreateEventContentEmptyPreview() {
    MaterialTheme {
        CreateEventContent(
            uiState = CreateEventMutableState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Formulaire rempli")
@Composable
private fun CreateEventContentFilledPreview() {
    MaterialTheme {
        CreateEventContent(
            uiState = CreateEventMutableState(
                name = "Soirée louange & partage",
                description = "Une soirée conviviale ouverte à tous.",
                dateMillis = 1783807200000L,   // ~ juillet 2026
                hour = 19,
                minute = 30,
                address = "12 rue de la Paix, 75002 Paris"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}