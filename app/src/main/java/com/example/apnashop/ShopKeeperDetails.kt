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
import com.example.apnashop.utils.PincodeUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopkeeperDetails(navController: NavController) {

    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }

    var pincode by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var chooseShop by remember { mutableStateOf("") }

    val shopTypes = listOf("General", "Medical", "Hardware", "Electronics", "Vegetable", "Fish")

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()
    var isFetchingLocation by remember { mutableStateOf(false) }

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
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Shopkeeper Registration",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )

                OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Shop Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = chooseShop,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Shop Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        shopTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    chooseShop = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = shopAddress, onValueChange = { shopAddress = it }, label = { Text("Shop Address") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = pincode,
                    onValueChange = {
                        pincode = it

                        if (it.length == 6 && district.isEmpty()) {

                            isFetchingLocation = true

                            PincodeUtils.fetchDistrictState(it) { d, s ->

                                android.os.Handler(android.os.Looper.getMainLooper()).post {

                                    isFetchingLocation = false

                                    if (d != null && s != null) {
                                        district = d
                                        state = s
                                    }
                                }
                            }
                        }
                    },
                    label = { Text("PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = district,
                    onValueChange = {},
                    label = { Text("District") },
                    enabled = false,
                    trailingIcon = {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = {},
                    label = { Text("State") },
                    enabled = false,
                    trailingIcon = {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

//                OutlinedTextField(
//                    value = state,
//                    onValueChange = {},
//                    label = { Text("State") },
//                    enabled = false,
//                    modifier = Modifier.fillMaxWidth()
//                )

                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = retypePassword, onValueChange = { retypePassword = it }, label = { Text("Re-enter Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth())

                if (errorMessage.isNotEmpty())
                    Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {

                        if (shopName.isBlank() || ownerName.isBlank() || phoneNumber.isBlank() ||
                            email.isBlank() || password.isBlank() || shopAddress.isBlank() || chooseShop.isBlank()
                        ) {
                            errorMessage = "Please fill in all fields."
                            return@Button
                        }

                        if (password != retypePassword) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        val customerRef = db.collection("Customers")

                        customerRef.whereEqualTo("email", email).get()
                            .addOnSuccessListener { result ->

                                if (!result.isEmpty) {
                                    isLoading = false
                                    errorMessage = "This email is already registered as a Customer."
                                    return@addOnSuccessListener
                                }

                                AuthManager.registerUser(email, password) { success, authError ->

                                    if (success) {

                                        val userId = AuthManager.getCurrentUserId()

                                        if (userId != null) {

                                            val shopData = hashMapOf(
                                                "uid" to userId,
                                                "shopName" to shopName,
                                                "ownerName" to ownerName,
                                                "phone" to phoneNumber,
                                                "email" to email,
                                                "shopAddress" to shopAddress,
                                                "shopType" to chooseShop,
                                                "pincode" to pincode,
                                                "district" to district,
                                                "state" to state,
                                                "createdAt" to Timestamp.now()
                                            )

                                            db.collection("Shopkeepers").document(userId)
                                                .set(shopData)
                                                .addOnSuccessListener {

                                                    val categoryCollection = when (chooseShop) {
                                                        "Medical" -> "MedicalShops"
                                                        "General" -> "GeneralShops"
                                                        "Hardware" -> "HardwareShops"
                                                        "Electronics" -> "ElectronicsShops"
                                                        "Vegetable" -> "VegetableShops"
                                                        "Fish" -> "FishShops"
                                                        else -> "OtherShops"
                                                    }

                                                    db.collection(categoryCollection).document(userId)
                                                        .set(shopData)
                                                        .addOnSuccessListener {

                                                            isLoading = false

                                                            Toast.makeText(
                                                                context,
                                                                "Shop Registered Successfully!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            navController.navigate("ShopkeeperRegistrationDone_Screen") {
                                                                popUpTo("CustomerAndShopKeeper_Screen") { inclusive = false }
                                                            }

                                                        }
                                                        .addOnFailureListener {
                                                            isLoading = false
                                                            errorMessage = "Failed to save shop data"
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    errorMessage = "Failed to save shop data"
                                                }

                                        } else {
                                            isLoading = false
                                            errorMessage = "User ID not found"
                                        }

                                    } else {

                                        isLoading = false
                                        errorMessage = authError ?: "Registration failed"

                                        if (errorMessage.contains("already in use")) {
                                            errorMessage = "This email is already registered"
                                        }
                                    }
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B), contentColor = Color.White)
                ) {

                    if (isLoading)
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else
                        Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.navigate("ShopkeeperLogin_Screen") },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B))
                ) {
                    Text("Already have an account? Login", fontSize = 16.sp)
                }
            }
        }
    }
}