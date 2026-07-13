package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.SyncNotificationSubscriptionUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class SyncNotificationSubscriptionUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mockk<UserProfileRepository>()
    val useCase = SyncNotificationSubscriptionUseCase(repository)

    Given("un utilisateur avec un profil valide") {
        val uid = "user123"
        val profile = UserProfile(uid, "A", "B", "a@b.com", "", notificationEnabled = true)

        every { repository.getCurrentUserId() } returns uid
        every { repository.getUserProfile(uid) } returns flowOf(profile)
        coEvery { repository.syncNotificationSubscription(any()) } just Runs

        When("on synchronise l'abonnement") {
            useCase()

            Then("il utilise la valeur du profil pour synchroniser") {
                coVerify(exactly = 1) { repository.syncNotificationSubscription(true) }
            }
        }
    }

    Given("un utilisateur non connecté") {
        every { repository.getCurrentUserId() } returns null

        When("on synchronise") {
            useCase()

            Then("l'action est abandonnée") {
                coVerify(exactly = 0) { repository.syncNotificationSubscription(any()) }
            }
        }
    }
})