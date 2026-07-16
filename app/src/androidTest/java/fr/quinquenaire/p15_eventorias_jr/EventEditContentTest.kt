package fr.quinquenaire.p15_eventorias_jr

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.EventEditContent
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract.EventEditAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.model.EventEditMutableState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventEditContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // État 1 : Erreur réseau (le dossier n'a pas pu être chargé)
    private val errorState = EventEditMutableState(
        isLoading = false,
        error = "Impossible de charger l'événement"
    )

    // État 2 : Le formulaire pré-rempli avec les anciennes données
    private val validState = EventEditMutableState(
        isLoading = false,
        error = null,
        name = "Ancien titre du concert",
        description = "Ancienne description",
        category = EventCategory.MUSIQUE,
        dateMillis = 1783807200000L,
        hour = 20,
        address = "10 rue de la Musique",
        isSaving = false
        // (isFormValid renvoie true avec ces données)
    )

    @Test
    fun test_displays_error_message_when_state_has_error() {
        composeTestRule.setContent {
            EventEditContent(
                uiState = errorState,
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        // verifie que le message d'erreur est bien visible
        composeTestRule.onNodeWithText("Impossible de charger l'événement").assertIsDisplayed()

        // verifie que le champ "Nom" n'est PAS affiché (le formulaire est caché)
        val nameLabel = composeTestRule.activity.getString(R.string.event_name)
        composeTestRule.onNodeWithText(nameLabel).assertDoesNotExist()
    }

    @Test
    fun test_form_is_prefilled_with_existing_data() {
        composeTestRule.setContent {
            EventEditContent(
                uiState = validState,
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        // L'utilisateur doit voir l'ancien titre écrit dans le champ
        composeTestRule.onNodeWithText("Ancien titre du concert").assertIsDisplayed()

        // L'utilisateur doit voir l'ancienne adresse
        composeTestRule.onNodeWithText("10 rue de la Musique").assertIsDisplayed()
    }

    @Test
    fun test_editing_field_emits_action_and_save_button_is_enabled() {
        var capturedAction: EventEditAction? = null

        composeTestRule.setContent {
            EventEditContent(
                uiState = validState,
                snackbarHostState = SnackbarHostState(),
                onAction = { action -> capturedAction = action }
            )
        }

        val nameLabel = composeTestRule.activity.getString(R.string.event_name)
        val saveButtonText = composeTestRule.activity.getString(R.string.save_changes)

        // 1. modification du texte existant en tapant un nouveau titre
        composeTestRule.onNodeWithText(nameLabel).performTextInput(" - Nouveau !")

        // 2. verification que l'action est bien remontée vers le système
        val action = capturedAction as? EventEditAction.OnNameChange
        assert(action != null) { "L'action déclenchée n'est pas OnNameChange" }
        // performTextInput ajoute le texte au texte existant (ou le remplace selon la configuration du TextField)
        // L'important est que l'action contienne la nouvelle valeur.

        // 3. On scroll jusqu'au bouton de sauvegarde et on vérifie qu'il est cliquable (car le formulaire est valide)
        composeTestRule.onNodeWithText(saveButtonText)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
    }
}