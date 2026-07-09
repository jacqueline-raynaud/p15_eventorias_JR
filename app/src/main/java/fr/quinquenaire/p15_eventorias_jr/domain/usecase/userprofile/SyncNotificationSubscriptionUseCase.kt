package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import android.util.Log
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncNotificationSubscriptionUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke() {
        val uid = userProfileRepository.getCurrentUserId() ?: return
        val profile = userProfileRepository.getUserProfile(uid).first() ?: return
        Log.d("EventoriasApp-jr", "SyncNotificationSubscriptionUseCase: $profile")
        userProfileRepository.syncNotificationSubscription(profile.notificationEnabled)
    }
}