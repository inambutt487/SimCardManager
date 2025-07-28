package com.ultranet.simcardmanager.data.database

import androidx.room.*
import com.ultranet.simcardmanager.domain.models.SimCard
import kotlinx.coroutines.flow.Flow

@Dao
interface SimCardDao {
    
    @Query("SELECT * FROM simcards ORDER BY createdAt DESC")
    fun getAllSimCards(): Flow<List<SimCard>>
    
    @Query("SELECT * FROM simcards WHERE id = :id")
    suspend fun getSimCardById(id: Long): SimCard?

    @Query("SELECT * FROM simcards WHERE slotNumber = :slotNumber")
    suspend fun getSimCardBySlot(slotNumber: Int): SimCard?
    
    @Query("SELECT * FROM simcards WHERE isActive = 1")
    fun getActiveSimCards(): Flow<List<SimCard>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimCard(simCard: SimCard): Long
    
    @Update
    suspend fun updateSimCard(simCard: SimCard)
    
    @Delete
    suspend fun deleteSimCard(simCard: SimCard)
    
    @Query("DELETE FROM simcards WHERE id = :id")
    suspend fun deleteSimCardById(id: Long)
    
    @Query("UPDATE simcards SET isActive = 0")
    suspend fun deactivateAllSimCards()
    
    @Query("UPDATE simcards SET isActive = 1 WHERE id = :id")
    suspend fun activateSimCard(id: Long)
} 