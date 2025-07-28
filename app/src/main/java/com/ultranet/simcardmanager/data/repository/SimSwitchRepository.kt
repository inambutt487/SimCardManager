package com.ultranet.simcardmanager.data.repository

import com.ultranet.simcardmanager.data.database.SimSwitchDao
import com.ultranet.simcardmanager.domain.models.SimSwitchEvent
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class SimSwitchRepository(
    private val simSwitchDao: SimSwitchDao
) {
    
    suspend fun logSimSwitch(
        oldSim: String,
        newSim: String,
        oldSimSlot: Int,
        newSimSlot: Int,
        switchReason: String? = null,
        isSuccessful: Boolean = true
    ): Long {
        val simSwitchEvent = SimSwitchEvent(
            timestamp = System.currentTimeMillis(),
            oldSim = oldSim,
            newSim = newSim,
            oldSimSlot = oldSimSlot,
            newSimSlot = newSimSlot,
            switchReason = switchReason,
            isSuccessful = isSuccessful
        )
        
        return simSwitchDao.insertSimSwitchEvent(simSwitchEvent)
    }
    
    fun getAllSimSwitchEvents(): Flow<List<SimSwitchEvent>> {
        return simSwitchDao.getAllSimSwitchEvents()
    }
    
    fun getSimSwitchEventsSince(startTime: Long): Flow<List<SimSwitchEvent>> {
        return simSwitchDao.getSimSwitchEventsSince(startTime)
    }
    
    fun getSimSwitchEventsForSlot(slotNumber: Int): Flow<List<SimSwitchEvent>> {
        return simSwitchDao.getSimSwitchEventsForSlot(slotNumber)
    }
    
    suspend fun getSimSwitchEventById(eventId: Long): SimSwitchEvent? {
        return simSwitchDao.getSimSwitchEventById(eventId)
    }
    
    suspend fun getSimSwitchCountSince(startTime: Long): Int {
        return simSwitchDao.getSimSwitchCountSince(startTime)
    }
    
    suspend fun getLatestSimSwitchEvent(): SimSwitchEvent? {
        return simSwitchDao.getLatestSimSwitchEvent()
    }
    
    suspend fun getSimSwitchCountInLast24Hours(): Int {
        val twentyFourHoursAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        return simSwitchDao.getSimSwitchCountSince(twentyFourHoursAgo)
    }
    
    suspend fun getSimSwitchCountInLastWeek(): Int {
        val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return simSwitchDao.getSimSwitchCountSince(oneWeekAgo)
    }
    
    suspend fun updateSimSwitchEvent(simSwitchEvent: SimSwitchEvent) {
        simSwitchDao.updateSimSwitchEvent(simSwitchEvent)
    }
    
    suspend fun deleteSimSwitchEvent(simSwitchEvent: SimSwitchEvent) {
        simSwitchDao.deleteSimSwitchEvent(simSwitchEvent)
    }
    
    suspend fun deleteSimSwitchEventsOlderThan(timestamp: Long) {
        simSwitchDao.deleteSimSwitchEventsOlderThan(timestamp)
    }
    
    suspend fun deleteAllSimSwitchEvents() {
        simSwitchDao.deleteAllSimSwitchEvents()
    }
    
    suspend fun cleanupOldEvents(keepDays: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays.toLong())
        simSwitchDao.deleteSimSwitchEventsOlderThan(cutoffTime)
    }
} 