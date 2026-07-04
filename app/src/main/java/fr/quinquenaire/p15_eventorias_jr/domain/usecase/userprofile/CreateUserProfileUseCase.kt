package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(
    private val userRepository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        userRepository.createUserProfileIfMissing(profile)
    }
}