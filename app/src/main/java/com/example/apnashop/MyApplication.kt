package com.example.apnashop

import android.app.Application
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // You can prompt this in your first screen’s Activity if needed
            }
        }

        // Fetch FCM token for this device
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            println("✅ FCM Token: $token")
        }
    }
}
