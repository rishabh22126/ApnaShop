package com.example.apnashop.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ShopRepository {

    fun getNearbyShops(
        shopType: String,
        onResult: (List<Map<String, Any>>) -> Unit
    ) {

        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onResult(emptyList())
            return
        }

        db.collection("Customers")
            .document(uid)
            .get()
            .addOnSuccessListener { customerDoc ->

                val customerPincode = customerDoc.getString("pincode")

                db.collection("Shopkeepers")
                    .whereEqualTo("shopType", shopType)
                    .whereEqualTo("pincode", customerPincode)
                    .get()
                    .addOnSuccessListener { result ->

                        val shopList = mutableListOf<Map<String, Any>>()

                        for (doc in result.documents) {
                            val data = doc.data
                            if (data != null) {
                                shopList.add(data)
                            }
                        }

                        onResult(shopList)
                    }
                    .addOnFailureListener {
                        onResult(emptyList())
                    }
            }
    }
}