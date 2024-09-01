package com.hit11.zeus.model


data class UserReward(
    val rewardType: String,
    val rewardAmountType: String,
    val rewardAmount: Double,
    val rewardDate: Long,
)