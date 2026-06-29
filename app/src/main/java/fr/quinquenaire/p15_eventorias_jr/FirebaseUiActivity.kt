package fr.quinquenaire.p15_eventorias_jr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseUiActivity : AppCompatActivity() {

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
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            Log.d("FirebaseUiActivity", "UserProfile signed in: $user")
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            if (response == null) {
                finish()
            } else {
                val errorCode = response.error?.errorCode
                Log.e("FirebaseUI", "Sign-in error: $errorCode")
            }
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
            // ...
        }
    }
}
