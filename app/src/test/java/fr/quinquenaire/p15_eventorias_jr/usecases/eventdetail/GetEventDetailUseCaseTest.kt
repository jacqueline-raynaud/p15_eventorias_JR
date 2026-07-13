package fr.quinquenaire.p15_eventorias_jr.usecases.eventdetail

import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetEventDetailUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class GetEventDetailUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un eventId existant") {
        val eventRepository = mockk<EventRepository>()
        val useCase = GetEventDetailUseCase(eventRepository)
        val eventId = "event123"
        val fakeEvent = Event(id = eventId, name = "Concert", organizerId = "uid1")

        every { eventRepository.getEventDetail(eventId) } returns flowOf(fakeEvent)

        When("on invoque le use case") {
            Then("il délègue au repository et retourne le même flux") {
                useCase(eventId).test {
                    awaitItem() shouldBe fakeEvent
                    awaitComplete()
                }
                verify(exactly = 1) { eventRepository.getEventDetail(eventId) }
            }
        }
    }

    Given("un eventId dont le document a été supprimé") {
        val eventRepository = mockk<EventRepository>()
        val useCase = GetEventDetailUseCase(eventRepository)
        val eventId = "unknown"

        every { eventRepository.getEventDetail(eventId) } returns flowOf(null)

        When("on invoque le use case") {
            Then("il retourne null") {
                useCase(eventId).test {
                    awaitItem() shouldBe null
                    awaitComplete()
                }
            }
        }
    }
})