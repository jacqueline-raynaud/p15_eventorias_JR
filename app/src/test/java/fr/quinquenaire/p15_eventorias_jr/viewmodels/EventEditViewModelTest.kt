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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
        organizerId = organizerId
    )

    fun buildViewModel(
        eventFlow: Flow<Event?> = flowOf(fakeEvent),
        updateError: String? = null
    ): Triple<EventEditViewModel, UpdateEventUseCase, CapturingSlot<Event>> {
        val getEventDetailUseCase = mockk<GetEventDetailUseCase>()
        val updateEventUseCase = mockk<UpdateEventUseCase>()
        val eventSlot = slot<Event>()

        every { getEventDetailUseCase(eventId) } returns eventFlow

        if (updateError == null) {
            coEvery { updateEventUseCase(any(), capture(eventSlot), any()) } returns Result.success(Unit)
        } else {
            coEvery { updateEventUseCase(any(), any(), any()) } returns Result.failure(Exception(updateError))
        }

        val viewModel = EventEditViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            getEventDetailUseCase = getEventDetailUseCase,
            updateEventUseCase = updateEventUseCase
        )
        return Triple(viewModel, updateEventUseCase, eventSlot)
    }

    // --- Tests de chargement ---
    Given("un événement existant à charger") {
        val (viewModel, _, _) = buildViewModel()
        Then("le formulaire est pré-rempli") {
            viewModel.uiState.value.name shouldBe "Concert"
        }
    }

    Given("le chargement initial a échoué") {
        val getEventDetailUseCase = mockk<GetEventDetailUseCase>()
        val updateEventUseCase = mockk<UpdateEventUseCase>()
        every { getEventDetailUseCase(eventId) } returns flow { throw RuntimeException("boom") }

        val viewModel = EventEditViewModel(SavedStateHandle(mapOf("eventId" to eventId)), getEventDetailUseCase, updateEventUseCase)

        When("handleAction(OnRetry)") {
            every { getEventDetailUseCase(eventId) } returns flowOf(fakeEvent)
            viewModel.handleAction(EventEditAction.OnRetry)
            Then("le formulaire se charge enfin") {
                verify(exactly = 2) { getEventDetailUseCase(eventId) }
                viewModel.uiState.value.name shouldBe "Concert"
            }
        }
    }

    // --- Tests de sauvegarde ---
    Given("l'adresse n'a pas été modifiée") {
        val (viewModel, updateEventUseCase, eventSlot) = buildViewModel()

        When("OnSaveClick") {
            Then("la localisation existante est conservée") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.NavigateBack
                }
                coVerify { updateEventUseCase(eventId, capture(eventSlot), null) }
                eventSlot.captured.location shouldBe referenceLocation
                eventSlot.captured.locationName shouldBe "Paris"
            }
        }
    }

    Given("l'adresse a été modifiée") {
        val (viewModel, updateEventUseCase, eventSlot) = buildViewModel()
        viewModel.handleAction(EventEditAction.OnAddressChange("Lyon"))

        When("OnSaveClick") {
            Then ("la localisation est mise à null pour le géocodage")
            viewModel.effect.test {
                viewModel.handleAction(EventEditAction.OnSaveClick)
                awaitItem() shouldBe EventEditEffect.NavigateBack
            }
            coVerify { updateEventUseCase(eventId, capture(eventSlot), null) }
            eventSlot.captured.location shouldBe null
            eventSlot.captured.locationName shouldBe "Lyon"
            }
        }


    Given("la sauvegarde échoue") {
        val (viewModel, _, _) = buildViewModel(updateError = "Erreur de mise à jour")

        When("OnSaveClick") {
            Then("un snackbar d'erreur s'affiche") {
                viewModel.effect.test {
                    viewModel.handleAction(EventEditAction.OnSaveClick)
                    awaitItem() shouldBe EventEditEffect.ShowSnackbar("Erreur de mise à jour")
                }
                viewModel.uiState.value.isSaving shouldBe false
            }
        }
    }
})