package fr.quinquenaire.p15_eventorias_jr.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun deleteCurrentUserAccount() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("Aucun utilisateur connecté")
        user.delete().await()
        // Peut lever FirebaseAuthRecentLoginRequiredException
        // si la connexion date de trop longtemps — à gérer côté ViewModel.
    }
}