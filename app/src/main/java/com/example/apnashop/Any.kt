package com.example.apnashop

data class CustomerRequest(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "", // ✅ Added field
    val itemName: String = "",
    val quantity: String = "",
    val extraNote: String = "",
    val status: String = "Pending"
)
