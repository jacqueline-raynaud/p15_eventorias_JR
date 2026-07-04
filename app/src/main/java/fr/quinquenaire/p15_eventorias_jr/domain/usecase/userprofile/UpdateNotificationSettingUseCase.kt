package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateNotificationSettingUseCase @Inject constructor(private val userRepository: UserProfileRepository) {
    suspend operator fun invoke(enabled: Boolean) {
        val uid = userRepository.getCurrentUserId() ?: return
        userRepository.updateNotificationSetting(uid, enabled)
    }
}
