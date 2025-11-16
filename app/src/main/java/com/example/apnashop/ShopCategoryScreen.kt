package com.example.apnashop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopCategoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var hasNewNotification by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // ✅ Listen to Firestore for new notifications
    LaunchedEffect(Unit) {
        db.collection("Notifications")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    hasNewNotification = true
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shop Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    // 🔔 Notification Bell with Red Dot
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        IconButton(onClick = {
                            hasNewNotification = false
                            navController.navigate("CustomerNotifications_Screen")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.Black
                            )
                        }

                        // 🔴 Red dot indicator
                        if (hasNewNotification) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, shape = CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("CustomerProfile_Screen")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Cyan,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(paddingValues)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Category",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(25.dp))

            // ✅ Category Grid
            CategoryGrid(navController)

            Spacer(modifier = Modifier.height(30.dp))

            // ✅ Manual Refresh Button
            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    isRefreshing = true
                    hasNewNotification = false
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1500)
                        isRefreshing = false
                    }
                },

                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Refresh",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGrid(navController: NavController) {
    val categories = listOf(
        Triple("General Store", R.drawable.store, "GenralShop_Screen"),
        Triple("Medical Store", R.drawable.medical, "MedicalShop_Screen"),
        Triple("Electronic Store", R.drawable.electronic, "ElectronicShop_Screen"),
        Triple("Vegetable Store", R.drawable.vegetables, "VegetableShop_Screen"),
        Triple("Fish (Non-Veg) Store", R.drawable.fishshop, "FishShop_Screen"),
        Triple("Hardware Store", R.drawable.hardware, "HardwareShop_Screen")
    )

    for (i in categories.indices step 2) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CategoryCard(
                name = categories[i].first,
                iconRes = categories[i].second,
                onClick = { navController.navigate(categories[i].third) }
            )

            if (i + 1 < categories.size) {
                CategoryCard(
                    name = categories[i + 1].first,
                    iconRes = categories[i + 1].second,
                    onClick = { navController.navigate(categories[i + 1].third) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, iconRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(150.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = name,
                modifier = Modifier.size(70.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}
