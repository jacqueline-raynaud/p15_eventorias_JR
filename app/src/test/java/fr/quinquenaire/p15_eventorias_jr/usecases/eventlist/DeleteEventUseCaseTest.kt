package fr.quinquenaire.p15_eventorias_jr.usecases.eventlist

import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.DeleteEventUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

class DeleteEventUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un eventId et une imageUrl") {
        val eventRepository = mockk<EventRepository>()
        val useCase = DeleteEventUseCase(eventRepository)
        val eventId = "event123"
        val imageUrl = "https://example.com/image.jpg"

        coEvery { eventRepository.deleteEvent(eventId, imageUrl) } just Runs

        When("on invoque le use case") {
            useCase(eventId, imageUrl)

            Then("il doit appeler la suppression dans le repository") {
                coVerify(exactly = 1) { eventRepository.deleteEvent(eventId, imageUrl) }
            }
        }
    }
})