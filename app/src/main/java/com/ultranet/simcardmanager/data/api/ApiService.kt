package com.ultranet.simcardmanager.data.api

import com.ultranet.simcardmanager.domain.models.SimCard
import retrofit2.http.*

interface ApiService {
    
    @GET("simcards.php")
    suspend fun getSimCards(): List<SimCard>
    
    @GET("simcards.php/{id}")
    suspend fun getSimCardById(@Path("id") id: Long): SimCard
    
    @POST("simcards.php")
    suspend fun createSimCard(@Body simCard: SimCard): SimCard
    
    @PUT("simcards.php/{id}")
    suspend fun updateSimCard(@Path("id") id: Long, @Body simCard: SimCard): SimCard
    
    @DELETE("simcards.php/{id}")
    suspend fun deleteSimCard(@Path("id") id: Long)
} 