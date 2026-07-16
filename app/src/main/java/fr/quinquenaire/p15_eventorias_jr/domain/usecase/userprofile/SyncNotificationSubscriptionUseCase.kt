package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseMessagingManager
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncNotificationSubscriptionUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val messagingManager: FirebaseMessagingManager
) {
    suspend operator fun invoke() {
        val uid = userProfileRepository.getCurrentUserId() ?: return

        // recupère token mfc
        val token = messagingManager.getToken()
        if (token != null) {
            userProfileRepository.updateFcmToken(uid, token)
        }
/*
        // abonnement aux topics
        val profile = userProfileRepository.getUserProfile(uid).first() ?: return
        userProfileRepository.syncNotificationSubscription(profile.notificationEnabled)
    */
    }
}