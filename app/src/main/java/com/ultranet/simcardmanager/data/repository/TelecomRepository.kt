package com.ultranet.simcardmanager.data.repository

import com.ultranet.simcardmanager.data.api.TelecomApiService
import com.ultranet.simcardmanager.data.database.TelecomPlanDao
import com.ultranet.simcardmanager.domain.models.ApiResponse
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class TelecomRepository(
    private val telecomPlanDao: TelecomPlanDao,
    private val telecomApiService: TelecomApiService
) {
    
    // Offline-first approach: Always return cached data first
    fun getAllTelecomPlans(): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getAllTelecomPlans()
    }
    
    fun getTelecomPlansByCarrier(carrier: String): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getTelecomPlansByCarrier(carrier)
    }
    
    fun getTopTelecomPlans(limit: Int): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getTopTelecomPlans(limit)
    }
    
    suspend fun getTelecomPlanById(id: String): TelecomPlan? {
        return telecomPlanDao.getTelecomPlanById(id)
    }
    
    // Sync data from API and update local cache
    suspend fun syncTelecomPlansFromApi(): ApiResponse<List<TelecomPlan>> {
        return try {
            val telecomPlans = telecomApiService.getTelecomPlans()
            
            // Check for empty response
            if (telecomPlans.isEmpty()) {
                return ApiResponse.Error("No telecom plans available")
            }
            
            // Update local cache with fresh data
            telecomPlanDao.insertTelecomPlans(telecomPlans)
            
            // Clean up old data (older than 24 hours)
            val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
            telecomPlanDao.deleteOldTelecomPlans(cutoffTime)
            
            ApiResponse.Success(telecomPlans)
        } catch (e: java.net.UnknownHostException) {
            ApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResponse.Error("Request timed out. Please try again.")
        } catch (e: retrofit2.HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "Telecom plans not found"
                500 -> "Server error. Please try again later."
                503 -> "Service temporarily unavailable"
                else -> "Network error (${e.code()})"
            }
            ApiResponse.Error(errorMessage)
        } catch (e: Exception) {
            ApiResponse.Error("Failed to sync telecom plans: ${e.message}")
        }
    }
    
    suspend fun syncTelecomPlansByCarrierFromApi(carrier: String): ApiResponse<List<TelecomPlan>> {
        return try {
            val telecomPlans = telecomApiService.getTelecomPlansByCarrier(carrier)
            
            // Check for empty response
            if (telecomPlans.isEmpty()) {
                return ApiResponse.Error("No plans found for carrier: $carrier")
            }
            
            // Update local cache
            telecomPlanDao.insertTelecomPlans(telecomPlans)
            
            ApiResponse.Success(telecomPlans)
        } catch (e: java.net.UnknownHostException) {
            ApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResponse.Error("Request timed out. Please try again.")
        } catch (e: retrofit2.HttpException) {
            val errorMessage = when (e.code()) {
                404 -> "No plans found for carrier: $carrier"
                500 -> "Server error. Please try again later."
                503 -> "Service temporarily unavailable"
                else -> "Network error (${e.code()})"
            }
            ApiResponse.Error(errorMessage)
        } catch (e: Exception) {
            ApiResponse.Error("Failed to sync telecom plans for carrier: ${e.message}")
        }
    }
    
    // Insert mock data for testing
    suspend fun insertMockTelecomPlans() {
        val mockPlans = listOf(
            TelecomPlan(
                id = "plan_1",
                name = "Basic Plan",
                price = 29.99,
                data = "5GB",
                carrierName = "Verizon Wireless",
                planType = "POSTPAID",
                contractLength = 24,
                features = "Unlimited talk, Unlimited text, 5GB data",
                createdAt = null,
                updatedAt = null
            ),
            TelecomPlan(
                id = "plan_2",
                name = "Standard Plan",
                price = 49.99,
                data = "15GB",
                carrierName = "Verizon Wireless",
                planType = "POSTPAID",
                contractLength = 24,
                features = "Unlimited talk, Unlimited text, 15GB data",
                createdAt = null,
                updatedAt = null
            ),
            TelecomPlan(
                id = "plan_3",
                name = "Premium Plan",
                price = 79.99,
                data = "Unlimited",
                carrierName = "Verizon Wireless",
                planType = "POSTPAID",
                contractLength = 24,
                features = "Unlimited talk, Unlimited text, Unlimited data",
                createdAt = null,
                updatedAt = null
            ),
            TelecomPlan(
                id = "plan_4",
                name = "Student Plan",
                price = 19.99,
                data = "3GB",
                carrierName = "AT&T Mobility",
                planType = "POSTPAID",
                contractLength = 12,
                features = "Unlimited talk, Unlimited text, 3GB data",
                createdAt = null,
                updatedAt = null
            ),
            TelecomPlan(
                id = "plan_5",
                name = "Family Plan",
                price = 99.99,
                data = "50GB",
                carrierName = "AT&T Mobility",
                planType = "POSTPAID",
                contractLength = 24,
                features = "Unlimited talk, Unlimited text, 50GB data",
                createdAt = null,
                updatedAt = null
            )
        )
        
        telecomPlanDao.insertTelecomPlans(mockPlans)
    }
    
    // Clear all cached data
    suspend fun clearAllTelecomPlans() {
        telecomPlanDao.deleteAllTelecomPlans()
    }
} 