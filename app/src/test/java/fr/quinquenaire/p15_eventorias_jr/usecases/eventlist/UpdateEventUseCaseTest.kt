package fr.quinquenaire.p15_eventorias_jr.usecases.eventlist

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.UpdateEventUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

class UpdateEventUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un événement modifié et une nouvelle image") {
        val eventRepository = mockk<EventRepository>()
        val useCase = UpdateEventUseCase(eventRepository)
        val event = Event(id = "event1", name = "Concert modifié", organizerId = "uid1")
        val imageUri = mockk<Uri>()

        coEvery { eventRepository.updateEvent(event, imageUri) } just Runs

        When("on invoque le use case") {
            Then("il délègue au repository") {
                useCase(event, imageUri)
                coVerify(exactly = 1) { eventRepository.updateEvent(event, imageUri) }
            }
        }
    }

    Given("un événement modifié sans nouvelle image") {
        val eventRepository = mockk<EventRepository>()
        val useCase = UpdateEventUseCase(eventRepository)
        val event = Event(id = "event1", name = "Concert modifié", organizerId = "uid1")

        coEvery { eventRepository.updateEvent(event, null) } just Runs

        When("on invoque le use case sans imageUri") {
            Then("null est transmis au repository") {
                useCase(event)
                coVerify(exactly = 1) { eventRepository.updateEvent(event, null) }
            }
        }
    }
})