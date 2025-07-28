package com.ultranet.simcardmanager.data.repository

import com.ultranet.simcardmanager.data.api.ApiService
import com.ultranet.simcardmanager.data.database.SimCardDao
import com.ultranet.simcardmanager.domain.models.ApiResponse
import com.ultranet.simcardmanager.domain.models.SimCard
import kotlinx.coroutines.flow.Flow

class SimCardRepository(
    private val simCardDao: SimCardDao,
    private val apiService: ApiService
) {
    
    fun getAllSimCards(): Flow<List<SimCard>> {
        return simCardDao.getAllSimCards()
    }
    
    fun getActiveSimCards(): Flow<List<SimCard>> {
        return simCardDao.getActiveSimCards()
    }
    
    suspend fun getSimCardById(id: Long): SimCard? {
        return simCardDao.getSimCardById(id)
    }
    
    suspend fun getSimCardBySlot(slotNumber: Int): SimCard? {
        return simCardDao.getSimCardBySlot(slotNumber)
    }
    
    suspend fun insertSimCard(simCard: SimCard): Long {
        return simCardDao.insertSimCard(simCard)
    }
    
    suspend fun updateSimCard(simCard: SimCard) {
        simCardDao.updateSimCard(simCard)
    }
    
    suspend fun deleteSimCard(simCard: SimCard) {
        simCardDao.deleteSimCard(simCard)
    }
    
    suspend fun activateSimCard(id: Long) {
        simCardDao.deactivateAllSimCards()
        simCardDao.activateSimCard(id)
    }
    
    // API operations
    suspend fun fetchSimCardsFromApi(): ApiResponse<List<SimCard>> {
        return try {
            val simCards = apiService.getSimCards()
            // Save to local database
            simCards.forEach { simCard ->
                simCardDao.insertSimCard(simCard)
            }
            ApiResponse.Success(simCards)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun syncSimCardWithApi(simCard: SimCard): ApiResponse<SimCard> {
        return try {
            val updatedSimCard = apiService.createSimCard(simCard)
            simCardDao.insertSimCard(updatedSimCard)
            ApiResponse.Success(updatedSimCard)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to sync with API")
        }
    }
} 