package fr.quinquenaire.p15_eventorias_jr

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventlist.CreateEventUseCase
import io.kotest.core.config.Defaults.isolationMode
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CreateEventUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un événement valide et une image") {
        val eventRepository = mockk<EventRepository>()
        val useCase = CreateEventUseCase(eventRepository)
        val event = Event(name = "Concert", organizerId = "uid1")
        val imageUri = mockk<Uri>()

        coEvery { eventRepository.createEvent(event, imageUri) } returns "newEventId"

        When("on invoque le use case") {
            Then("il délègue au repository et retourne l'id créé") {
                val result = useCase(event, imageUri)
                result shouldBe "newEventId"
                coVerify(exactly = 1) { eventRepository.createEvent(event, imageUri) }
            }
        }
    }

    Given("un événement sans image") {
        val eventRepository = mockk<EventRepository>()
        val useCase = CreateEventUseCase(eventRepository)
        val event = Event(name = "Concert", organizerId = "uid1")

        coEvery { eventRepository.createEvent(event, null) } returns "newEventId"

        When("on invoque le use case sans imageUri") {
            Then("null est transmis au repository") {
                useCase(event)
                coVerify(exactly = 1) { eventRepository.createEvent(event, null) }
            }
        }
    }
})