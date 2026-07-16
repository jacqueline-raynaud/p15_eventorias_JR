package fr.quinquenaire.p15_eventorias_jr.viewmodels

import android.net.Uri
import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.EventCategory
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.CreateEventUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.CreateEventViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.contract.CreateEventEffect
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
        createError: String? = null
    ): Pair<CreateEventViewModel, CreateEventUseCase> {
        val createEventUseCase = mockk<CreateEventUseCase>()
        val getCurrentUidUseCase = mockk<GetCurrentUidUseCase>()

        every { getCurrentUidUseCase() } returns currentUid

        if (createError == null) {
            coEvery {
                createEventUseCase(any(), any(), any(), any(), any(), any(), any())
            } returns Result.success("newEventId")
        } else {
            coEvery {
                createEventUseCase(any(), any(), any(), any(), any(), any(), any())
            } returns Result.failure(Exception(createError))
        }

        val viewModel = CreateEventViewModel(createEventUseCase, getCurrentUidUseCase)
        return Pair(viewModel, createEventUseCase)
    }

    fun CreateEventViewModel.fillValidForm(imageUri: Uri? = null) {
        handleAction(CreateEventAction.OnNameChange("Concert"))
        handleAction(CreateEventAction.OnDescriptionChange("Un super concert"))
        handleAction(CreateEventAction.OnCategoryChange(EventCategory.MUSIQUE))
        handleAction(CreateEventAction.OnDateSelected(1_752_000_000_000L))
        handleAction(CreateEventAction.OnTimeSelected(19, 30))
        handleAction(CreateEventAction.OnAddressChange("12 rue de la Paix, Paris"))
        imageUri?.let { handleAction(CreateEventAction.OnImageSelected(it)) }
    }

    Given("un ViewModel fraîchement créé") {
        val (viewModel, _) = buildViewModel()

        When("OnNameChange") {
            viewModel.handleAction(CreateEventAction.OnNameChange("Concert"))
            Then("le nom est mis à jour") {
                viewModel.uiState.value.name shouldBe "Concert"
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

    Given("le formulaire est valide, l'utilisateur est connecté, sans image") {
        val (viewModel, createEventUseCase) = buildViewModel(currentUid = organizerId)
        viewModel.fillValidForm()

        When("OnSaveClick") {
            Then("l'événement est créé avec les bonnes données et NavigateBack est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.NavigateBack
                }

                coVerify(exactly = 1) {
                    createEventUseCase(
                        name = "Concert",
                        description = "Un super concert",
                        date = any(),
                        locationName = "12 rue de la Paix, Paris",
                        category = "MUSIQUE",
                        organizerId = organizerId,
                        imageUri = null
                    )
                }
                viewModel.uiState.value.isSaving shouldBe true
            }
        }
    }

    Given("le formulaire est valide avec une image sélectionnée") {
        val (viewModel, createEventUseCase) = buildViewModel(currentUid = organizerId)
        val imageUri = mockk<Uri>()
        viewModel.fillValidForm(imageUri = imageUri)

        When("OnSaveClick") {
            Then("l'imageUri est transmise au use case") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.NavigateBack
                }
                coVerify(exactly = 1) {
                    createEventUseCase(any(), any(), any(), any(), any(), any(), imageUri)
                }
            }
        }
    }

    Given("la création échoue côté repository") {
        val (viewModel, _) = buildViewModel(
            currentUid = organizerId,
            createError = "boom"
        )
        viewModel.fillValidForm()

        When("OnSaveClick") {
            Then("isSaving repasse à false et un snackbar d'erreur s'affiche") {
                viewModel.effect.test {
                    viewModel.handleAction(CreateEventAction.OnSaveClick)
                    awaitItem() shouldBe CreateEventEffect.ShowSnackbar("boom")
                }
                viewModel.uiState.value.isSaving shouldBe false
            }
        }
    }
})