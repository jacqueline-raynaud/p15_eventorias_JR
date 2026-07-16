package fr.quinquenaire.p15_eventorias_jr.data.remote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessagingManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) {
    companion object {
        const val EVENTS_TOPIC = "event_notifications"
    }

    suspend fun subscribe() {
        try {
            firebaseMessaging.subscribeToTopic(EVENTS_TOPIC).await()
        } catch (e: Exception) {
            Log.e("EventoriasApp", "Error subscribing to topic", e)
            throw e
        }
    }

    suspend fun unsubscribe() {
        try {
            firebaseMessaging.unsubscribeFromTopic(EVENTS_TOPIC).await()
        } catch (e: Exception) {
            Log.e("EventoriasApp", "Error unsubscribing from topic", e)
            throw e
        }
    }

    suspend fun getToken(): String? {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            Log.e("EventoriasApp", "Error getting FCM token", e)
            null
        }
    }
}