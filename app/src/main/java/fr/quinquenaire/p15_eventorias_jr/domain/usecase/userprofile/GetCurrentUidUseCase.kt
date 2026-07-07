package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import javax.inject.Inject

class GetCurrentUidUseCase @Inject constructor(
    private val userRepository: UserProfileRepository
) {
    operator fun invoke(): String? = userRepository.getCurrentUserId()
}
