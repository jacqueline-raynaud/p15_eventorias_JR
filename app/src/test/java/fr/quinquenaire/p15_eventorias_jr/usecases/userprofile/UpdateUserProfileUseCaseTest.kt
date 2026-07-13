package fr.quinquenaire.p15_eventorias_jr.usecases.userprofile

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.UpdateUserProfileUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

class UpdateUserProfileUseCaseTest : BehaviorSpec({

    val repository = mockk<UserProfileRepository>()
    val useCase = UpdateUserProfileUseCase(repository)
    val profile = UserProfile("uid1", "Jean", "Dupont", "j@d.com", "old_url", true)

    Given("une mise à jour sans nouvel avatar") {
        coEvery { repository.updateUserProfile(any()) } just Runs

        When("on invoque le use case") {
            useCase(profile, null)
            Then("il met à jour le profil directement") {
                coVerify(exactly = 1) { repository.updateUserProfile(profile) }
            }
        }
    }

    Given("une mise à jour avec un nouvel avatar") {
        val newUri = mockk<Uri>()
        val newUrl = "http://new-avatar.com"
        coEvery { repository.uploadUserAvatar("uid1", newUri) } returns newUrl
        coEvery { repository.updateUserProfile(any()) } just Runs

        When("on invoque le use case") {
            useCase(profile, newUri)
            Then("il upload l'image puis met à jour le profil avec la nouvelle URL") {
                coVerify { repository.uploadUserAvatar("uid1", newUri) }
                coVerify { repository.updateUserProfile(profile.copy(avatarUrl = newUrl)) }
            }
        }
    }
})