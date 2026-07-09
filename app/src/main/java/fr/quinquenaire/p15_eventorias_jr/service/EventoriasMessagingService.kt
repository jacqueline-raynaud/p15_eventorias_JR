package fr.quinquenaire.p15_eventorias_jr.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import fr.quinquenaire.p15_eventorias_jr.R

class EventoriasMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Handle notification
        val title = remoteMessage.notification?.title ?: "Eventorias"
        val body = remoteMessage.notification?.body ?: ""

        sendNotification(title, body)
    }

    override fun onNewToken(token: String) {
        // Send token to your backend server if needed
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "events_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
