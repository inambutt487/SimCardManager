package com.ultranet.simcardmanager.data.repository

import com.ultranet.simcardmanager.data.api.TelecomApiService
import com.ultranet.simcardmanager.data.database.TelecomPlanDao
import com.ultranet.simcardmanager.domain.models.ApiResponse
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

/**
 * TelecomRepository - Model Layer (MVVM Architecture)
 * 
 * RESPONSIBILITIES:
 * - Manages telecom plans data operations
 * - Implements offline-first approach with local caching
 * - Handles API communication with error handling
 * - Provides data synchronization between local and remote sources
 * - Manages mock data for testing scenarios
 * 
 * MVVM PATTERN:
 * - MODEL: This Repository is part of the Model layer
 * - VIEWMODEL: TelecomPlansViewModel uses this Repository
 * - VIEW: Views never directly access this Repository
 * 
 * TELECOM CHALLENGES ADDRESSED:
 * - Network connectivity issues and offline scenarios
 * - API rate limiting and timeout handling
 * - Carrier-specific plan data management
 * - Data synchronization between local cache and remote API
 * - Error handling for different network conditions
 * 
 * PERFORMANCE CONSIDERATIONS:
 * - Offline-first approach reduces API calls
 * - Efficient database queries with Room
 * - Flow-based reactive data streams
 * - Background sync with WorkManager
 * 
 * BATTERY OPTIMIZATION:
 * - Minimizes network requests through caching
 * - Efficient database operations
 * - Background sync only when needed
 * - Network constraints in WorkManager
 * 
 * MEMORY MANAGEMENT:
 * - Efficient data structures
 * - No static references
 * - Proper cleanup of resources
 * - Flow-based memory-efficient data streams
 */
class TelecomRepository(
    private val telecomPlanDao: TelecomPlanDao,
    private val telecomApiService: TelecomApiService
) {
    
    /**
     * Get all telecom plans from local database (offline-first approach)
     * Telecom Challenge: Provides immediate data access even without network
     * Performance: Fast local database access
     * Battery: No network calls required for cached data
     */
    fun getAllTelecomPlans(): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getAllTelecomPlans()
    }
    
    /**
     * Get telecom plans by carrier from local database
     * Telecom Challenge: Efficient filtering for carrier-specific plans
     * Performance: Database-level filtering reduces memory usage
     * Battery: No network calls for filtered data
     */
    fun getTelecomPlansByCarrier(carrier: String): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getTelecomPlansByCarrier(carrier)
    }
    
    /**
     * Get top telecom plans with limit (for performance optimization)
     * Telecom Challenge: Efficient pagination for large datasets
     * Performance: Limits data transfer and memory usage
     * Battery: Reduced processing time for limited data
     */
    fun getTopTelecomPlans(limit: Int): Flow<List<TelecomPlan>> {
        return telecomPlanDao.getTopTelecomPlans(limit)
    }
    
    /**
     * Get specific telecom plan by ID
     * Telecom Challenge: Efficient lookup for plan details
     * Performance: Direct database lookup by primary key
     * Battery: Minimal database operation
     */
    suspend fun getTelecomPlanById(id: String): TelecomPlan? {
        return telecomPlanDao.getTelecomPlanById(id)
    }
    
    /**
     * Sync telecom plans from API and update local cache
     * Telecom Challenge: Handles network failures gracefully
     * Performance: Background sync with user feedback
     * Battery: Efficient network usage with proper error handling
     */
    suspend fun syncTelecomPlansFromApi(): ApiResponse<List<TelecomPlan>> {
        return try {
            // Telecom Challenge: API call with timeout handling
            val response = telecomApiService.getTelecomPlans()
            
            // Telecom Challenge: Validate API response
            if (!response.success) {
                return ApiResponse.Error(response.error ?: "API request failed")
            }
            
            val telecomPlans = response.data
            
            if (telecomPlans.isEmpty()) {
                return ApiResponse.Error("No telecom plans available")
            }
            
            // Performance: Batch database operations
            telecomPlanDao.insertTelecomPlans(telecomPlans)
            
            // Telecom Challenge: Clean up old data to prevent storage bloat
            // Performance: Automatic cleanup of outdated data
            val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
            telecomPlanDao.deleteOldTelecomPlans(cutoffTime)
            
            ApiResponse.Success(telecomPlans)
        } catch (e: java.net.UnknownHostException) {
            // Telecom Challenge: Handle no internet connection
            ApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            // Telecom Challenge: Handle slow network connections
            ApiResponse.Error("Request timed out. Please try again.")
        } catch (e: retrofit2.HttpException) {
            // Telecom Challenge: Handle different HTTP error codes
            val errorMessage = when (e.code()) {
                404 -> "Telecom plans not found"
                500 -> "Server error. Please try again later."
                503 -> "Service temporarily unavailable"
                else -> "Network error (${e.code()})"
            }
            ApiResponse.Error(errorMessage)
        } catch (e: Exception) {
            // Telecom Challenge: Handle unexpected errors
            ApiResponse.Error("Failed to sync telecom plans: ${e.message}")
        }
    }
    
    /**
     * Sync telecom plans by carrier from API
     * Telecom Challenge: Carrier-specific API calls
     * Performance: Targeted data fetching
     * Battery: Efficient network usage for specific carriers
     */
    suspend fun syncTelecomPlansByCarrierFromApi(carrier: String): ApiResponse<List<TelecomPlan>> {
        return try {
            // Telecom Challenge: Carrier-specific API call
            val response = telecomApiService.getTelecomPlansByCarrier(carrier)
            
            // Telecom Challenge: Validate carrier-specific response
            if (!response.success) {
                return ApiResponse.Error(response.error ?: "API request failed for carrier")
            }
            
            val telecomPlans = response.data
            
            if (telecomPlans.isEmpty()) {
                return ApiResponse.Error("No plans found for carrier: $carrier")
            }
            
            // Performance: Update local cache with carrier-specific data
            telecomPlanDao.insertTelecomPlans(telecomPlans)
            
            ApiResponse.Success(telecomPlans)
        } catch (e: java.net.UnknownHostException) {
            // Telecom Challenge: Handle network connectivity issues
            ApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            // Telecom Challenge: Handle slow network for carrier API
            ApiResponse.Error("Request timed out. Please try again.")
        } catch (e: retrofit2.HttpException) {
            // Telecom Challenge: Handle carrier-specific API errors
            val errorMessage = when (e.code()) {
                404 -> "No plans found for carrier: $carrier"
                500 -> "Server error. Please try again later."
                503 -> "Service temporarily unavailable"
                else -> "Network error (${e.code()})"
            }
            ApiResponse.Error(errorMessage)
        } catch (e: Exception) {
            // Telecom Challenge: Handle carrier-specific errors
            ApiResponse.Error("Failed to sync telecom plans for carrier: ${e.message}")
        }
    }
    
    /**
     * Insert mock telecom plans for testing and offline scenarios
     * Telecom Challenge: Ensures app functionality without real API data
     * Performance: Fast local data insertion
     * Battery: No network calls required
     */
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
        
        // Performance: Batch insert for efficient database operations
        telecomPlanDao.insertTelecomPlans(mockPlans)
    }
    
    /**
     * Clear all cached telecom plans
     * Telecom Challenge: Memory management for large datasets
     * Performance: Efficient database cleanup
     * Battery: Reduces storage and memory usage
     */
    suspend fun clearAllTelecomPlans() {
        telecomPlanDao.deleteAllTelecomPlans()
    }
} 