package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import app.cash.turbine.test
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUserProfileUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class GetCurrentUserProfileUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val userRepository = mockk<UserProfileRepository>()
    val useCase = GetCurrentUserProfileUseCase(userRepository)

    Given("un utilisateur non connecté") {
        every { userRepository.getCurrentUserId() } returns null

        When("on invoque le use case") {
            Then("il retourne un flow contenant null") {
                useCase().test {
                    awaitItem() shouldBe null
                    awaitComplete()
                }
            }
        }
    }

    Given("un utilisateur connecté") {
        val uid = "user123"
        val profile = UserProfile(
            uid = uid,
            firstName = "Jean",
            lastName = "Dupont",
            email = "jean.dupont@test.com",
            avatarUrl = "",
            notificationEnabled = true
        )
        every { userRepository.getCurrentUserId() } returns uid
        every { userRepository.getUserProfile(uid) } returns flowOf(profile)

        When("on invoque le use case") {
            Then("il retourne le profil correspondant à l'UID actuel") {
                useCase().test {
                    awaitItem() shouldBe profile
                    awaitComplete()
                }
            }
        }
    }
})
