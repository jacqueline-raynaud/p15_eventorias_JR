package fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile

import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import fr.quinquenaire.p15_eventorias_jr.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val getCurrentUidUseCase: GetCurrentUidUseCase,
    private val getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
    private val userProfileRepository: UserProfileRepository,
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke() {
        val uid = getCurrentUidUseCase()
            ?: throw IllegalStateException("Utilisateur non connecté")

        val profile = getCurrentUserProfileUseCase().first()

        // Attention à l'ordre des appels : anonymiser > supprimer profil > supprimer compte

        // 1. Anonymiser les événements dont l'utilisateur est organisateur
        eventRepository.anonymizeOrganizerEvents(uid)

        // 2. Supprimer l'avatar (si sur Storage) + le document profil
        userProfileRepository.deleteProfileData(uid, profile?.avatarUrl.orEmpty())

        // 3. Supprimer le compte Firebase Auth — en tout dernier, car après
        //    cet appel l'utilisateur n'est plus authentifié : toute action
        //    nécessitant request.auth.uid échouerait.
        userProfileRepository.deleteAuthAccount()
    }
}