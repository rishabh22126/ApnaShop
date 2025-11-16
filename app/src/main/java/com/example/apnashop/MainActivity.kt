package com.example.apnashop

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.example.apnashop.ui.theme.ApnaShopTheme
import com.example.apnashop.utils.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Firebase (must be first)
        FirebaseApp.initializeApp(this)

        // ✅ Setup notification channel
        NotificationHelper.createNotificationChannel(this)

        // ✅ Ask permission (Android 13+)
        askNotificationPermission()

        enableEdgeToEdge()

        setContent {
            ApnaShopTheme {
                val navController = rememberNavController()

                // ✅ Handle notification tap navigation safely
                val destination = intent?.getStringExtra("navigateTo")
                val startDestination = when (destination) {
                    "CustomerNotifications_Screen" -> "CustomerNotifications_Screen"
                    else -> "splash_screen"
                }

                NavHost(navController = navController, startDestination = startDestination) {

                    composable("splash_screen") { SplashScreen(navController) }
                    composable("CustomerAndShopKeeper_Screen") { CustomerAndShopKeeper(navController) }
                    composable("CustomerDetails_Screen") { CustomerDetails(navController) }
                    composable("ShopkeeperDetails_Screen") { ShopkeeperDetails(navController) }
                    composable("ShopCategory_Screen") { ShopCategoryScreen(navController) }
                    composable("CustomerLogin_Screen") { CustomerLogin(navController) }
                    composable("CustomerRegistrationDone_Screen") { CustomerRegistrationDone(navController) }
                    composable("ShopkeeperDashboard_Screen") { ShopKeeperDashboard(navController) }
                    composable("ShopkeeperLogin_Screen") { ShopkeeperLogin(navController) }
                    composable("ShopkeeperRegistrationDone_Screen") { ShopkeeperRegistrtionDone(navController) }
                    composable("GenralShop_Screen") { GenralShop(navController) }
                    composable("MedicalShop_Screen") { MedicalShop(navController) }
                    composable("ElectronicShop_Screen") { ElectronicShop(navController) }
                    composable("VegetableShop_Screen") { VegetableShop(navController) }
                    composable("FishShop_Screen") { FishShop(navController) }
                    composable("HardwareShop_Screen") { HardwareShop(navController) }
                    composable("CustomerRequest_Screen") { CustomerRequestScreen(navController) }
                    composable("RequestDone_Screen") { RequestDone(navController) }
                    composable("CustomerProfile_Screen") { CustomerProfile(navController) }
                    composable("ShopkeeperProfile_Screen") { ShopkeeperProfile(navController) }
                    composable("CustomerNotifications_Screen") { CustomerNotificationScreen(navController) }
                }
            }
        }
    }

    // ✅ Request notification permission (Android 13+)
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }
    }
}
