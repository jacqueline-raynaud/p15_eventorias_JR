package fr.quinquenaire.p15_eventorias_jr

import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.meta.When

class GetCurrentUidUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un utilisateur connecté") {
        val userRepository = mockk<UserProfileRepository>()
        val useCase = GetCurrentUidUseCase(userRepository)

        every { userRepository.getCurrentUserId() } returns "uid1"

        When("on invoque le use case") {
            Then("il retourne l'uid") {
                useCase() shouldBe "uid1"
            }
        }
    }

    Given("aucun utilisateur connecté") {
        val userRepository = mockk<UserProfileRepository>()
        val useCase = GetCurrentUidUseCase(userRepository)

        every { userRepository.getCurrentUserId() } returns null

        When("on invoque le use case") {
            Then("il retourne null") {
                useCase() shouldBe null
            }
        }
    }
})