package com.partner.demo.model

import com.google.gson.annotations.SerializedName

/**
 * 转账请求
 */
data class TransferRequest(
    @SerializedName("cardNumber")
    val cardNumber: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String = "USD"
)

/**
 * 转账响应
 */
data class TransferResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("transactionToken")
    val transactionToken: String?,

    @SerializedName("authUrl")
    val authUrl: String?,

    @SerializedName("transactionId")
    val transactionId: String?,

    @SerializedName("message")
    val message: String?
)

/**
 * 交易结果（从WebView回调）
 */
data class TransactionResult(
    val success: Boolean,
    val transactionId: String? = null
)