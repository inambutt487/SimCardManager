package com.ultranet.simcardmanager.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.SimSwitchRepository
import java.util.concurrent.TimeUnit

class BalanceSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "BalanceSyncWorker"
        private const val KEY_SIM_SLOT = "sim_slot"
        private const val KEY_CARRIER_NAME = "carrier_name"
        private const val KEY_SWITCH_EVENT_ID = "switch_event_id"
        
        fun createWorkRequest(
            simSlot: Int,
            carrierName: String,
            switchEventId: Long
        ): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putInt(KEY_SIM_SLOT, simSlot)
                .putString(KEY_CARRIER_NAME, carrierName)
                .putLong(KEY_SWITCH_EVENT_ID, switchEventId)
                .build()
            
            return OneTimeWorkRequestBuilder<BalanceSyncWorker>()
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("balance_sync")
                .build()
        }
    }
    
    override suspend fun doWork(): Result {
        val simSlot = inputData.getInt(KEY_SIM_SLOT, -1)
        val carrierName = inputData.getString(KEY_CARRIER_NAME) ?: "Unknown"
        val switchEventId = inputData.getLong(KEY_SWITCH_EVENT_ID, -1)
        
        Log.d(TAG, "Starting balance sync for SIM slot $simSlot, carrier: $carrierName")
        
        return try {
            // Simulate balance sync process
            simulateBalanceSync(simSlot, carrierName, switchEventId)
            
            Log.d(TAG, "Balance sync completed successfully for SIM slot $simSlot")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Balance sync failed for SIM slot $simSlot", e)
            
            // Update the switch event to mark it as failed
            updateSwitchEventStatus(switchEventId, false)
            
            // Retry if it's a network-related error
            if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun simulateBalanceSync(simSlot: Int, carrierName: String, switchEventId: Long) {
        // Simulate API call delay
        kotlinx.coroutines.delay(2000)
        
        // Simulate different scenarios based on carrier
        when {
            carrierName.contains("AT&T", ignoreCase = true) -> {
                Log.d(TAG, "Syncing AT&T balance for slot $simSlot")
                // Simulate AT&T specific sync logic
                kotlinx.coroutines.delay(1500)
            }
            carrierName.contains("Verizon", ignoreCase = true) -> {
                Log.d(TAG, "Syncing Verizon balance for slot $simSlot")
                // Simulate Verizon specific sync logic
                kotlinx.coroutines.delay(1800)
            }
            carrierName.contains("T-Mobile", ignoreCase = true) -> {
                Log.d(TAG, "Syncing T-Mobile balance for slot $simSlot")
                // Simulate T-Mobile specific sync logic
                kotlinx.coroutines.delay(1200)
            }
            else -> {
                Log.d(TAG, "Syncing generic carrier balance for slot $simSlot")
                // Simulate generic sync logic
                kotlinx.coroutines.delay(1000)
            }
        }
        
        // Simulate random failure (10% chance for testing)
        if (Math.random() < 0.1) {
            throw RuntimeException("Simulated balance sync failure")
        }
        
        // Update the switch event to mark it as successful
        updateSwitchEventStatus(switchEventId, true)
        
        Log.d(TAG, "Balance sync simulation completed for slot $simSlot")
    }
    
    private suspend fun updateSwitchEventStatus(switchEventId: Long, isSuccessful: Boolean) {
        if (switchEventId > 0) {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = SimSwitchRepository(database.simSwitchDao())
                
                val event = repository.getSimSwitchEventById(switchEventId)
                event?.let {
                    val updatedEvent = it.copy(isSuccessful = isSuccessful)
                    repository.updateSimSwitchEvent(updatedEvent)
                    Log.d(TAG, "Updated switch event $switchEventId success status to $isSuccessful")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update switch event status", e)
            }
        }
    }
} 