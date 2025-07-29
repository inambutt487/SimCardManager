package com.ultranet.simcardmanager.data.api

import com.google.gson.annotations.SerializedName
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

// Response wrapper for API responses
data class TelecomPlansResponse(
    val success: Boolean,
    val data: List<TelecomPlan>,
    val count: Int? = null,
    val error: String? = null,
    val message: String? = null
)

// Response wrapper for single plan
data class TelecomPlanResponse(
    val success: Boolean,
    val data: TelecomPlan,
    val error: String? = null,
    val message: String? = null
)

interface TelecomApiService {
    
    @GET("telecom_plans.php")
    suspend fun getTelecomPlans(): TelecomPlansResponse
    
    @GET("telecom_plans.php")
    suspend fun getTelecomPlansByCarrier(@Query("carrier") carrier: String): TelecomPlansResponse
    
    @GET("telecom_plans.php/{id}")
    suspend fun getTelecomPlanById(@Path("id") id: String): TelecomPlanResponse
} 