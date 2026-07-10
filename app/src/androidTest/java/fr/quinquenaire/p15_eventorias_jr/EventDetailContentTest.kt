package fr.quinquenaire.p15_eventorias_jr

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.EventDetailContent
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.EventDetailMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.model.EventDetailUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDetailContentTest {

    // Règle Compose standard (pas de Hilt nécessaire ici !)
    @get:Rule
    val composeTestRule = createComposeRule()

    // Notre fausse "pile" d'informations
    private val fakeEvent = EventDetailUiState(
        id = "1",
        name = "Soirée Jazz au Parc",
        description = "Une soirée inoubliable sous les étoiles.",
        date = "15 Juillet 2026",
        time = "20:00",
        locationName = "Parc de la Tête d'Or, Lyon",
        imageUrl = "",
        organizerId = "user123",
        organizerAvatarUrl = "",
        staticMapUrl = ""
    )

    @Test
    fun test_display_event_details_as_visitor() {
        // 1. On branche l'ampoule en mode "Visiteur" (isOrganizer = false)
        composeTestRule.setContent {
            EventDetailContent(
                uiState = EventDetailMutableState(
                    event = fakeEvent,
                    isLoading = false,
                    isOrganizer = false
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        // 2. Vérification des textes affichés
        composeTestRule.onNodeWithText("Soirée Jazz au Parc").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 Juillet 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Parc de la Tête d'Or, Lyon").assertIsDisplayed()

        // 3. Vérification de la sécurité : le bouton Menu ne doit PAS exister
        composeTestRule.onNodeWithContentDescription("Options de l'événement")
            .assertDoesNotExist() // Assure-toi que la stringResource correspond à "Options de l'événement"
    }

    @Test
    fun test_organizer_menu_is_displayed_and_clickable() {
        // 1. On branche l'ampoule en mode "Organisateur" (isOrganizer = true)
        composeTestRule.setContent {
            EventDetailContent(
                uiState = EventDetailMutableState(
                    event = fakeEvent,
                    isLoading = false,
                    isOrganizer = true
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        // 2. On trouve le bouton du menu et on clique dessus
        val menuDesc = "Event Detail Menu"
        composeTestRule.onNodeWithContentDescription(menuDesc).performClick()

        // 3. On vérifie que le menu déroulant s'est ouvert avec les bonnes options
        composeTestRule.onNodeWithText("Edit Event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Event").assertIsDisplayed()
    }

    @Test
    fun test_delete_dialog_shows_and_confirms() {
        // Variable pour espionner si l'action OnConfirmDelete a bien été appelée
        var deleteConfirmed = false

        composeTestRule.setContent {
            EventDetailContent(
                uiState = EventDetailMutableState(
                    event = fakeEvent,
                    isOrganizer = true,
                    showDeleteConfirmation = true // On force l'affichage de la modale
                ),
                snackbarHostState = SnackbarHostState(),
                onAction = { action ->
                    // Si l'action reçue est "Confirmer la suppression", on passe notre espion à true
                    if (action is EventDetailAction.OnConfirmDelete) {
                        deleteConfirmed = true
                    }
                }
            )
        }

        // 1. On vérifie que la modale est bien à l'écran
        composeTestRule.onNodeWithText("Did your confirm delete event ?")
            .assertIsDisplayed() // Remplace par ta string exacte

        // 2. On clique sur le bouton de confirmation (en rouge)
        composeTestRule.onNodeWithText("Delete Event").performClick()

        // 3. On affirme que notre espion a bien été déclenché
        assert(deleteConfirmed) { "L'action OnConfirmDelete aurait dû être déclenchée" }
    }
}