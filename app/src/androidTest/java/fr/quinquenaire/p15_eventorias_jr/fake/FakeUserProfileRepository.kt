package fr.quinquenaire.p15_eventorias_jr.fake

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserProfileRepository @Inject constructor() : UserProfileRepository {

    // On stocke les profils dans une liste en mémoire
    private val profiles = MutableStateFlow<List<UserProfile>>(emptyList())

    // On simule un utilisateur connecté (null si déconnecté)
    private var currentUserId: String? = "fake_current_uid"

    // --- Outils pour les tests ---
    fun setProfiles(list: List<UserProfile>) {
        profiles.value = list
    }

    fun setCurrentUserId(uid: String?) {
        currentUserId = uid
    }
    // -----------------------------

    override fun getUserProfile(uid: String): Flow<UserProfile?> {
        return profiles.map { list -> list.find { it.uid == uid } }
    }

    override fun getCurrentUserId(): String? {
        return currentUserId
    }

    override suspend fun createUserProfileIfMissing(profile: UserProfile) {
        val exists = profiles.value.any { it.uid == profile.uid }
        if (!exists) {
            profiles.value = profiles.value + profile
        }
    }

    override suspend fun updateNotificationSetting(uid: String, enabled: Boolean) {
        // Optionnel : on pourrait mettre à jour le profil en mémoire si tu as un champ "notificationsEnabled"
    }

    override suspend fun syncNotificationSubscription(enabled: Boolean) {
        // Rien à faire en mémoire pour l'instant
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        profiles.value = profiles.value.map {
            if (it.uid == profile.uid) profile else it
        }
    }

    override suspend fun uploadUserAvatar(uid: String, avatarUri: Uri): String {
        // On retourne une fausse URL
        return "https://fake-storage.com/avatar_$uid.jpg"
    }

    override suspend fun deleteProfileData(uid: String, avatarUrl: String) {
        profiles.value = profiles.value.filter { it.uid != uid }
    }

    override suspend fun deleteAuthAccount() {
        // Simule la déconnexion / suppression
        currentUserId = null
    }
}