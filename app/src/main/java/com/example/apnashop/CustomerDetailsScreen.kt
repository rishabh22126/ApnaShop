package com.example.apnashop

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.apnashop.firebase.AuthManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetails(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ApnaShop",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00BCD4),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE0F7FA))
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Customer Registration",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = retypePassword, onValueChange = { retypePassword = it }, label = { Text("Re-enter Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth())

                if (errorMessage.isNotEmpty()) Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || phoneNumber.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill in all fields."
                            return@Button
                        }
                        if (password != retypePassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        // 🔍 Check if email exists in Shopkeepers collection
                        val shopRef = db.collection("Shopkeepers")
                        shopRef.whereEqualTo("email", email).get()
                            .addOnSuccessListener { result ->
                                if (!result.isEmpty) {
                                    isLoading = false
                                    errorMessage = "This email is already registered as a Shopkeeper."
                                    return@addOnSuccessListener
                                }

                                // ✅ Continue registration
                                AuthManager.registerUser(email, password) { success, authError ->
                                    if (success) {
                                        val userId = AuthManager.getCurrentUserId()
                                        if (userId != null) {
                                            val userData = hashMapOf(
                                                "uid" to userId,
                                                "name" to name,
                                                "phone" to phoneNumber,
                                                "email" to email,
                                                "createdAt" to Timestamp.now()
                                            )

                                            db.collection("Customers").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                                    navController.navigate("CustomerRegistrationDone_Screen") {
                                                        popUpTo("CustomerAndShopKeeper_Screen") { inclusive = false }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    errorMessage = "Failed to save data: ${e.message}"
                                                }

                                        } else {
                                            isLoading = false
                                            errorMessage = "User ID not found."
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = authError ?: "Registration failed. Try again."
                                        if (errorMessage.contains("already in use")) {
                                            errorMessage = "This email is already registered."
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = "Error checking email: ${it.message}"
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B), contentColor = Color.White)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.navigate("CustomerLogin_Screen") },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B))
                ) {
                    Text("Already have an account? Login", fontSize = 16.sp)
                }
            }
        }
    }
}
