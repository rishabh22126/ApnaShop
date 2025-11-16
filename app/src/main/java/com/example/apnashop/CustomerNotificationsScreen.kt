package com.example.apnashop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: String = "",
    var isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerNotificationScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var notifications by remember { mutableStateOf(listOf<NotificationItem>()) }
    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    // ✅ Listen only to notifications of logged-in customer
    DisposableEffect(Unit) {
        if (currentUserId != null) {
            listener = db.collection("Notifications")
                .whereEqualTo("customerId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->

                    if (snapshot != null) {
                        notifications = snapshot.documents.map { doc ->
                            NotificationItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getTimestamp("timestamp")?.toDate().toString(),
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }
                    }

                }
        }
        onDispose { listener?.remove() }
    }

    // ✅ Mark unread as read once displayed
    LaunchedEffect(notifications) {
        if (currentUserId != null) {
            notifications.filter { !it.isRead }.forEach { notif ->
                db.collection("Notifications").document(notif.id)
                    .update("isRead", true)
            }
        }
    }

    // ✅ UI — No changes from your original
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Notifications", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(notifications) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead)
                                    Color(0xFFF1F1F1)
                                else
                                    Color(0xFFE0F7FA)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(notif.message, fontSize = 16.sp)
                                Text(notif.timestamp, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
