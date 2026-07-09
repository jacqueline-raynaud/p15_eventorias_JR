package fr.quinquenaire.p15_eventorias_jr.domain.repository

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    /** Profil d'un utilisateur quelconque (utilisé par EventDetail) */
    fun getUserProfile(uid: String): Flow<UserProfile?>

    /** UID de l'utilisateur actuellement connecté, null si déconnecté */
    fun getCurrentUserId(): String?

    /** Crée le document users s'il n'existe pas déjà (1re connexion) */
    suspend fun createUserProfileIfMissing(profile: UserProfile)

    /** Met à jour l'activation des notifications */
    suspend fun updateNotificationSetting(uid: String, enabled: Boolean)

    /** met à jour le profil utilisateur */
    suspend fun updateUserProfile(profile: UserProfile)

    // upload avatar
    suspend fun uploadUserAvatar(uid: String, avatarUri: Uri): String

    suspend fun deleteProfileData(uid: String, avatarUrl: String)
    suspend fun deleteAuthAccount()
}