package fr.quinquenaire.p15_eventorias_jr

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.quinquenaire.p15_eventorias_jr.R
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.UserProfileContent
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract.UserProfileAction
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model.UserProfileMutableState
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.model.UserProfileUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Les données officielles de notre faux utilisateur (le dossier de base)
    private val fakeProfile = UserProfileUiState(
        uid = "user_123",
        firstName = "Jean",
        lastName = "Dupont",
        email = "jean.dupont@test.com",
        avatarUrl = "",
        notificationEnabled = false
    )

    // État 1 : Le dossier intact
    // Les champs "edited" sont à null par défaut.
    // Le getter "hasChanges" renverra donc "false" automatiquement.
    private val defaultState = UserProfileMutableState(
        profile = fakeProfile
    )

    // État 2 : Le dossier en cours de modification
    // On ajoute notre "post-it" : editedFirstName n'est plus null.
    // Le getter "hasChanges" renverra donc "true" automatiquement.
    private val modifiedState = defaultState.copy(
        editedFirstName = "Jean-Pierre"
    )

    @Test
    fun test_initial_display_hides_save_buttons_and_locks_email() {
        composeTestRule.setContent {
            UserProfileContent(
                uiState = defaultState,
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        // 1. On vérifie que les informations sont bien affichées
        composeTestRule.onNodeWithText("Jean").assertIsDisplayed()
        composeTestRule.onNodeWithText("jean.dupont@test.com").assertIsDisplayed()

        // 2. On vérifie que l'email n'est PAS éditable (readOnly = true)
        // L'action SetText n'est pas supportée sur un champ en lecture seule
        val emailLabel = composeTestRule.activity.getString(R.string.email)
        composeTestRule.onNodeWithText(emailLabel).assert(hasSetTextAction().not())

        // 3. On vérifie que le bouton "Enregistrer" n'existe pas encore
        val saveText = composeTestRule.activity.getString(R.string.save)
        composeTestRule.onNodeWithText(saveText).assertDoesNotExist()
    }
    @Test
    fun test_save_and_cancel_buttons_appear_when_changes_are_made() {
        composeTestRule.setContent {
            UserProfileContent(
                uiState = modifiedState, // <-- On utilise l'état modifié
                snackbarHostState = SnackbarHostState(),
                onAction = {}
            )
        }

        val saveText = composeTestRule.activity.getString(R.string.save)
        val cancelText = composeTestRule.activity.getString(R.string.cancel)

        // Les boutons doivent maintenant être visibles à l'écran
        composeTestRule.onNodeWithText(saveText)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(cancelText)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun test_delete_account_dialog_shows_and_emits_action() {
        var capturedAction: UserProfileAction? = null

        composeTestRule.setContent {
            UserProfileContent(
                uiState = defaultState.copy(showDeleteAccountConfirmation = true),
                snackbarHostState = SnackbarHostState(),
                onAction = { action -> capturedAction = action }
            )
        }

        val confirmTitle = composeTestRule.activity.getString(R.string.delete_account_confirm_title)

        // 1. On vérifie que la boîte de dialogue est bien ouverte (le titre suffit)
        composeTestRule.onNodeWithText(confirmTitle).assertIsDisplayed()

        // 2. On clique directement sur le bon bouton grâce à ton tag !
        composeTestRule.onNodeWithTag("confirm_delete_dialog_button").performClick()

        // 3. On vérifie que l'action est bien remontée
        assert(capturedAction is UserProfileAction.OnConfirmDeleteAccount) {
            "L'action de confirmation de suppression n'a pas été déclenchée"
        }
    }
}
