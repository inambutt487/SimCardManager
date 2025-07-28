package com.ultranet.simcardmanager.data.database

import androidx.room.*
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface TelecomPlanDao {
    
    @Query("SELECT * FROM telecom_plans ORDER BY price ASC")
    fun getAllTelecomPlans(): Flow<List<TelecomPlan>>
    
    @Query("SELECT * FROM telecom_plans WHERE id = :id")
    suspend fun getTelecomPlanById(id: String): TelecomPlan?
    
    @Query("SELECT * FROM telecom_plans WHERE name LIKE '%' || :carrier || '%' ORDER BY price ASC")
    fun getTelecomPlansByCarrier(carrier: String): Flow<List<TelecomPlan>>
    
    @Query("SELECT * FROM telecom_plans ORDER BY price ASC LIMIT :limit")
    fun getTopTelecomPlans(limit: Int): Flow<List<TelecomPlan>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelecomPlan(telecomPlan: TelecomPlan)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelecomPlans(telecomPlans: List<TelecomPlan>)
    
    @Update
    suspend fun updateTelecomPlan(telecomPlan: TelecomPlan)
    
    @Delete
    suspend fun deleteTelecomPlan(telecomPlan: TelecomPlan)
    
    @Query("DELETE FROM telecom_plans")
    suspend fun deleteAllTelecomPlans()
    
    @Query("DELETE FROM telecom_plans WHERE updatedAt < :timestamp")
    suspend fun deleteOldTelecomPlans(timestamp: Long)
} 