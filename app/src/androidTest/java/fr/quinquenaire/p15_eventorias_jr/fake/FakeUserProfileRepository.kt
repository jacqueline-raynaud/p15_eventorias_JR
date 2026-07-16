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

    // On stocke les profils dans une liste en mémoire (notre "fausse" base de données)
    private val _profiles = MutableStateFlow<List<UserProfile>>(emptyList())

    // On simule un utilisateur connecté (par défaut "fake_current_uid")
    private var _currentUserId: String? = "fake_current_uid"

    // --- Outils pour les tests (pour préparer tes scénarios) ---

    fun setProfiles(list: List<UserProfile>) {
        _profiles.value = list
    }

    fun setCurrentUserId(uid: String?) {
        _currentUserId = uid
    }
    // -----------------------------------------------------------

    override fun getUserProfile(uid: String): Flow<UserProfile?> {
        // Correction : on utilise _profiles et non _events
        return _profiles.map { list -> list.find { it.uid == uid } }
    }

    override fun getCurrentUserId(): String? {
        return _currentUserId
    }

    override suspend fun createUserProfileIfMissing(profile: UserProfile) {
        val exists = _profiles.value.any { it.uid == profile.uid }
        if (!exists) {
            _profiles.value = _profiles.value + profile
        }
    }

    override suspend fun updateNotificationSetting(uid: String, enabled: Boolean) {
        _profiles.value = _profiles.value.map {
            if (it.uid == uid) it.copy(notificationEnabled = enabled) else it
        }
    }

    override suspend fun updateFcmToken(uid: String, token: String) {
        _profiles.value = _profiles.value.map {
            if (it.uid == uid) it.copy(fcmToken = token) else it
        }
    }

    override suspend fun syncNotificationSubscription(enabled: Boolean) {
        // Rien à faire pour le fake
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        _profiles.value = _profiles.value.map {
            if (it.uid == profile.uid) profile else it
        }
    }

    override suspend fun uploadUserAvatar(uid: String, avatarUri: Uri): String {
        return "https://fake-storage.com/avatar_$uid.jpg"
    }

    override suspend fun deleteProfileData(uid: String, avatarUrl: String) {
        _profiles.value = _profiles.value.filter { it.uid != uid }
    }

    override suspend fun deleteAuthAccount() {
        val oldUid = _currentUserId
        _currentUserId = null
        
        // Correction : on nomme la variable uidToDelete pour éviter la confusion avec "it"
        oldUid?.let { uidToDelete -> 
            _profiles.value = _profiles.value.filter { profile -> 
                profile.uid != uidToDelete 
            } 
        }
    }
}
