package fr.quinquenaire.p15_eventorias_jr

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.quinquenaire.p15_eventorias_jr.R
import fr.quinquenaire.p15_eventorias_jr.TestActivity
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.CreateEventContent
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.model.CreateEventMutableState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateEventContentTest {

    // On utilise ComponentActivity pour avoir accès à getString()
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // État 1 : Incomplet (isFormValid renverra automatiquement false)
    private val emptyState = CreateEventMutableState(
        name = "",
        category = null,
        dateMillis = null,
        hour = null,
        address = ""
    )

    // État 2 : Complet (isFormValid renverra automatiquement true grâce uax conditions)
    private val validState = CreateEventMutableState(
        name = "Mon super événement",
        category = EventCategory.MUSIQUE,
        dateMillis = 1783807200000L,
        hour = 19,
        address = "25 place des Pavillons, 69007 Lyon"
    )


    @Test
    fun test_save_button_is_disabled_when_form_is_invalid() {
        composeTestRule.setContent {
            CreateEventContent(
                uiState = emptyState, // isFormValid y est "false"
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        val buttonText = composeTestRule.activity.getString(R.string.create_event_button)

        composeTestRule.onNodeWithText(buttonText)
            .performScrollTo() //scroll to the button
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun test_typing_in_name_field_emits_action() {
        var capturedAction: CreateEventAction? = null

        composeTestRule.setContent {
            CreateEventContent(
                uiState = emptyState,
                snackbarHostState = SnackbarHostState(),
                onAction = { action -> capturedAction = action }
            )
        }

        val nameLabel = composeTestRule.activity.getString(R.string.event_name)

        // 1. On écrit dans le champ
        composeTestRule.onNodeWithText(nameLabel).performTextInput("Mon super événement")

        // 2. On transforme (cast) de façon sécurisée
        val action = capturedAction as? CreateEventAction.OnNameChange

        // 3. On vérifie que ce n'est pas null (donc que c'était bien le bon type d'action)
        assert(action != null) { "L'action déclenchée n'est pas OnNameChange" }

        // 4. On vérifie le contenu avec ".value"
        assert(action?.value == "Mon super événement") {
            "Le texte capturé ne correspond pas"
        }
    }

    @Test
    fun test_loading_state_hides_button_text() {
        composeTestRule.setContent {
            CreateEventContent(
                // On part du passeport complet, et on ajoute juste le tampon "sauvegarde en cours"
                uiState = validState.copy(isSaving = true),
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        val buttonText = composeTestRule.activity.getString(R.string.create_event_button)

        // Le texte du bouton disparaît au profit du CircularProgressIndicator
        composeTestRule.onNodeWithText(buttonText)
            .assertDoesNotExist()
    }


}