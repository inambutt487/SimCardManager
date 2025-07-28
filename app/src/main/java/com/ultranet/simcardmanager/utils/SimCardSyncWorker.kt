package com.ultranet.simcardmanager.utils

import android.content.Context
import androidx.work.*
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.SimCardRepository
import com.ultranet.simcardmanager.data.api.RetrofitClient
import java.util.concurrent.TimeUnit

class SimCardSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val repository = SimCardRepository(
        AppDatabase.getDatabase(context).simCardDao(),
        RetrofitClient.apiService
    )
    
    override suspend fun doWork(): Result {
        return try {
            // Sync SIM cards from device
            val telephonyHelper = TelephonyManagerHelper(applicationContext)
            val deviceSimCards = telephonyHelper.getSimCards()
            
            // Save to local database
            deviceSimCards.forEach { simCard ->
                repository.insertSimCard(simCard)
            }
            
            // Try to sync with API
            repository.fetchSimCardsFromApi()
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        private const val WORK_NAME = "sim_card_sync_worker"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SimCardSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
        
        fun scheduleOneTimeSync(context: Context) {
            val syncRequest = OneTimeWorkRequestBuilder<SimCardSyncWorker>()
                .build()
            
            WorkManager.getInstance(context)
                .enqueue(syncRequest)
        }
    }
} 