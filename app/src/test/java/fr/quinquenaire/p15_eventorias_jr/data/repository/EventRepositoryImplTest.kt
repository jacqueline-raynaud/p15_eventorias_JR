package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
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

class EventRepositoryImplTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    beforeSpec {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    afterSpec {
        unmockkStatic(Log::class)
    }

    val firestore = mockk<FirebaseFirestore>()
    val firestoreManager = mockk<FirebaseFirestoreManager>()
    val storageManager = mockk<FirebaseStorageManager>()
    
    val repository = EventRepositoryImpl(firestore, firestoreManager, storageManager)

    Given("getEventDetail") {
        val eventId = "1"
        val event = Event(id = eventId, name = "Event 1")
        every { firestoreManager.getEventDetail(eventId) } returns flowOf(event)

        When("appel de getEventDetail") {
            Then("doit retourner le flow de firestoreManager") {
                repository.getEventDetail(eventId).collect {
                    it shouldBe event
                }
            }
        }
    }

    Given("createEvent") {
        val event = Event(name = "New Event")
        val imageUri = mockk<Uri>()
        val eventId = "generated_id"

        When("une image est fournie") {
            coEvery { firestoreManager.createEvent(event) } returns eventId
            coEvery { storageManager.uploadEventImage(eventId, imageUri) } returns "http://image.url"
            coEvery { firestoreManager.updateEventImageUrl(eventId, "http://image.url") } returns Unit

            runTest {
                val result = repository.createEvent(event, imageUri)
                result shouldBe eventId
            }

            Then("on crée l'event puis on upload l'image") {
                coVerify { firestoreManager.createEvent(event) }
                coVerify { storageManager.uploadEventImage(eventId, imageUri) }
                coVerify { firestoreManager.updateEventImageUrl(eventId, "http://image.url") }
            }
        }

        When("aucune image n'est fournie") {
            coEvery { firestoreManager.createEvent(event) } returns eventId

            runTest {
                val result = repository.createEvent(event, null)
                result shouldBe eventId
            }

            Then("seul firestoreManager.createEvent est appelé") {
                coVerify { firestoreManager.createEvent(event) }
                coVerify(exactly = 0) { storageManager.uploadEventImage(any(), any()) }
            }
        }
    }

    Given("updateEvent") {
        val event = Event(id = "1", name = "Updated Event", imageUrl = "old_url")

        When("une nouvelle image est fournie") {
            val imageUri = mockk<Uri>()
            val newImageUrl = "http://new.url"
            coEvery { storageManager.uploadEventImage("1", imageUri) } returns newImageUrl
            coEvery { firestoreManager.updateEvent(any()) } returns Unit

            runTest {
                repository.updateEvent(event, imageUri)
            }

            Then("l'image est uploadée et l'event mis à jour avec la nouvelle URL") {
                coVerify { storageManager.uploadEventImage("1", imageUri) }
                coVerify { firestoreManager.updateEvent(match { it.imageUrl == newImageUrl }) }
            }
        }

        When("aucune image n'est fournie") {
            coEvery { firestoreManager.updateEvent(any()) } returns Unit

            runTest {
                repository.updateEvent(event, null)
            }

            Then("l'event est mis à jour avec son URL existante") {
                coVerify { firestoreManager.updateEvent(match { it.imageUrl == "old_url" }) }
                coVerify(exactly = 0) { storageManager.uploadEventImage(any(), any()) }
            }
        }
    }

    Given("deleteEvent") {
        val eventId = "1"
        val imageUrl = "http://image.url"

        When("suppression avec image") {
            coEvery { firestoreManager.deleteEvent(eventId) } returns Unit
            coEvery { storageManager.deleteEventImage(eventId) } returns Unit

            runTest {
                repository.deleteEvent(eventId, imageUrl)
            }

            Then("on supprime le document et l'image") {
                coVerify { firestoreManager.deleteEvent(eventId) }
                coVerify { storageManager.deleteEventImage(eventId) }
            }
        }
    }

    Given("anonymizeOrganizerEvents") {
        val uid = "user_123"
        coEvery { firestoreManager.anonymizeOrganizerEvents(uid) } returns Unit

        When("appel de anonymizeOrganizerEvents") {
            runTest {
                repository.anonymizeOrganizerEvents(uid)
            }
            Then("doit déléguer à firestoreManager") {
                coVerify { firestoreManager.anonymizeOrganizerEvents(uid) }
            }
        }
    }
})
