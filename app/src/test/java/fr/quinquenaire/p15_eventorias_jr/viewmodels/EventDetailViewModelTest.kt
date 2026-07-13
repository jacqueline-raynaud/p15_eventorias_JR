package fr.quinquenaire.p15_eventorias_jr.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetUserProfileUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.DeleteEventUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.EventDetailViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.contract.EventDetailEffect
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val eventId = "event123"
    val organizerUid = "uid-organizer"
    val otherUid = "uid-autre"

    val fakeEvent = Event(
        id = eventId,
        name = "Concert",
        description = "Un super concert",
        locationName = "Paris",
        category = "Musique",
        imageUrl = "https://storage/image.jpg",
        organizerId = organizerUid
    )

    val fakeProfile = UserProfile(
        uid = organizerUid, firstName = "Jacqueline", lastName = "G",
        email = "j@test.com", avatarUrl = "https://storage/avatar.jpg",
        notificationEnabled = true
    )

    fun buildViewModel(
        currentUid: String? = organizerUid,
        eventFlow: Flow<Event?> = flowOf(fakeEvent),
        profileFlow: Flow<UserProfile?> = flowOf(fakeProfile),
        deleteThrows: Exception? = null
    ): Pair<EventDetailViewModel, DeleteEventUseCase> {
        val getEventDetailUseCase = mockk<GetEventDetailUseCase>()
        val getUserProfileUseCase = mockk<GetUserProfileUseCase>()
        val getCurrentUidUseCase = mockk<GetCurrentUidUseCase>()
        val deleteEventUseCase = mockk<DeleteEventUseCase>()

        every { getEventDetailUseCase(eventId) } returns eventFlow
        every { getUserProfileUseCase(organizerUid) } returns profileFlow
        every { getCurrentUidUseCase() } returns currentUid

        if (deleteThrows == null) {
            coEvery { deleteEventUseCase(eventId, any()) } just Runs
        } else {
            coEvery { deleteEventUseCase(eventId, any()) } throws deleteThrows
        }

        val viewModel = EventDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            getEventDetailUseCase = getEventDetailUseCase,
            getUserProfileUseCase = getUserProfileUseCase,
            getCurrentUidUseCase = getCurrentUidUseCase,
            deleteEventUseCase = deleteEventUseCase
        )
        return viewModel to deleteEventUseCase
    }

    Given("l'événement existe et l'utilisateur connecté est l'organisateur") {
        val (viewModel, _) = buildViewModel(currentUid = organizerUid)

        When("on observe uiState") {
            Then("isOrganizer est vrai et les données sont mappées") {
                viewModel.uiState.test {
                    val state = expectMostRecentItem()
                    state.isLoading shouldBe false
                    state.error shouldBe null
                    state.isOrganizer shouldBe true
                    state.event?.name shouldBe "Concert"
                    state.event?.organizerAvatarUrl shouldBe "https://storage/avatar.jpg"
                }
            }
        }
    }

    Given("l'événement existe mais l'utilisateur n'est pas l'organisateur") {
        val (viewModel, _) = buildViewModel(currentUid = otherUid)

        When("on observe uiState") {
            Then("isOrganizer est faux") {
                viewModel.uiState.test {
                    expectMostRecentItem().isOrganizer shouldBe false
                }
            }
        }
    }

    Given("l'événement a été supprimé entre-temps") {
        val (viewModel, _) = buildViewModel(eventFlow = flowOf(null))

        When("on observe uiState") {
            Then("le message \"Événement introuvable\" s'affiche") {
                viewModel.uiState.test {
                    val state = expectMostRecentItem()
                    state.error shouldBe "Événement introuvable"
                    state.event shouldBe null
                }
            }
        }
    }

    Given("le chargement de l'événement échoue") {
        val (viewModel, _) = buildViewModel(
            eventFlow = flow { throw RuntimeException("Erreur réseau") }
        )

        When("on observe uiState") {
            Then("l'erreur est capturée dans l'état") {
                viewModel.uiState.test {
                    expectMostRecentItem().error shouldBe "Erreur réseau"
                }
            }
        }
    }

    Given("l'utilisateur clique sur Modifier") {
        val (viewModel, _) = buildViewModel()

        When("handleAction(OnEditClick)") {
            Then("l'effet NavigateToEdit est émis avec le bon eventId") {
                viewModel.effect.test {
                    viewModel.handleAction(EventDetailAction.OnEditClick)
                    awaitItem() shouldBe EventDetailEffect.NavigateToEdit(eventId)
                }
            }
        }
    }

    Given("l'utilisateur clique sur Retour") {
        val (viewModel, _) = buildViewModel()

        When("handleAction(OnBackClick)") {
            Then("l'effet NavigateBack est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(EventDetailAction.OnBackClick)
                    awaitItem() shouldBe EventDetailEffect.NavigateBack
                }
            }
        }
    }

    Given("l'utilisateur clique sur Supprimer") {
        val (viewModel, _) = buildViewModel()

        When("handleAction(OnDeleteClick)") {
            viewModel.handleAction(EventDetailAction.OnDeleteClick)

            Then("la boîte de dialogue de confirmation s'affiche") {
                viewModel.uiState.test {
                    expectMostRecentItem().showDeleteConfirmation shouldBe true
                }
            }
        }
    }

    Given("l'utilisateur annule la suppression") {
        val (viewModel, _) = buildViewModel()
        viewModel.handleAction(EventDetailAction.OnDeleteClick)

        When("handleAction(OnDismissDeleteDialog)") {
            viewModel.handleAction(EventDetailAction.OnDismissDeleteDialog)

            Then("la boîte de dialogue se ferme") {
                viewModel.uiState.test {
                    expectMostRecentItem().showDeleteConfirmation shouldBe false
                }
            }
        }
    }

    Given("l'organisateur confirme la suppression avec succès") {
        val (viewModel, deleteEventUseCase) = buildViewModel(currentUid = organizerUid)

        When("handleAction(OnConfirmDelete)") {
            Then("le use case est invoqué et NavigateBack est émis") {
                viewModel.uiState.test {
                    awaitItem() // "réveille" le StateFlow, event devient non-null

                    viewModel.effect.test {
                        viewModel.handleAction(EventDetailAction.OnConfirmDelete)
                        awaitItem() shouldBe EventDetailEffect.NavigateBack
                    }
                }
                coVerify(exactly = 1) { deleteEventUseCase(eventId, fakeEvent.imageUrl) }
            }
        }
    }

    Given("un non-organisateur tente de confirmer la suppression") {
        val (viewModel, deleteEventUseCase) = buildViewModel(currentUid = otherUid)

        When("handleAction(OnConfirmDelete)") {
            Then("un snackbar d'erreur s'affiche et le use case n'est jamais appelé") {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.effect.test {
                        viewModel.handleAction(EventDetailAction.OnConfirmDelete)
                        awaitItem() shouldBe EventDetailEffect.ShowSnackbar(
                            "Seul l'organisateur peut supprimer cet événement"
                        )
                    }
                }
                coVerify(exactly = 0) { deleteEventUseCase(any(), any()) }
            }
        }
    }

    Given("la suppression échoue côté repository") {
        val (viewModel, _) = buildViewModel(
            currentUid = organizerUid,
            deleteThrows = RuntimeException("boom")
        )

        When("handleAction(OnConfirmDelete)") {
            Then("un snackbar d'erreur générique s'affiche") {
                viewModel.uiState.test {
                    awaitItem()

                    viewModel.effect.test {
                        viewModel.handleAction(EventDetailAction.OnConfirmDelete)
                        awaitItem() shouldBe EventDetailEffect.ShowSnackbar("Erreur lors de la suppression")
                    }
                }
            }
        }
    }
})