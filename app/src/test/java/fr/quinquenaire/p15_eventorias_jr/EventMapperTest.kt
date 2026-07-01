package fr.quinquenaire.p15_eventorias_jr

import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import com.google.firebase.firestore.GeoPoint
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.model.toUi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

class EventMapperTest : DescribeSpec({

    describe("Event.toUi()") {

        it("mappe correctement tous les champs de base") {
            val event = Event(
                id = "abc",
                name = "Soirée Jazz",
                date = "2025-06-15",
                time = "20:00",
                category = "Musique",
                imageUrl = "https://example.com/img.jpg",
                locationName = "Lyon",
                organizerId = "user_001"
            )

            val ui = event.toUi()

            ui.id shouldBe "abc"
            ui.name shouldBe "Soirée Jazz"
            ui.date shouldBe "2025-06-15"
            ui.time shouldBe "20:00"
            ui.category shouldBe "Musique"
            ui.imageUrl shouldBe "https://example.com/img.jpg"
            ui.locationName shouldBe "Lyon"
            ui.organizerId shouldBe "user_001"
        }

        it("extrait latitude et longitude depuis un GeoPoint") {
            val event = Event(
                id = "abc",
                location = GeoPoint(45.7579, 4.8320)
            )

            val ui = event.toUi()

            ui.latitude shouldBe 45.7579
            ui.longitude shouldBe 4.8320
        }

        it("retourne null pour latitude et longitude si location est null") {
            val event = Event(id = "abc", location = null)

            val ui = event.toUi()

            ui.latitude.shouldBeNull()
            ui.longitude.shouldBeNull()
        }
    }
})