package com.ultranet.simcardmanager.data.api

import com.ultranet.simcardmanager.domain.models.TelecomPlan
import retrofit2.http.GET
import retrofit2.http.Query

interface TelecomApiService {
    
    @GET("telecom_plans.php")
    suspend fun getTelecomPlans(): List<TelecomPlan>
    
    @GET("telecom_plans.php")
    suspend fun getTelecomPlansByCarrier(@Query("carrier") carrier: String): List<TelecomPlan>
    
    @GET("telecom_plans.php/{id}")
    suspend fun getTelecomPlanById(@retrofit2.http.Path("id") id: String): TelecomPlan
} 