package fr.quinquenaire.p15_eventorias_jr.viewmodels

import android.net.Uri
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.DeleteAccountUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateNotificationSettingUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.UserProfileViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract.UserProfileAction
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.contract.UserProfileEffect
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain


@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val uid = "uid1"
    val fakeProfile = UserProfile(
        uid = uid,
        firstName = "Jacqueline",
        lastName = "G",
        email = "j@test.com",
        avatarUrl = "https://storage/avatar.jpg",
        notificationEnabled = false
    )

    fun buildViewModel(
        profileFlow: Flow<UserProfile?> = flowOf(fakeProfile),
        notificationThrows: Exception? = null,
        updateProfileThrows: Exception? = null,
        deleteAccountThrows: Exception? = null
    ): TestBundle {
        val getCurrentUserProfileUseCase = mockk<GetCurrentUserProfileUseCase>()
        val updateNotificationSettingUseCase = mockk<UpdateNotificationSettingUseCase>()
        val updateUserProfileUseCase = mockk<UpdateUserProfileUseCase>()
        val deleteAccountUseCase = mockk<DeleteAccountUseCase>()

        every { getCurrentUserProfileUseCase() } returns profileFlow

        if (notificationThrows == null) {
            coEvery { updateNotificationSettingUseCase(any()) } just Runs
        } else {
            coEvery { updateNotificationSettingUseCase(any()) } throws notificationThrows
        }

        val profileSlot = slot<UserProfile>()
        if (updateProfileThrows == null) {
            coEvery { updateUserProfileUseCase(capture(profileSlot), any()) } just Runs
        } else {
            coEvery {
                updateUserProfileUseCase(
                    capture(profileSlot),
                    any()
                )
            } throws updateProfileThrows
        }

        if (deleteAccountThrows == null) {
            coEvery { deleteAccountUseCase() } just Runs
        } else {
            coEvery { deleteAccountUseCase() } throws deleteAccountThrows
        }

        val viewModel = UserProfileViewModel(
            getCurrentUserProfileUseCase = getCurrentUserProfileUseCase,
            updateNotificationSettingUseCase = updateNotificationSettingUseCase,
            updateUserProfileUseCase = updateUserProfileUseCase,
            deleteAccountUseCase = deleteAccountUseCase
        )

        return TestBundle(
            viewModel, updateNotificationSettingUseCase,
            updateUserProfileUseCase, deleteAccountUseCase, profileSlot
        )
    }

    // -----------------------------------------------------------
    // Chargement du profil
    // -----------------------------------------------------------

    Given("un profil existant") {
        val bundle = buildViewModel()

        Then("il est mappé en UiState") {
            bundle.viewModel.uiState.test {
                val state = awaitItem()
                state.profile?.firstName shouldBe "Jacqueline"
                state.profile?.email shouldBe "j@test.com"
                state.error shouldBe null
            }
        }
    }

    Given("aucun profil trouvé (hors suppression en cours)") {
        val bundle = buildViewModel(profileFlow = flowOf(null))

        Then("l'erreur \"Profil introuvable\" s'affiche") {
            bundle.viewModel.uiState.test {
                awaitItem().error shouldBe "Profil introuvable"
            }
        }
    }

    Given("le chargement du profil échoue") {
        val bundle = buildViewModel(
            profileFlow = flow { throw RuntimeException("Erreur réseau") }
        )

        Then("l'erreur est capturée") {
            bundle.viewModel.uiState.test {
                awaitItem().error shouldBe "Erreur réseau"
            }
        }
    }

    // -----------------------------------------------------------
    // Édition du brouillon
    // -----------------------------------------------------------

    Given("un profil chargé") {
        val bundle = buildViewModel()

        When("OnFirstNameChanged") {
            Then("le brouillon et displayedFirstName sont mis à jour") {
                bundle.viewModel.uiState.test {
                    awaitItem() // réveil du StateFlow
                    bundle.viewModel.handleAction(UserProfileAction.OnFirstNameChanged("Nouveau"))
                    val state = awaitItem()
                    state.editedFirstName shouldBe "Nouveau"
                    state.displayedFirstName shouldBe "Nouveau"
                    state.hasChanges shouldBe true
                }
            }
        }

        When("OnCancelEdit après une modification") {
            Then("le brouillon est effacé") {
                bundle.viewModel.uiState.test {
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnLastNameChanged("NouveauNom"))
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnCancelEdit)
                    val state = awaitItem()
                    state.editedLastName shouldBe null
                    state.hasChanges shouldBe false
                    state.displayedLastName shouldBe "G" // retombe sur le profil réel
                }
            }
        }
    }

    // -----------------------------------------------------------
    // Sauvegarde du profil
    // -----------------------------------------------------------

    Given("aucune modification en attente") {
        val bundle = buildViewModel()

        When("OnSaveClick") {
            Then("aucune sauvegarde n'est tentée") {
                bundle.viewModel.uiState.test { awaitItem() }
                bundle.viewModel.handleAction(UserProfileAction.OnSaveClick)
                coVerify(exactly = 0) { bundle.updateUserProfileUseCase(any(), any()) }
            }
        }
    }


    Given("des modifications valides en attente") {
        val bundle = buildViewModel()
        val newAvatarUri = mockk<Uri>()

        When("OnSaveClick") {
            Then("le profil est sauvegardé, le brouillon effacé, un snackbar de succès") {
                bundle.viewModel.uiState.test {
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnFirstNameChanged("Jacky"))
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnAvatarSelected(newAvatarUri))
                    awaitItem()

                    bundle.viewModel.effect.test {
                        bundle.viewModel.handleAction(UserProfileAction.OnSaveClick)
                        awaitItem() shouldBe UserProfileEffect.ShowSnackbar("Profil mis à jour")
                    }

                    // Draine les émissions intermédiaires (isSaving=true, puis les 3 .update de clearDraft)
                    var state = awaitItem()
                    while (state.isSaving || state.hasChanges) {
                        state = awaitItem()
                    }

                    state.editedFirstName shouldBe null
                    state.isSaving shouldBe false
                }

                coVerify(exactly = 1) { bundle.updateUserProfileUseCase(any(), newAvatarUri) }
                bundle.updateProfileSlot.captured.firstName shouldBe "Jacky"
                bundle.updateProfileSlot.captured.uid shouldBe uid
            }
        }
    }

    Given("la sauvegarde du profil échoue") {
        val bundle = buildViewModel(updateProfileThrows = RuntimeException("boom"))

        When("OnSaveClick avec des modifications") {
            Then("un snackbar d'erreur s'affiche et le brouillon n'est pas effacé") {
                bundle.viewModel.uiState.test {
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnFirstNameChanged("Jacky"))
                    awaitItem()

                    bundle.viewModel.effect.test {
                        bundle.viewModel.handleAction(UserProfileAction.OnSaveClick)
                        awaitItem() shouldBe UserProfileEffect.ShowSnackbar("Erreur : boom")
                    }

                    var state = awaitItem()
                    while (state.isSaving) {
                        state = awaitItem()
                    }

                    state.editedFirstName shouldBe "Jacky" // pas effacé
                    state.isSaving shouldBe false
                }
            }
        }
    }


    // -----------------------------------------------------------
    // Notifications
    // -----------------------------------------------------------

    Given("un profil chargé") {
        val bundle = buildViewModel()

        When("OnNotificationToggle(true)") {
            Then("le use case est appelé avec la bonne valeur") {
                bundle.viewModel.uiState.test { awaitItem() }
                bundle.viewModel.handleAction(UserProfileAction.OnNotificationToggle(true))
                coVerify(exactly = 1) { bundle.updateNotificationSettingUseCase(true) }
            }
        }
    }

    Given("la mise à jour de la préférence de notification échoue") {
        val bundle = buildViewModel(notificationThrows = RuntimeException("network"))

        When("OnNotificationToggle") {
            Then("un snackbar d'erreur s'affiche") {
                bundle.viewModel.uiState.test { awaitItem() }
                bundle.viewModel.effect.test {
                    bundle.viewModel.handleAction(UserProfileAction.OnNotificationToggle(true))
                    awaitItem() shouldBe UserProfileEffect.ShowSnackbar("Erreur : network")
                }
            }
        }
    }

    // -----------------------------------------------------------
    // Déconnexion
    // -----------------------------------------------------------

    Given("un profil chargé") {
        val bundle = buildViewModel()

        When("OnSignOutClick") {
            Then("l'effet NavigateToLogin est émis") {
                bundle.viewModel.uiState.test { awaitItem() }
                bundle.viewModel.effect.test {
                    bundle.viewModel.handleAction(UserProfileAction.OnSignOutClick)
                    awaitItem() shouldBe UserProfileEffect.NavigateToLogin
                }
            }
        }
    }

    // -----------------------------------------------------------
    // Suppression de compte
    // -----------------------------------------------------------

    Given("un profil chargé") {
        val bundle = buildViewModel()

        When("OnDeleteAccountClick") {
            Then("la boîte de confirmation s'affiche") {
                bundle.viewModel.uiState.test {
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnDeleteAccountClick)
                    awaitItem().showDeleteAccountConfirmation shouldBe true
                }
            }
        }

        When("OnDismissDeleteAccountDialog après ouverture") {
            Then("la boîte de confirmation se ferme") {
                bundle.viewModel.uiState.test {
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnDeleteAccountClick)
                    awaitItem()
                    bundle.viewModel.handleAction(UserProfileAction.OnDismissDeleteAccountDialog)
                    awaitItem().showDeleteAccountConfirmation shouldBe false
                }
            }
        }
    }

    Given("la suppression de compte réussit") {
        val bundle = buildViewModel()

        When("OnConfirmDeleteAccount") {
            Then("le use case est appelé et NavigateToLogin est émis") {
                bundle.viewModel.uiState.test {
                    awaitItem()

                    bundle.viewModel.effect.test {
                        bundle.viewModel.handleAction(UserProfileAction.OnConfirmDeleteAccount)
                        awaitItem() shouldBe UserProfileEffect.NavigateToLogin
                    }

                    var state = awaitItem()
                    while (state.isDeletingAccount) {
                        state = awaitItem()
                    }
                }
                coVerify(exactly = 1) { bundle.deleteAccountUseCase() }
            }
        }
    }

    Given("la suppression échoue pour cause de reconnexion trop ancienne") {
        val bundle = buildViewModel(
            deleteAccountThrows = mockk<FirebaseAuthRecentLoginRequiredException>()
        )

        When("OnConfirmDeleteAccount") {
            Then("un snackbar explicite puis NavigateToLogin sont émis, la boîte se ferme") {
                bundle.viewModel.uiState.test {
                    awaitItem()

                    bundle.viewModel.effect.test {
                        bundle.viewModel.handleAction(UserProfileAction.OnConfirmDeleteAccount)
                        awaitItem() shouldBe UserProfileEffect.ShowSnackbar(
                            "Pour des raisons de sécurité, reconnecte-toi puis réessaie"
                        )
                        awaitItem() shouldBe UserProfileEffect.NavigateToLogin
                    }

                    var state = awaitItem()
                    while (state.isDeletingAccount) {
                        state = awaitItem()
                    }
                    state.showDeleteAccountConfirmation shouldBe false
                }
            }
        }
    }

    Given("la suppression échoue pour une raison générique") {
        val bundle = buildViewModel(deleteAccountThrows = RuntimeException("boom"))

        When("OnConfirmDeleteAccount") {
            Then("un snackbar d'erreur générique s'affiche, la boîte se ferme") {
                bundle.viewModel.uiState.test {
                    awaitItem()

                    bundle.viewModel.effect.test {
                        bundle.viewModel.handleAction(UserProfileAction.OnConfirmDeleteAccount)
                        awaitItem() shouldBe UserProfileEffect.ShowSnackbar(
                            "Erreur lors de la suppression du compte"
                        )
                    }

                    var state = awaitItem()
                    while (state.isDeletingAccount) {
                        state = awaitItem()
                    }
                    state.showDeleteAccountConfirmation shouldBe false
                }
            }
        }
    }
})

private data class TestBundle(
    val viewModel: UserProfileViewModel,
    val updateNotificationSettingUseCase: UpdateNotificationSettingUseCase,
    val updateUserProfileUseCase: UpdateUserProfileUseCase,
    val deleteAccountUseCase: DeleteAccountUseCase,
    val updateProfileSlot: CapturingSlot<UserProfile>
)