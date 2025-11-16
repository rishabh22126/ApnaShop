package com.example.apnashop

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.apnashop.firebase.AuthManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(request: Any, onBackClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val shopId = AuthManager.getCurrentUserId()
    val context = LocalContext.current

    var extraInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Request Details",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Cyan)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Customer: ${request.customerName}", fontWeight = FontWeight.Bold)
            Text("Item: ${request.itemName}")
            Text("Quantity: ${request.quantity}")
            if (request.extraNote.isNotEmpty())
                Text("Note: ${request.extraNote}", color = Color.Gray)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = extraInfo,
                onValueChange = { extraInfo = it },
                label = { Text("Additional Info (Price, etc.)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        updateRequestStatusAndNotify(
                            db = db,
                            shopId = shopId,
                            request = request,
                            newStatus = "Available",
                            extraInfo = extraInfo,
                            context = context,
                            onBackClick = onBackClick
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Available") }

                Button(
                    onClick = {
                        updateRequestStatusAndNotify(
                            db = db,
                            shopId = shopId,
                            request = request,
                            newStatus = "Not Available",
                            extraInfo = extraInfo,
                            context = context,
                            onBackClick = onBackClick
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Not Available") }
            }
        }
    }
}

// ✅ Request model
data class RequestModel(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val itemName: String = "",
    val quantity: String = "",
    val extraNote: String = "",
    val status: String = ""
)

// ✅ Update & send notification
fun updateRequestStatusAndNotify(
    db: FirebaseFirestore,
    shopId: String?,
    request: Any,
    newStatus: String,
    extraInfo: String,
    context: Context,
    onBackClick: () -> Unit
) {
    if (shopId == null) {
        Toast.makeText(context, "Shopkeeper not logged in", Toast.LENGTH_SHORT).show()
        return
    }

    val updateData = mapOf(
        "status" to newStatus,
        "shopResponse" to extraInfo,
        "responseTime" to Timestamp.now()
    )

    // ✅ Step 1: Fetch shopkeeper name for notification
    db.collection("Shopkeepers").document(shopId).get()
        .addOnSuccessListener { shopDoc ->
            val shopName = shopDoc.getString("shopName") ?: "Shopkeeper"

            // ✅ Step 2: Update Shopkeeper side
            db.collection("ShopkeeperRequests").document(shopId)
                .collection("Requests").document(request.id)
                .update(updateData)
                .addOnSuccessListener {
                    // ✅ Step 3: Update Customer side
                    db.collection("CustomerRequests")
                        .document(request.customerId)
                        .collection("Requests")
                        .document(request.id)
                        .update(updateData)
                        .addOnSuccessListener {
                            // ✅ Step 4: Send notification to customer
                            val notificationData = hashMapOf(
                                "customerId" to request.customerId,
                                "title" to "$shopName updated your request",
                                "message" to "Status: $newStatus\nShop Info: ${if (extraInfo.isNotEmpty()) extraInfo else "No extra details"}",
                                "timestamp" to Timestamp.now(),
                                "isRead" to false
                            )

                            db.collection("Notifications")
                                .add(notificationData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Response updated & notification sent!", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Notification", "Failed to send: ${e.message}")
                                    Toast.makeText(context, "Updated but notification failed", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                }
                        }
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to get shop info", Toast.LENGTH_SHORT).show()
        }
}
