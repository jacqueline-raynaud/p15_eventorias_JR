package fr.quinquenaire.p15_eventorias_jr.domain.usecase.eventdetail

import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(private val userRepository: UserProfileRepository) {
    operator fun invoke(uid: String): Flow<UserProfile?> {
        return userRepository.getUserProfile(uid)
    }
}
