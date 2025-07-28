package com.ultranet.simcardmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ultranet.simcardmanager.domain.models.SimCard
import com.ultranet.simcardmanager.domain.models.SimSwitchEvent
import com.ultranet.simcardmanager.domain.models.TelecomPlan

@Database(
    entities = [SimCard::class, TelecomPlan::class, SimSwitchEvent::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun simCardDao(): SimCardDao
    abstract fun telecomPlanDao(): TelecomPlanDao
    abstract fun simSwitchDao(): SimSwitchDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simcard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 