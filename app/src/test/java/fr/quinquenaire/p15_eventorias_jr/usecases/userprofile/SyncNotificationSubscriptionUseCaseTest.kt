package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseMessagingManager
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
import kotlinx.coroutines.test.runTest

class SyncNotificationSubscriptionUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mockk<UserProfileRepository>()
    val messagingManager = mockk<FirebaseMessagingManager>()
    val useCase = SyncNotificationSubscriptionUseCase(repository, messagingManager)

    Given("un utilisateur connecté") {
        val uid = "user123"
        val fakeToken = "fcm_token_abc"

        every { repository.getCurrentUserId() } returns uid

        When("un token FCM est disponible") {
            coEvery { messagingManager.getToken() } returns fakeToken
            coEvery { repository.updateFcmToken(uid, fakeToken) } returns Unit

            runTest {
                useCase()
            }

            Then("le token est envoyé au repository") {
                coVerify { repository.updateFcmToken(uid, fakeToken) }
            }
        }

        When("aucun token FCM n'est trouvé") {
            coEvery { messagingManager.getToken() } returns null

            runTest {
                useCase()
            }

            Then("le repository n'est pas sollicité pour le token") {
                coVerify(exactly = 0) { repository.updateFcmToken(any(), any()) }
            }
        }
    }

    Given("un utilisateur non connecté") {
        every { repository.getCurrentUserId() } returns null

        When("on synchronise") {
            runTest {
                useCase()
            }

            Then("l'action s'arrête immédiatement") {
                coVerify(exactly = 0) { messagingManager.getToken() }
                coVerify(exactly = 0) { repository.updateFcmToken(any(), any()) }
            }
        }
    }
})