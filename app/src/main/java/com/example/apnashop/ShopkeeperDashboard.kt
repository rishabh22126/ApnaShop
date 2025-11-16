package com.example.apnashop

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.apnashop.firebase.AuthManager
import com.example.apnashop.utils.NotificationHelper
import com.google.firebase.firestore.*

data class Any(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val itemName: String = "",
    val quantity: String = "",
    val extraNote: String = "",
    val status: String = "Pending"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopKeeperDashboard(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val shopkeeperId = AuthManager.getCurrentUserId()
    val requests = remember { mutableStateListOf<Any>() }
    var selectedRequest by remember { mutableStateOf<Any?>(null) }
    var listener: ListenerRegistration? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pending, 1 = History
    var isFirstLoad by remember { mutableStateOf(true) }

    // listen for changes
    LaunchedEffect(shopkeeperId) {
        if (shopkeeperId != null) {
            listener = db.collection("ShopkeeperRequests")
                .document(shopkeeperId)
                .collection("Requests")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // build temp list from snapshot
                        val temp = mutableListOf<Any>()
                        for (doc in snapshot.documents) {
                            val req = Any(
                                id = doc.id,
                                customerId = doc.getString("customerId") ?: "",
                                customerName = doc.getString("customerName") ?: "",
                                itemName = doc.getString("itemName") ?: "",
                                quantity = doc.getString("quantity") ?: "",
                                extraNote = doc.getString("extraNote") ?: "",
                                status = doc.getString("status") ?: "Pending"
                            )
                            temp.add(req)
                        }

                        // set to state list (replace)
                        requests.clear()
                        requests.addAll(temp)

                        // notify on new docs
                        if (!isFirstLoad) {
                            snapshot.documentChanges.forEach { change ->
                                if (change.type == DocumentChange.Type.ADDED) {
                                    val itemName = change.document.getString("itemName") ?: "item"
                                    NotificationHelper.showNotification(context, "New request for $itemName", "New request for $itemName")
                                    Toast.makeText(context, "New request received for $itemName", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        isFirstLoad = false

                        // If some entries don't have customerName, fetch them
                        temp.filter { it.customerName.isBlank() && it.customerId.isNotBlank() }.forEach { r ->
                            db.collection("Customers").document(r.customerId).get()
                                .addOnSuccessListener { custDoc ->
                                    val name = custDoc.getString("name") ?: "Unknown"
                                    val idx = requests.indexOfFirst { it.id == r.id }
                                    if (idx != -1) {
                                        requests[idx] = requests[idx].copy(customerName = name)
                                    }
                                }
                                .addOnFailureListener { /* ignore */ }
                        }
                    }
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose { listener?.remove() }
    }

    BackHandler(enabled = selectedRequest != null) {
        selectedRequest = null
    }

    if (selectedRequest == null) {
        Scaffold(
            topBar = {
                CustomTopAppBar(
                    title = "ApnaShop",
                    showBackButton = false,
                    onBackClick = {},
                    onProfileClick = { navController.navigate("ShopkeeperProfile_Screen") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // show shop details card
                ShopDetailsCard(shopkeeperId)

                // tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Pending") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("History") })
                }

                val filtered = when (selectedTab) {
                    0 -> requests.filter { it.status == "Pending" }
                    1 -> requests.filter { it.status != "Pending" }
                    else -> requests
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No requests to show.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { r ->
                            RequestCard(customerRequest = r) {
                                // only open details for Pending
                                if (selectedTab == 0) selectedRequest = it
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    } else {
        RequestDetailsScreen(
            request = selectedRequest!!,
            onBackClick = { selectedRequest = null }
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(title: String, showBackButton: Boolean, onBackClick: () -> Unit, onProfileClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        navigationIcon = {
            if (showBackButton) IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            else IconButton(onClick = {}) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
        },
        actions = {
            Button(onClick = onProfileClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(end = 8.dp)) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Color.Black)
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Cyan,
            titleContentColor = Color.Black
        )
    )
}

@Composable
fun ShopDetailsCard(shopkeeperId: String?) {
    val db = FirebaseFirestore.getInstance()
    var shopName by remember { mutableStateOf("N/A") }
    var shopType by remember { mutableStateOf("N/A") }
    var shopAddress by remember { mutableStateOf("N/A") }
    var verified by remember { mutableStateOf(false) }

    LaunchedEffect(shopkeeperId) {
        if (shopkeeperId != null) {
            db.collection("Shopkeepers").document(shopkeeperId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        shopName = doc.getString("shopName") ?: doc.getString("ownerName") ?: "N/A"
                        shopType = doc.getString("shopType") ?: doc.getString("category") ?: "N/A"
                        shopAddress = doc.getString("address") ?: doc.getString("shopAddress") ?: doc.getString("addressLine") ?: "N/A"
                        verified = doc.getBoolean("verified") ?: false
                    }
                }
                .addOnFailureListener { Log.e("ShopDetails", "err ${it.message}") }
        }
    }

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(shopName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Type: $shopType", fontSize = 16.sp, color = Color.Black)
            Text("Address: $shopAddress", fontSize = 16.sp, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = verified, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = Color.Green, uncheckedColor = Color.Red))
                Text(text = if (verified) "Verified" else "Not Verified", color = if (verified) Color.Green else Color.Red, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun RequestCard(customerRequest: Any, onClick: (Any) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(customerRequest) }, shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(customerRequest.itemName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Customer: ${customerRequest.customerName.ifEmpty { "Unknown" }}", fontSize = 15.sp, color = Color.DarkGray)
            Text("Quantity: ${customerRequest.quantity}", fontSize = 15.sp, color = Color.DarkGray)
            if (customerRequest.extraNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: ${customerRequest.extraNote}", fontSize = 14.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = customerRequest.status, color = when (customerRequest.status) {
                "Available" -> Color(0xFF4CAF50)
                "Not Available" -> Color.Red
                else -> Color.Gray
            }, fontWeight = FontWeight.Bold)
        }
    }
}