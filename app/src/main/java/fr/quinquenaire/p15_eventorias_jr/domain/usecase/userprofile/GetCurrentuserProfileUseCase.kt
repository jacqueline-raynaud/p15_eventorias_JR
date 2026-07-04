package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetCurrentUserProfileUseCase @Inject constructor(private val userRepository: UserProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> {
        val uid = userRepository.getCurrentUserId()
            ?: return flowOf(null)   // déconnecté → profil null
        return userRepository.getUserProfile(uid)
    }
}

