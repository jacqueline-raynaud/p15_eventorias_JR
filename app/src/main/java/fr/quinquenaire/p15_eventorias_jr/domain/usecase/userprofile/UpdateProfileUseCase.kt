package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserProfileRepository
) {

    suspend operator fun invoke(profile: UserProfile, newAvatarUri: Uri? = null) {
        val profileToSave = if (newAvatarUri != null) {
            val avatarUrl = userRepository.uploadUserAvatar(profile.uid, newAvatarUri)
            profile.copy(avatarUrl = avatarUrl)
        } else {
            profile
        }
        userRepository.updateUserProfile(profileToSave)
    }
}