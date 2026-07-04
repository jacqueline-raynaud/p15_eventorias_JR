package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val firestoreManager: FirebaseFirestoreManager,
    private val storageManager: FirebaseStorageManager,
    private val firebaseAuth: FirebaseAuth
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

    override suspend fun updateNotificationSetting(uid: String, enabled: Boolean) {
        firestoreManager.updateNotificationSetting(uid, enabled)
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        firestoreManager.updateUserProfile(profile)
    }

    override suspend fun uploadUserAvatar(uid: String, avatarUri: Uri): String {
        return storageManager.uploadUserAvatar(uid, avatarUri)
    }
}
