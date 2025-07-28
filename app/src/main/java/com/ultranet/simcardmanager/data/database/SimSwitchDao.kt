package com.ultranet.simcardmanager.data.database

import androidx.room.*
import com.ultranet.simcardmanager.domain.models.SimSwitchEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SimSwitchDao {
    
    @Insert
    suspend fun insertSimSwitchEvent(simSwitchEvent: SimSwitchEvent): Long
    
    @Query("SELECT * FROM sim_switch_events ORDER BY timestamp DESC")
    fun getAllSimSwitchEvents(): Flow<List<SimSwitchEvent>>
    
    @Query("SELECT * FROM sim_switch_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSimSwitchEventsSince(startTime: Long): Flow<List<SimSwitchEvent>>
    
    @Query("SELECT * FROM sim_switch_events WHERE oldSimSlot = :slotNumber OR newSimSlot = :slotNumber ORDER BY timestamp DESC")
    fun getSimSwitchEventsForSlot(slotNumber: Int): Flow<List<SimSwitchEvent>>
    
    @Query("SELECT * FROM sim_switch_events WHERE id = :eventId")
    suspend fun getSimSwitchEventById(eventId: Long): SimSwitchEvent?
    
    @Query("SELECT COUNT(*) FROM sim_switch_events WHERE timestamp >= :startTime")
    suspend fun getSimSwitchCountSince(startTime: Long): Int
    
    @Query("SELECT * FROM sim_switch_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSimSwitchEvent(): SimSwitchEvent?
    
    @Update
    suspend fun updateSimSwitchEvent(simSwitchEvent: SimSwitchEvent)
    
    @Delete
    suspend fun deleteSimSwitchEvent(simSwitchEvent: SimSwitchEvent)
    
    @Query("DELETE FROM sim_switch_events WHERE timestamp < :timestamp")
    suspend fun deleteSimSwitchEventsOlderThan(timestamp: Long)
    
    @Query("DELETE FROM sim_switch_events")
    suspend fun deleteAllSimSwitchEvents()
} 