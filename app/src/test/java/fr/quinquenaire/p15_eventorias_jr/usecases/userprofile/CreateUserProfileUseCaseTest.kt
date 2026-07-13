package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.CreateUserProfileUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class CreateUserProfileUseCaseTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    Given("un nouveau profil utilisateur") {
        val userRepository = mockk<UserProfileRepository>()
        val useCase = CreateUserProfileUseCase(userRepository)
        val profile = UserProfile(
            uid = "user123",
            firstName = "Jean",
            lastName = "Dupont",
            email = "jean.dupont@test.com",
            avatarUrl = "",
            notificationEnabled = true
        )

        coEvery { userRepository.createUserProfileIfMissing(profile) } returns Unit

        When("on invoque le use case CreateUserProfileUseCase") {
            runTest {
                useCase(profile)
            }

            Then("il doit appeler le repository pour créer le profil s'il manque") {
                coVerify(exactly = 1) { userRepository.createUserProfileIfMissing(profile) }
            }
        }
    }
})
