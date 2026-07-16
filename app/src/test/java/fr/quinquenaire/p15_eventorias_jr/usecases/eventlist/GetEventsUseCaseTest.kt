package fr.quinquenaire.p15_eventorias_jr.usecases.eventlist

import fr.quinquenaire.p15_eventorias_jr.domain.EventQueryParams
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.GetEventsUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class GetEventsUseCaseTest : BehaviorSpec({

    val repository = mockk<EventRepository>()
    val useCase = GetEventsUseCase(repository)
    val defaultParams = EventQueryParams()

    Given("a repository with events") {
        val fakeEvents = listOf(
            Event(id = "1", name = "Concert", category = "MUSIQUE"),
            Event(id = "2", name = "Marathon", category = "SPORT")
        )
        every { repository.getEventsStream(defaultParams) } returns flowOf(fakeEvents)

        When("The use case is invoked") {
            val result = useCase(defaultParams).first()

            Then("it returns the complete list from the repository") {
                result shouldHaveSize 2
                result[0].name shouldBe "Concert"
                result[1].name shouldBe "Marathon"
            }

            Then("it correctly delegates to the repository without transformation") {
                verify(exactly = 1) { repository.getEventsStream(defaultParams) }
            }
        }
    }

    Given("a repository with an empty list") {
        every { repository.getEventsStream(defaultParams) } returns flowOf(emptyList())

        When("the use case is invoked") {
            val result = useCase(defaultParams).first()

            Then("It returns an empty list") {
                result shouldHaveSize 0
            }
        }
    }
})
