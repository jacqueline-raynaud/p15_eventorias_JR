package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseAuthManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseMessagingManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val firestoreManager: FirebaseFirestoreManager,
    private val storageManager: FirebaseStorageManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val messagingManager: FirebaseMessagingManager
) : UserProfileRepository {

    override fun getUserProfile(uid: String): Flow<UserProfile?> {
        return firestoreManager.getUserProfile(uid)
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun createUserProfileIfMissing(profile: UserProfile) {
        firestoreManager.createUserProfileIfMissing(profile)
    }

    // notifications

    private suspend fun applyNotificationSubscription(enabled: Boolean) {
        if (enabled) messagingManager.subscribe() else messagingManager.unsubscribe()
    }

    override suspend fun updateNotificationSetting(uid: String, enabled: Boolean) {
        applyNotificationSubscription(enabled)
        firestoreManager.updateNotificationSetting(uid, enabled)
    }

    override suspend fun syncNotificationSubscription(enabled: Boolean) {
        try {
            applyNotificationSubscription(enabled)
        } catch (e: Exception) {
            Log.e("EventoriasApp", "Error syncing notification subscription", e)
            // Non bloquant : ne doit jamais empêcher le démarrage de l'app
        }
    }

    // mise à jour profil

    override suspend fun updateUserProfile(profile: UserProfile) {
        firestoreManager.updateUserProfile(profile)
    }

    override suspend fun uploadUserAvatar(uid: String, avatarUri: Uri): String {
        return storageManager.uploadUserAvatar(uid, avatarUri)
    }

    // suppression du compte

    override suspend fun deleteProfileData(uid: String, avatarUrl: String) {
        storageManager.deleteAvatarByUrl(avatarUrl)
        firestoreManager.deleteUserProfile(uid)
    }
    override suspend fun deleteAuthAccount() {
        firebaseAuthManager.deleteCurrentUserAccount()
    }
}
