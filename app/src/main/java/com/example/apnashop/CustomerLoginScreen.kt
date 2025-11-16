package com.example.apnashop

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.apnashop.firebase.AuthManager
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLogin(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Scaffold { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color.White)
                    )
                )
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Welcome to",
                    fontSize = 18.sp,
                    color = Color.Gray
                )

                Text(
                    text = "ApnaShop",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1565C0)
                )

//                Spacer(modifier = Modifier.height(20.dp))
//
//                Image(
//                    painter = painterResource(id = R.drawable.apnalogo2), // 👈 YOUR LOGO
//                    contentDescription = "App Logo",
//                    modifier = Modifier
//                        .size(140.dp)
//                )

                Spacer(modifier = Modifier.height(30.dp))

                // 🔷 Card Container
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }

                        // 🔥 Login Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Email and Password cannot be empty."
                                } else {
                                    errorMessage = ""
                                    isLoading = true

                                    db.collection("Shopkeepers")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if (!documents.isEmpty) {
                                                isLoading = false
                                                errorMessage =
                                                    "This email belongs to a Shopkeeper. Please login as Shopkeeper."
                                            } else {
                                                AuthManager.loginUser(email, password) { success, errorMsg ->
                                                    isLoading = false
                                                    if (success) {
                                                        Toast.makeText(
                                                            context,
                                                            "Login Successful!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navController.navigate("ShopCategory_Screen") {
                                                            popUpTo("CustomerAndShopKeeper_Screen") { inclusive = true }
                                                        }
                                                    } else {
                                                        errorMessage =
                                                            errorMsg ?: "Login failed."
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Error: ${e.message}"
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 🔗 Actions
                TextButton(
                    onClick = { navController.navigate("CustomerForgotPassword") }
                ) {
                    Text("Forgot Password?", color = Color.Blue)
                }

                OutlinedButton(
                    onClick = { navController.navigate("CustomerDetails_Screen") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Create New Account", color = Color.Blue)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Privacy Policy",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://rishabh22126.github.io/apnashop-privacy-policy/")
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}