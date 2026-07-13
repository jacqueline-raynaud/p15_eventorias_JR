package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import fr.quinquenaire.p15_eventorias_jr.data.location.GeocoderManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.io.IOException

class EventRepositoryImplTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    beforeSpec {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    afterSpec {
        unmockkStatic(Log::class)
    }

    val firestoreManager = mockk<FirebaseFirestoreManager>()
    val storageManager = mockk<FirebaseStorageManager>()
    val geocoderManager = mockk<GeocoderManager>()
    val repository = EventRepositoryImpl(firestoreManager, storageManager, geocoderManager)

    Given("getEvents") {
        val events = listOf(Event(id = "1", name = "Event 1"))
        every { firestoreManager.getEvents() } returns flowOf(events)

        When("appel de getEvents") {
            Then("doit retourner le flow de firestoreManager") {
                repository.getEvents().collect {
                    it shouldBe events
                }
            }
        }
    }

    Given("createEvent") {
        val event = Event(name = "New Event", locationName = "Paris")
        val eventWithLocation = event.copy(location = GeoPoint(48.8566, 2.3522))
        val imageUri = mockk<Uri>()
        val eventId = "generated_id"

        When("le géocodage réussit et une image est fournie") {
            coEvery { geocoderManager.geocode("Paris") } returns GeoPoint(48.8566, 2.3522)
            coEvery { firestoreManager.createEvent(eventWithLocation) } returns eventId
            coEvery { storageManager.uploadEventImage(eventId, imageUri) } returns "http://image.url"
            coEvery { firestoreManager.updateEventImageUrl(eventId, "http://image.url") } returns Unit

            runTest {
                val result = repository.createEvent(event, imageUri)
                result shouldBe eventId
            }

            Then("tous les managers doivent être sollicités correctement") {
                coVerify { geocoderManager.geocode("Paris") }
                coVerify { firestoreManager.createEvent(eventWithLocation) }
                coVerify { storageManager.uploadEventImage(eventId, imageUri) }
                coVerify { firestoreManager.updateEventImageUrl(eventId, "http://image.url") }
            }
        }

        When("le géocodage échoue (IOException)") {
            coEvery { geocoderManager.geocode("Paris") } throws IOException("Network error")
            coEvery { firestoreManager.createEvent(event.copy(location = null)) } returns eventId

            runTest {
                val result = repository.createEvent(event, null)
                result shouldBe eventId
            }

            Then("l'événement est créé sans localisation") {
                coVerify { firestoreManager.createEvent(event.copy(location = null)) }
            }
        }
    }

    Given("updateEvent") {
        val event = Event(id = "1", name = "Updated Event", locationName = "Lyon")
        
        When("aucune nouvelle image n'est fournie et la location est déjà présente") {
            val eventWithLocation = event.copy(location = GeoPoint(45.7640, 4.8357))
            coEvery { firestoreManager.updateEvent(eventWithLocation) } returns Unit

            runTest {
                repository.updateEvent(eventWithLocation, null)
            }

            Then("seul firestoreManager.updateEvent est appelé") {
                coVerify { firestoreManager.updateEvent(eventWithLocation) }
                coVerify(exactly = 0) { geocoderManager.geocode(any()) }
                coVerify(exactly = 0) { storageManager.uploadEventImage(any(), any()) }
            }
        }

        When("une nouvelle image est fournie et la location doit être géocodée") {
            val imageUri = mockk<Uri>()
            val location = GeoPoint(45.7640, 4.8357)
            val updatedEvent = event.copy(location = location, imageUrl = "http://new.url")
            
            coEvery { geocoderManager.geocode("Lyon") } returns location
            coEvery { storageManager.uploadEventImage("1", imageUri) } returns "http://new.url"
            coEvery { firestoreManager.updateEvent(updatedEvent) } returns Unit

            runTest {
                repository.updateEvent(event, imageUri)
            }

            Then("le géocodage et l'upload sont effectués") {
                coVerify { geocoderManager.geocode("Lyon") }
                coVerify { storageManager.uploadEventImage("1", imageUri) }
                coVerify { firestoreManager.updateEvent(updatedEvent) }
            }
        }
    }

    Given("deleteEvent") {
        val eventId = "1"
        val imageUrl = "http://image.url"

        When("suppression de l'événement") {
            coEvery { firestoreManager.deleteEvent(eventId) } returns Unit
            coEvery { storageManager.deleteEventImage(eventId) } returns Unit

            runTest {
                repository.deleteEvent(eventId, imageUrl)
            }

            Then("firestore et storage sont sollicités") {
                coVerify { firestoreManager.deleteEvent(eventId) }
                coVerify { storageManager.deleteEventImage(eventId) }
            }
        }

        When("suppression d'un événement sans image") {
            coEvery { firestoreManager.deleteEvent(eventId) } returns Unit

            runTest {
                repository.deleteEvent(eventId, "")
            }

            Then("storageManager n'est pas appelé") {
                coVerify { firestoreManager.deleteEvent(eventId) }
                coVerify(exactly = 0) { storageManager.deleteEventImage(any()) }
            }
        }
    }
})
