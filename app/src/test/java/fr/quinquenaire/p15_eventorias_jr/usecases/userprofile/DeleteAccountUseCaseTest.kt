package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.DeleteAccountUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUidUseCase
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.GetCurrentUserProfileUseCase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class DeleteAccountUseCaseTest : BehaviorSpec({

    val getUidUseCase = mockk<GetCurrentUidUseCase>()
    val getProfileUseCase = mockk<GetCurrentUserProfileUseCase>()
    val profileRepo = mockk<UserProfileRepository>()
    val eventRepo = mockk<EventRepository>()
    val useCase = DeleteAccountUseCase(getUidUseCase, getProfileUseCase, profileRepo, eventRepo)

    Given("un utilisateur connecté") {
        val uid = "user123"
        val profile = UserProfile(uid, "A", "B", "a@b.com", "avatar_url", true)

        coEvery { getUidUseCase() } returns uid
        coEvery { getProfileUseCase() } returns flowOf(profile)
        coEvery { eventRepo.anonymizeOrganizerEvents(uid) } just Runs
        coEvery { profileRepo.deleteProfileData(uid, "avatar_url") } just Runs
        coEvery { profileRepo.deleteAuthAccount() } just Runs

        When("il supprime son compte") {
            useCase()
            Then("toutes les étapes de suppression sont exécutées dans l'ordre") {
                coVerify(exactly = 1) { eventRepo.anonymizeOrganizerEvents(uid) }
                coVerify(exactly = 1) { profileRepo.deleteProfileData(uid, "avatar_url") }
                coVerify(exactly = 1) { profileRepo.deleteAuthAccount() }
            }
        }
    }

    Given("aucun utilisateur connecté") {
        coEvery { getUidUseCase() } returns null

        When("il tente de supprimer son compte") {
            Then("une exception est levée") {
                shouldThrow<IllegalStateException> { useCase() }
            }
        }
    }
})