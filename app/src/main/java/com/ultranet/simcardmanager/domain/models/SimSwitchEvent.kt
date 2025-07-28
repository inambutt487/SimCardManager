package com.ultranet.simcardmanager.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_switch_events")
data class SimSwitchEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val oldSim: String,
    val newSim: String,
    val oldSimSlot: Int,
    val newSimSlot: Int,
    val switchReason: String? = null,
    val isSuccessful: Boolean = true
) 