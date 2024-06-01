package com.hit11.zeus.model


data class Contest(
    val id: Int,
    var docRef: String? = "",
    val title: String,
    val prize: String,
    val originalAmount: String,
    val discountedAmount: String,
    val discountEndsIn: String,
    val discountText: String,
    val detail: String,
    val spotsLeft: String,
    val totalSpots: String,
    val specialPrizes: String
)
