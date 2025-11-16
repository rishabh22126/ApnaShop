package com.example.apnashop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.Any

@Composable
fun VegetableShop(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    var shopList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Fetch Vegetable shops from Firestore
    LaunchedEffect(Unit) {
        db.collection("VegetableShops")
            .get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val data = document.data
                    tempList.add(data)
                }
                shopList = tempList
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🏪 Title
        Text(
            text = "Welcome to ApnaShop",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF222222),
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🛍️ Main Image
        Image(
            painter = painterResource(id = R.drawable.vegetablebord),
            contentDescription = "Vegetable Shop Icon",
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🧾 Subheading
        Text(
            text = "Choose Your Store",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 🔄 Show loading or data
        when {
            isLoading -> {
                CircularProgressIndicator(color = Color.Black)
            }

            shopList.isEmpty() -> {
                Text("No shops found", color = Color.Gray)
            }

            else -> {
                // 🔹 Display fetched shops dynamically (2 in each row)
                val chunkedList = shopList.chunked(2)
                chunkedList.forEach { rowShops ->
                    StoreRow2(
                        navController = navController,
                        storeNames = rowShops.map {
                            it["shopName"]?.toString() ?: "Unnamed Store"
                        },
                        imageRes = R.drawable.vegetables
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 📨 Send Request Button
        Button(
            onClick = {
                navController.navigate("CustomerRequest_Screen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Send Request To All",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StoreRow2(navController: NavController, storeNames: List<String>, imageRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        storeNames.forEach { name ->
            StoreCard2(
                name = name,
                imageRes = imageRes,
                onClick = {
                    navController.navigate("CustomerRequest_Screen")
                }
            )
        }
    }
}

@Composable
fun StoreCard2(name: String, imageRes: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(150.dp)
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
