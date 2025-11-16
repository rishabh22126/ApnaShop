package com.example.apnashop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.example.apnashop.firebase.AuthManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRequestScreen(navController: NavController) {

    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var extraNote by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("General") } // change UI to select real categories as you need

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val db = FirebaseFirestore.getInstance()
    val userId = AuthManager.getCurrentUserId()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("ApnaShop", fontSize = 22.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Cyan)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Raise Request", fontSize = 28.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Enter Item Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Enter Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = extraNote,
                onValueChange = { extraNote = it },
                label = { Text("Extra Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (userId == null) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (itemName.isBlank() || quantity.isBlank()) {
                        Toast.makeText(context, "Please enter item and quantity", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    // First get customer name (if you store it)
                    db.collection("Customers").document(userId).get()
                        .addOnSuccessListener { custDoc ->
                            val customerName = custDoc.getString("name") ?: ""

                            // create a new request id (so same id can be used everywhere)
                            val globalReqRef = db.collection("Requests").document() // generate id
                            val requestId = globalReqRef.id

                            val requestData = hashMapOf(
                                "id" to requestId,
                                "customerId" to userId,
                                "customerName" to customerName,
                                "itemName" to itemName,
                                "quantity" to quantity,
                                "extraNote" to extraNote,
                                "category" to selectedCategory,
                                "status" to "Pending",
                                "timestamp" to Timestamp.now()
                            )

                            // Find matching shopkeepers by either "category" OR "shopType"
                            db.collection("Shopkeepers")
                                .get()
                                .addOnSuccessListener { snapCategory ->
                                    val shopDocs = snapCategory.documents.toMutableList()

                                    // also check where shopType == selectedCategory (in case DB uses shopType)
                                    db.collection("Shopkeepers")
                                        .whereEqualTo("shopType", selectedCategory)
                                        .get()
                                        .addOnSuccessListener { snapShopType ->
                                            // merge results (avoid duplicates)
                                            for (d in snapShopType.documents) {
                                                if (shopDocs.none { it.id == d.id }) shopDocs.add(d)
                                            }

                                            // send to each shopkeeper found
                                            if (shopDocs.isNotEmpty()) {
                                                for (shopDoc in shopDocs) {
                                                    val shopId = shopDoc.id

                                                    // Write request under ShopkeeperRequests/{shopId}/Requests/{requestId}
                                                    db.collection("ShopkeeperRequests")
                                                        .document(shopId)
                                                        .collection("Requests")
                                                        .document(requestId)
                                                        .set(requestData)
                                                }
                                            }

                                            // Save in global Requests with same id
                                            globalReqRef.set(requestData)

                                            // Save in customer's own history
                                            db.collection("CustomerRequests")
                                                .document(userId)
                                                .collection("Requests")
                                                .document(requestId)
                                                .set(requestData)

                                            isLoading = false
                                            Toast.makeText(context, "Request sent successfully", Toast.LENGTH_LONG).show()
                                            navController.navigate("RequestDone_Screen")
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Error searching shopType: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Error searching category: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Error fetching customer data", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Send Request", fontSize = 18.sp)
            }
        }
    }
}
