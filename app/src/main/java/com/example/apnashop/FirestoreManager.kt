package com.example.apnashop.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔹 Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // 🔹 Fetch data based on collection name (Customers or Shopkeepers)
    fun fetchUserData(
        collection: String,
        onResult: (Map<String, Any>?) -> Unit
    ) {
        val userId = getCurrentUserId() ?: return onResult(null)

        db.collection(collection)
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult(doc.data)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
