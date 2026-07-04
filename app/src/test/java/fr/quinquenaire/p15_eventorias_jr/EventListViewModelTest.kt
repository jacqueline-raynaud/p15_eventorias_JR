package fr.quinquenaire.p15_eventorias_jr

import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.GetEventsUseCase
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListViewModel
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.SortOrder
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListAction
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.contract.EventListEffect
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest : BehaviorSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    val fakeEvents = listOf(
        Event(id = "1", name = "Concert", category = "Musique", date = "2025-06-15"),
        Event(id = "2", name = "Marathon", category = "Sport", date = "2025-05-10"),
        Event(id = "3", name = "Expo Art", category = "Art", date = "2025-07-01")
    )

    // -------------------------------------------------------------------------
    // Chargement initial
    // -------------------------------------------------------------------------

    Given("un use case qui retourne 3 événements") {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase() } returns flowOf(fakeEvents)

        When("le ViewModel est créé") {
            val viewModel = EventListViewModel(useCase)

            Then("isLoading passe à false") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.isLoading shouldBe false
                }
            }

            Then("la liste contient 3 événements") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events shouldHaveSize 3
                }
            }

            Then("il n'y a pas d'erreur") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.error shouldBe null
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // État d'erreur
    // -------------------------------------------------------------------------

    Given("un use case qui lève une exception") {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase() } returns kotlinx.coroutines.flow.flow {
            throw Exception("Erreur réseau")
        }

        When("le ViewModel est créé") {
            val viewModel = EventListViewModel(useCase)

            Then("isLoading passe à false") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.isLoading shouldBe false
                }
            }

            Then("l'erreur est renseignée") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.error shouldNotBe null
                    state.error shouldBe "Erreur réseau"
                }
            }

            Then("la liste est vide") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events shouldHaveSize 0
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Filtre par catégorie
    // -------------------------------------------------------------------------

    Given("un ViewModel chargé avec 3 événements de catégories différentes") {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase() } returns flowOf(fakeEvents)
        val viewModel = EventListViewModel(useCase)

        When("on filtre par catégorie Musique") {
            viewModel.handleAction(EventListAction.FilterByCategory("Musique"))

            Then("seul le Concert est visible") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events shouldHaveSize 1
                    state.events[0].name shouldBe "Concert"
                }
            }

            Then("la catégorie sélectionnée est Musique") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.selectedCategory shouldBe "Musique"
                }
            }
        }

        When("on réinitialise le filtre avec null") {
            viewModel.handleAction(EventListAction.FilterByCategory(null))

            Then("tous les événements sont de nouveau visibles") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events shouldHaveSize 3
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tri
    // -------------------------------------------------------------------------

    Given("un ViewModel chargé avec 3 événements") {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase() } returns flowOf(fakeEvents)
        val viewModel = EventListViewModel(useCase)

        When("on trie par date") {
            viewModel.handleAction(EventListAction.ChangeSortOrder(SortOrder.BY_DATE_ASC))

            Then("le premier événement est le plus ancien") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events[0].name shouldBe "Marathon"
                }
            }
        }

/*        When("on trie par catégorie") {
            viewModel.handleAction(EventListAction.ChangeSortOrder(SortOrder.BY_CATEGORY))

            Then("les événements sont triés alphabétiquement par catégorie") {
                viewModel.uiState.test {
                    val state = awaitItem()
                    state.events[0].name shouldBe "Expo Art"
                    state.events[1].name shouldBe "Concert"
                    state.events[2].name shouldBe "Marathon"
                }
            }
        }*/
    }

    // -------------------------------------------------------------------------
    // Navigation (Effect)
    // -------------------------------------------------------------------------

    Given("un ViewModel chargé") {
        val useCase = mockk<GetEventsUseCase>()
        every { useCase() } returns flowOf(fakeEvents)
        val viewModel = EventListViewModel(useCase)

        When("on clique sur un événement") {
            Then("un effet NavigateToEventDetail est émis avec le bon ID") {
                viewModel.effect.test {
                    // Turbine écoute ICI avant que l'action soit déclenchée
                    viewModel.handleAction(EventListAction.OnEventClick("1"))

                    val effect = awaitItem() as EventListEffect.NavigateToEventDetail
                    effect.eventId shouldBe "1"
                }
            }
        }
    }
})