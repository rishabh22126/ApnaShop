package com.example.apnashop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*

@Composable
fun ShopkeeperRegistrtionDone(navController: NavController) {

    // 🎞️ Load Lottie Animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))

    // 🌈 Background gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF1F8E9), Color(0xFFE8F5E9))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🎉 Success Title
            Text(
                text = "🎉 Registration Successful!",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your shopkeeper account has been created successfully.",
                color = Color(0xFF555555),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ✅ Success Animation
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 🔘 Button to Login
            Button(
                onClick = {
                    navController.navigate("ShopkeeperLogin_Screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Go to Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
