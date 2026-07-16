package fr.quinquenaire.p15_eventorias_jr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import fr.quinquenaire.p15_eventorias_jr.domain.usecase.userprofile.CreateUserProfileUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseUiActivity : AppCompatActivity() {
    @Inject
    lateinit var createUserProfileUseCase: CreateUserProfileUseCase

    // create launcher for sign-in
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Déjà connecté -> Go vers MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Non connecté -> Lancer l'écran FirebaseUI
            createSingInIntent()
        }
    }

    private fun createSingInIntent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.logo_eventorias)
            .setTheme(R.style.Theme_P15_eventorias_jr).build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // Création du document users si 1re connexion.
                // lifecycleScope : coroutine liée au cycle de vie de l'activity.
                lifecycleScope.launch {
                    // displayName de Firebase Auth = "Prénom Nom" (Google) ou
                    // le nom saisi (email). On le découpe simplement.
                    val names = user.displayName?.split(" ") ?: emptyList()
                    val token = FirebaseMessaging.getInstance().token.await() // Nécessite import kotlinx.coroutines.tasks.await


                    createUserProfileUseCase(
                        UserProfile(
                            uid = user.uid,
                            firstName = names.firstOrNull() ?: "",
                            lastName = names.drop(1).joinToString(" "),
                            email = user.email ?: "",
                            avatarUrl = user.photoUrl?.toString() ?: "",
                            notificationEnabled = true,
                            fcmToken = token
                        )
                    )
                    startActivity(Intent(this@FirebaseUiActivity, MainActivity::class.java))
                    finish()
                }
            }
        } else {
            if (response == null) {
                finish()   // utilisateur a annulé → on ferme
            } else {
                val errorCode = response.error?.errorCode
                Log.e("FirebaseUI", "Sign-in error: $errorCode")
            }
        }
    }
}

