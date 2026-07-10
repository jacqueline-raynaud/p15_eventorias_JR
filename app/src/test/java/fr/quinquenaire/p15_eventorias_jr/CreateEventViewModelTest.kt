package fr.quinquenaire.p15_eventorias_jr

import android.net.Uri
import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.CreateEventUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.CreateEventViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventEffect
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.InternalPlatform.time
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEventViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val organizerId = "uid-organizer"

    fun buildViewModel(
        currentUid: String? = organizerId,
        createThrows: Exception? = null
    ): Triple<CreateEventViewModel, CreateEventUseCase, CapturingSlot<Event>> {
        val createEventUseCase = mockk<CreateEventUseCase>()
        val getCurrentUidUseCase = mockk<GetCurrentUidUseCase>()
        val eventSlot = slot<Event>()

        every { getCurrentUidUseCase() } returns currentUid

        if (createThrows == null) {
            coEvery { createEventUseCase(capture(eventSlot), any()) } returns "newEventId"
        } else {
            coEvery { createEventUseCase(capture(eventSlot), any()) } throws createThrows
        }

        val viewModel = CreateEventViewModel(createEventUseCase, getCurrentUidUseCase)
        return Triple(viewModel, createEventUseCase, eventSlot)
    }

    // Rempli un formulaire valide sur le ViewModel passé en paramètre
    fun CreateEventViewModel.fillValidForm(imageUri: Uri? = null) {
        handleAction(CreateEventAction.OnNameChange("Concert"))
        handleAction(CreateEventAction.OnDescriptionChange("Un super concert"))
        handleAction(CreateEventAction.OnCategoryChange(EventCategory.MUSIQUE))
        handleAction(CreateEventAction.OnDateSelected(1_752_000_000_000L))
        handleAction(CreateEventAction.OnTimeSelected(19, 30))
        handleAction(CreateEventAction.OnAddressChange("12 rue de la Paix, Paris"))
        imageUri?.let { handleAction(CreateEventAction.OnImageSelected(it)) }
    }

    // ---------------------------------------------------------------
    // Mise à jour des champs du formulaire
    // ---------------------------------------------------------------

    Given("un ViewModel fraîchement créé") {
        val (viewModel, _, _) = buildViewModel()

        When("OnNameChange") {
            viewModel.handleAction(CreateEventAction.OnNameChange("Concert"))
            Then("le nom est mis à jour") {
                viewModel.uiState.value.name shouldBe "Concert"
            }
        }

        When("OnDescriptionChange") {
            viewModel.handleAction(CreateEventAction.OnDescriptionChange("Description"))
            Then("la description est mise à jour") {
                viewModel.uiState.value.description shouldBe "Description"
            }
        }

        When("OnCategoryChange") {
            viewModel.handleAction(CreateEventAction.OnCategoryChange(EventCategory.SPORT))
            Then("la catégorie est mise à jour") {
                viewModel.uiState.value.category shouldBe EventCategory.SPORT
            }
        }

        When("OnDateSelected") {
            viewModel.handleAction(CreateEventAction.OnDateSelected(1_752_000_000_000L))
            Then("dateMillis est mis à jour") {
                viewModel.uiState.value.dateMillis shouldBe 1_752_000_000_000L
            }
        }

        When("OnTimeSelected") {
            viewModel.handleAction(CreateEventAction.OnTimeSelected(19, 30))
            Then("hour et minute sont mis à jour") {
                viewModel.uiState.value.hour shouldBe 19
                viewModel.uiState.value.minute shouldBe 30
            }
        }

        When("OnAddressChange") {
            viewModel.handleAction(CreateEventAction.OnAddressChange("Paris"))
            Then("l'adresse est mise à jour") {
                viewModel.uiState.value.address shouldBe "Paris"
            }
        }

        When("OnImageSelected") {
            val uri = mockk<Uri>()
            viewModel.handleAction(CreateEventAction.OnImageSelected(uri))
            Then("imageUri est mis à jour") {
                viewModel.uiState.value.imageUri shouldBe uri
            }
        }

        When("OnBackClick") {
            Then("l'effet NavigateBack est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnBackClick)
                    awaitItem() shouldBe CreateEventEffect.NavigateBack
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Validation et sauvegarde
    // ---------------------------------------------------------------

    Given("le formulaire est incomplet") {
        val (viewModel, createEventUseCase, _) = buildViewModel()
        viewModel.handleAction(CreateEventAction.OnNameChange("Concert"))
        // catégorie, date, heure, adresse manquants

        When("OnSaveClick") {
            viewModel.handleAction(CreateEventAction.OnSaveClick)

            Then("aucune sauvegarde n'est tentée") {
                coVerify(exactly = 0) { createEventUseCase(any(), any()) }
                viewModel.uiState.value.isSaving shouldBe false
            }
        }
    }

    Given("le formulaire est valide mais aucun utilisateur n'est connecté") {
        val (viewModel, createEventUseCase, _) = buildViewModel(currentUid = null)
        viewModel.fillValidForm()

        When("OnSaveClick") {
            Then("un snackbar d'erreur s'affiche et aucune sauvegarde n'est tentée") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.ShowSnackbar("Utilisateur non connecté")
                }
                coVerify(exactly = 0) { createEventUseCase(any(), any()) }
            }
        }
    }

    Given("le formulaire est valide, l'utilisateur est connecté, sans image") {
        val (viewModel, createEventUseCase, eventSlot) = buildViewModel(currentUid = organizerId)
        viewModel.fillValidForm()

        When("OnSaveClick") {
            Then("l'événement est créé avec les bonnes données et NavigateBack est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.NavigateBack
                }

                coVerify(exactly = 1) { createEventUseCase(any(), null) }

                val savedEvent = eventSlot.captured
                savedEvent.name shouldBe "Concert"
                savedEvent.description shouldBe "Un super concert"
                savedEvent.category shouldBe "MUSIQUE"
                savedEvent.locationName shouldBe "12 rue de la Paix, Paris"
                savedEvent.organizerId shouldBe organizerId

                val calendar = Calendar.getInstance().apply { time = savedEvent.date!!.toDate() }
                calendar.get(Calendar.HOUR_OF_DAY) shouldBe 19
                calendar.get(Calendar.MINUTE) shouldBe 30

                viewModel.uiState.value.isSaving shouldBe true
            }
        }
    }

    Given("le formulaire est valide avec une image sélectionnée") {
        val (viewModel, createEventUseCase, _) = buildViewModel(currentUid = organizerId)
        val imageUri = mockk<Uri>()
        viewModel.fillValidForm(imageUri = imageUri)

        When("OnSaveClick") {
            Then("l'imageUri est transmise au use case") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.NavigateBack
                }
                coVerify(exactly = 1) { createEventUseCase(any(), imageUri) }
            }
        }
    }

    Given("la création échoue côté repository") {
        val (viewModel, _, _) = buildViewModel(
            currentUid = organizerId,
            createThrows = RuntimeException("boom")
        )
        viewModel.fillValidForm()

        When("OnSaveClick") {
            Then("isSaving repasse à false et un snackbar d'erreur s'affiche") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.ShowSnackbar(
                        "Erreur lors de la création de l'événement"
                    )
                }
                viewModel.uiState.value.isSaving shouldBe false
            }
        }
    }
})