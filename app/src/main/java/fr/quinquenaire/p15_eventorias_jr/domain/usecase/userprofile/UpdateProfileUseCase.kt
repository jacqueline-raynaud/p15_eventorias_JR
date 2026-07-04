package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserProfileRepository
) {
    /**
     * Met à jour le profil. Si newAvatarUri est fourni, l'image est d'abord
     * uploadée dans Storage et son URL remplace l'ancienne.
     */
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