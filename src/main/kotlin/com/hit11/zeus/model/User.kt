package com.hit11.zeus.model

data class User(
    val id: Int  = 0,
    val firebaseUID: String = "",
    val email: String? = "",
    val name: String? = "",
    val phone: String = "",
    val walletBalance: Double = 0.0,
    val withdrawalBalance: Double = 0.0,
)