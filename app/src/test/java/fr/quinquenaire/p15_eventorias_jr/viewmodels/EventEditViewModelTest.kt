package fr.quinquenaire.p15_eventorias_jr.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.UpdateEventUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.EventEditViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract.EventEditAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.contract.EventEditEffect
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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EventEditViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val eventId = "event123"
    val organizerId = "uid-organizer"

    // Date de référence : 12 juillet 2026, 19h30
    val referenceCalendar = Calendar.getInstance().apply {
        set(2026, Calendar.JULY, 12, 19, 30, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val referenceTimestamp = Timestamp(referenceCalendar.time)
    val referenceLocation = GeoPoint(48.8584, 2.2945)

    val fakeEvent = Event(
        id = eventId,
        name = "Concert",
        description = "Un super concert",
        date = referenceTimestamp,
        locationName = "Paris",
        location = referenceLocation,
        category = "MUSIQUE",
        imageUrl = "https://storage/image.jpg",
        organizerId = organizerId,
        guests = listOf("uidGuest1", "uidGuest2")
    )

    fun buildViewModel(
        eventFlow: Flow<Event?> = flowOf(fakeEvent),
        updateThrows: Exception? = null
    ): Triple<EventEditViewModel, UpdateEventUseCase, CapturingSlot<Event>> {
        val getEventDetailUseCase = mockk<GetEventDetailUseCase>()
        val updateEventUseCase = mockk<UpdateEventUseCase>()
        val eventSlot = slot<Event>()

        every { getEventDetailUseCase(eventId) } returns eventFlow

        if (updateThrows == null) {
            coEvery { updateEventUseCase(capture(eventSlot), any()) } just Runs
        } else {
            coEvery { updateEventUseCase(capture(eventSlot), any()) } throws updateThrows
        }

        val viewModel = EventEditViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            getEventDetailUseCase = getEventDetailUseCase,
            updateEventUseCase = updateEventUseCase
        )
        return Triple(viewModel, updateEventUseCase, eventSlot)
    }

    // -----------------------------------------------------------
    // Chargement initial
    // -----------------------------------------------------------

    Given("un événement existant à charger") {
        val (viewModel, _, _) = buildViewModel()

        Then("le formulaire est pré-rempli avec les données de l'événement") {
            val state = viewModel.uiState.value
            state.isLoading shouldBe false
            state.error shouldBe null
            state.name shouldBe "Concert"
            state.description shouldBe "Un super concert"
            state.category shouldBe EventCategory.MUSIQUE
            state.dateMillis shouldBe referenceTimestamp.toDate().time
            state.hour shouldBe 19
            state.minute shouldBe 30
            state.address shouldBe "Paris"
            state.existingImageUrl shouldBe "https://storage/image.jpg"
            state.organizerId shouldBe organizerId
            state.guests shouldBe listOf("uidGuest1", "uidGuest2")
            state.initialAddress shouldBe "Paris"
            state.initialLocation shouldBe referenceLocation
        }
    }

    Given("l'événement demandé n'existe plus") {
        val (viewModel, _, _) = buildViewModel(eventFlow = flowOf(null))

        Then("l'état affiche \"Événement introuvable\"") {
            val state = viewModel.uiState.value
            state.isLoading shouldBe false
            state.error shouldBe "Événement introuvable"
        }
    }

    Given("le chargement échoue") {
        val (viewModel, _, _) = buildViewModel(
            eventFlow = flow { throw RuntimeException("Erreur réseau") }
        )

        Then("l'état affiche \"Erreur de chargement\"") {
            val state = viewModel.uiState.value
            state.isLoading shouldBe false
            state.error shouldBe "Erreur de chargement"
        }
    }

    Given("le chargement initial a échoué") {
        val getEventDetailUseCase = mockk<GetEventDetailUseCase>()
        val updateEventUseCase = mockk<UpdateEventUseCase>()
        every { getEventDetailUseCase(eventId) } returns flow { throw RuntimeException("boom") }

        val viewModel = EventEditViewModel(
            SavedStateHandle(mapOf("eventId" to eventId)),
            getEventDetailUseCase,
            updateEventUseCase
        )

        When("handleAction(OnRetry)") {
            every { getEventDetailUseCase(eventId) } returns flowOf(fakeEvent)
            viewModel.handleAction(EventEditAction.OnRetry)

            Then("le use case est réinvoqué et le formulaire se charge") {
                verify(exactly = 2) { getEventDetailUseCase(eventId) }
                viewModel.uiState.value.error shouldBe null
                viewModel.uiState.value.name shouldBe "Concert"
            }
        }
    }

    // -----------------------------------------------------------
    // Mise à jour des champs
    // -----------------------------------------------------------

    Given("un formulaire chargé") {
        val (viewModel, _, _) = buildViewModel()

        When("OnNameChange") {
            viewModel.handleAction(EventEditAction.OnNameChange("Nouveau nom"))
            Then("le nom est mis à jour") {
                viewModel.uiState.value.name shouldBe "Nouveau nom"
            }
        }

        When("OnCategoryChange") {
            viewModel.handleAction(EventEditAction.OnCategoryChange(EventCategory.SPORT))
            Then("la catégorie est mise à jour") {
                viewModel.uiState.value.category shouldBe EventCategory.SPORT
            }
        }

        When("OnImageSelected") {
            val uri = mockk<Uri>()
            viewModel.handleAction(EventEditAction.OnImageSelected(uri))
            Then("imageUri est mis à jour et devient l'aperçu prioritaire") {
                viewModel.uiState.value.imageUri shouldBe uri
                viewModel.uiState.value.imagePreview shouldBe uri
            }
        }

        When("OnBackClick") {
            Then("l'effet NavigateBack est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnBackClick)
                    awaitItem() shouldBe EventEditEffect.NavigateBack
                }
            }
        }
    }

    // -----------------------------------------------------------
    // Sauvegarde
    // -----------------------------------------------------------

    Given("l'adresse n'a pas été modifiée") {
        val (viewModel, updateEventUseCase, eventSlot) = buildViewModel()

        When("OnSaveClick") {
            Then("la localisation existante est conservée (pas de regéocodage déclenché)") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.NavigateBack
                }
                coVerify(exactly = 1) { updateEventUseCase(any(), null) }

                val saved = eventSlot.captured
                saved.location shouldBe referenceLocation
                saved.organizerId shouldBe organizerId
                saved.guests shouldBe listOf("uidGuest1", "uidGuest2")
                saved.imageUrl shouldBe "https://storage/image.jpg"
            }
        }
    }

    Given("l'adresse a été modifiée") {
        val (viewModel, updateEventUseCase, eventSlot) = buildViewModel()
        viewModel.handleAction(EventEditAction.OnAddressChange("Nouvelle adresse, Lyon"))

        When("OnSaveClick") {
            Then("la localisation est mise à null pour déclencher un regéocodage") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.NavigateBack
                }
                coVerify(exactly = 1) { updateEventUseCase(any(), null) }

                eventSlot.captured.location shouldBe null
                eventSlot.captured.locationName shouldBe "Nouvelle adresse, Lyon"
            }
        }
    }

    Given("une nouvelle image est sélectionnée") {
        val (viewModel, updateEventUseCase, _) = buildViewModel()
        val newImageUri = mockk<Uri>()
        viewModel.handleAction(EventEditAction.OnImageSelected(newImageUri))

        When("OnSaveClick") {
            Then("la nouvelle imageUri est transmise au use case") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.NavigateBack
                }
                coVerify(exactly = 1) { updateEventUseCase(any(), newImageUri) }
            }
        }
    }

    Given("le formulaire devient invalide (nom vidé)") {
        val (viewModel, updateEventUseCase, _) = buildViewModel()
        viewModel.handleAction(EventEditAction.OnNameChange(""))

        When("OnSaveClick") {
            viewModel.handleAction(EventEditAction.OnSaveClick)

            Then("aucune sauvegarde n'est tentée") {
                coVerify(exactly = 0) { updateEventUseCase(any(), any()) }
            }
        }
    }

    Given("la sauvegarde échoue côté repository") {
        val (viewModel, _, _) = buildViewModel(updateThrows = RuntimeException("boom"))

        When("OnSaveClick") {
            Then("isSaving repasse à false et un snackbar d'erreur s'affiche") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.ShowSnackbar(
                        "Erreur lors de la modification de l'événement"
                    )
                }
                viewModel.uiState.value.isSaving shouldBe false
            }
        }
    }
})