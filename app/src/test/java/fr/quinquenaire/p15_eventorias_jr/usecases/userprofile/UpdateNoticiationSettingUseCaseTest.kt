package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateNotificationSettingUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

class UpdateNotificationSettingUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mockk<UserProfileRepository>()
    val useCase = UpdateNotificationSettingUseCase(repository)

    Given("un utilisateur connecté") {
        val uid = "user123"
        every { repository.getCurrentUserId() } returns uid
        coEvery { repository.updateNotificationSetting(uid, any()) } just Runs

        When("on active les notifications") {
            useCase(true)

            Then("le repository doit enregistrer le changement") {
                coVerify(exactly = 1) { repository.updateNotificationSetting(uid, true) }
            }
        }
    }

    Given("aucun utilisateur connecté") {
        every { repository.getCurrentUserId() } returns null

        When("on essaie de changer le réglage") {
            useCase(true)

            Then("rien ne doit se passer (pas d'appel au repository)") {
                coVerify(exactly = 0) { repository.updateNotificationSetting(any(), any()) }
            }
        }
    }
})