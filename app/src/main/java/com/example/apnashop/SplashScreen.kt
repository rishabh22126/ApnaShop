package com.example.apnashop

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash_screen"
    const val ROLE_SELECTION = "CustomerAndShopKeeper_Screen"
}

@Composable
fun SplashScreen(navController: NavController) {
    val alpha = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
            delay(1500)

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                // Check user type: Customer or Shopkeeper
                db.collection("Customers").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            Toast.makeText(context, "Welcome back, ${doc.getString("name") ?: "Customer"}!", Toast.LENGTH_SHORT).show()
                            navController.navigate("ShopCategory_Screen") {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            db.collection("Shopkeepers").document(uid).get()
                                .addOnSuccessListener { shopDoc ->
                                    if (shopDoc.exists()) {
                                        Toast.makeText(context, "Welcome back, ${shopDoc.getString("ownerName") ?: "Shopkeeper"}!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("ShopkeeperDashboard_Screen") {
                                            popUpTo(Routes.SPLASH) { inclusive = true }
                                        }
                                    } else {
                                        // If user exists but no record found, go to role selection
                                        navController.navigate(Routes.ROLE_SELECTION) {
                                            popUpTo(Routes.SPLASH) { inclusive = true }
                                        }
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        navController.navigate(Routes.ROLE_SELECTION) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
            } else {
                navController.navigate(Routes.ROLE_SELECTION) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Center Logo + App Name
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha.value),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apnalogo2),
                    contentDescription = "AppLogo",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ApnaShop",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom Credits
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DESIGNED BY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Text(
                    text = "N-R",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
