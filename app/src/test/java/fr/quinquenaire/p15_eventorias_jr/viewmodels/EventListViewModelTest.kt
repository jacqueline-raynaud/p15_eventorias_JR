package fr.quinquenaire.p15_eventorias_jr.viewmodels

import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.SortOrder
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.GetEventsUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListEffect
import fr.quinquenaire.p15_eventorias_jr.testutils.TestTimestamps
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()
    beforeSpec { Dispatchers.setMain(testDispatcher) }
    afterSpec { Dispatchers.resetMain() }

    val fakeEvents = listOf(
        Event(id = "1", name = "Concert", category = "MUSIQUE"),
        Event(id = "2", name = "Marathon", category = "SPORT")
    )

    Given("un ViewModel chargé") {
        val useCase = mockk<GetEventsUseCase>()

        // Configure le mock par défaut (tous les événements)
        every { useCase(EventQueryParams()) } returns flowOf(fakeEvents)
        // Pour tout autre cas (par défaut), retourne aussi tous les événements
        every { useCase(match { it.category == null }) } returns flowOf(fakeEvents)
        // Pour la catégorie MUSIQUE, retourne seulement le premier
        every { useCase(match { it.category == "MUSIQUE" }) } returns flowOf(listOf(fakeEvents[0]))
        // Pour la catégorie SPORT, retourne seulement le deuxième
        every { useCase(match { it.category == "SPORT" }) } returns flowOf(listOf(fakeEvents[1]))

        val viewModel = EventListViewModel(useCase)

        When("le ViewModel s'initialise") {
            viewModel.uiState.test {
                // 1. Le premier item émis par combine (avec les données du useCase)
                val loadedState = awaitItem()
                loadedState.events shouldHaveSize 2
                loadedState.isLoading shouldBe false

                cancelAndConsumeRemainingEvents()
            }
        }

        /*When("on filtre par catégorie 'MUSIQUE'") {
            viewModel.uiState.test {
                awaitItem() // Initial state avec tous les événements
                
                viewModel.handleAction(EventListAction.FilterByCategory("MUSIQUE"))
                
                Then("le UseCase est appelé avec la catégorie et la liste est mise à jour") {
                    val filteredState = awaitItem()
                    filteredState.events shouldHaveSize 1
                    filteredState.events[0].name shouldBe "Concert"
                    filteredState.selectedCategory shouldBe "MUSIQUE"
                }
                
                cancelAndConsumeRemainingEvents()
            }
        }*/

        When("on change l'ordre de tri") {
            viewModel.uiState.test {
                awaitItem() // Initial state

                viewModel.handleAction(EventListAction.ChangeSortOrder(SortOrder.BY_DATE_DESC))

                Then("le UseCase est appelé avec le nouvel ordre") {
                    val sortedState = awaitItem()
                    sortedState.sortOrder shouldBe SortOrder.BY_DATE_DESC
                    verify { useCase(match { it.sortOrder == SortOrder.BY_DATE_DESC }) }
                }

                cancelAndConsumeRemainingEvents()
            }
        }

        When("on saisit une recherche") {
            viewModel.uiState.test {
                awaitItem() // Initial state

                viewModel.handleAction(EventListAction.OnSearchQueryChanged("Marathon"))

                Then("le UseCase est appelé avec la query") {
                    val searchState = awaitItem()
                    searchState.searchQuery shouldBe "Marathon"
                    verify { useCase(match { it.searchQuery == "Marathon" }) }
                }

                cancelAndConsumeRemainingEvents()
            }
        }
    }


    Given("le UseCase lève une erreur") {
        val useCase = mockk<GetEventsUseCase>(relaxed = true)
        every { useCase(any()) } returns flow { throw Exception("Erreur réseau") }

        val viewModel = EventListViewModel(useCase)

        Then("l'erreur est affichée dans le uiState") {
            viewModel.uiState.test {
                val errorState = awaitItem()
                errorState.error shouldBe "Erreur réseau"
                errorState.isLoading shouldBe false
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    Given("Navigation") {
        val useCase = mockk<GetEventsUseCase>(relaxed = true)
        every { useCase(any()) } returns flowOf(emptyList())
        val viewModel = EventListViewModel(useCase)

        When("on clique sur un événement") {
            Then("l'effet de navigation est émis") {
                viewModel.effect.test {
                    viewModel.handleAction(EventListAction.OnEventClick("123"))
                    val effect = awaitItem() as EventListEffect.NavigateToEventDetail
                    effect.eventId shouldBe "123"
                    cancelAndConsumeRemainingEvents()
                }
            }
        }
    }
})
