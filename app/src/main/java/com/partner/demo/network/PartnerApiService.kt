package com.partner.demo.network

import com.partner.demo.NetworkModule
import com.partner.demo.model.TransferRequest
import com.partner.demo.model.TransferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 合作伙伴后端API服务
 */
interface PartnerApiService {

    /**
     * 发起转账
     */
    @POST("api/transfer")
    suspend fun initiateTransfer(@Body request: TransferRequest): Response<TransferResponse>

    /**
     * 健康检查
     */
    @GET("api/health")
    suspend fun healthCheck(): Response<String>

    companion object {
        fun create(): PartnerApiService {
            return NetworkModule.create()
        }
    }
}