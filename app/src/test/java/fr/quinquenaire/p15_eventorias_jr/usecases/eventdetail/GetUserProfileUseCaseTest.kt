package fr.quinquenaire.p15_eventorias_jr.usecases.eventdetail

import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail.GetUserProfileUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class GetUserProfileUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un uid existant") {
        val userRepository = mockk<UserProfileRepository>()
        val useCase = GetUserProfileUseCase(userRepository)
        val uid = "uid1"
        val fakeProfile = UserProfile(
            uid = uid, firstName = "Jacqueline", lastName = "G",
            email = "j@test.com", avatarUrl = "url", notificationEnabled = true
        )

        every { userRepository.getUserProfile(uid) } returns flowOf(fakeProfile)

        When("on invoque le use case") {
            Then("il retourne le profil du repository") {
                useCase(uid).test {
                    awaitItem() shouldBe fakeProfile
                    awaitComplete()
                }
            }
        }
    }
})