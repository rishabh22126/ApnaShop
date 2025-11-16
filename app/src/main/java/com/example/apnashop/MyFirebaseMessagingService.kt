package com.example.apnashop.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.apnashop.MainActivity
import com.example.apnashop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("UserTokens")
                .document(userId)
                .set(mapOf("token" to token))
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        var title = "ApnaShop"
        var body = "You have a new update"

        // notification payload
        remoteMessage.notification?.let {
            title = it.title ?: title
            body = it.body ?: body
        }

        // data payload
        if (remoteMessage.data.isNotEmpty()) {
            title = remoteMessage.data["title"] ?: title
            body = remoteMessage.data["body"] ?: body
        }

        showNotification(title, body)
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "order_updates"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for order status updates"
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }

        // 🧭 Intent to open CustomerNotificationScreen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("navigateTo", "CustomerNotifications_Screen")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.apnalogo2)
            .setContentTitle(title ?: "ApnaShop")
            .setContentText(message ?: "You have a new order update.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
