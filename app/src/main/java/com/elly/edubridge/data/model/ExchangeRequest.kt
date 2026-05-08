package com.elly.edubridge.data.model

data class ExchangeRequest(
    val requestId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val skillOffered: String = "",
    val skillWanted: String = "",
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)