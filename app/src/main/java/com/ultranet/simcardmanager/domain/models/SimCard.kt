package com.ultranet.simcardmanager.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "simcards")
data class SimCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @SerializedName("slot_number")
    val slotNumber: Int,
    @SerializedName("carrier_name")
    val carrierName: String?,
    @SerializedName("sim_state")
    val simState: String,
    @SerializedName("network_type")
    val networkType: String?,
    val iccid: String?,
    val imsi: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("is_active")
    val isActive: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
) {
    // Custom constructor to handle string-to-number conversions from API
    constructor(
        id: String,
        slotNumber: String,
        carrierName: String?,
        simState: String,
        networkType: String?,
        iccid: String?,
        imsi: String?,
        phoneNumber: String?,
        countryCode: String?,
        isActive: String,
        createdAt: String?,
        updatedAt: String?
    ) : this(
        id = id.toLongOrNull() ?: 0L,
        slotNumber = slotNumber.toIntOrNull() ?: 0,
        carrierName = carrierName,
        simState = simState,
        networkType = networkType,
        iccid = iccid,
        imsi = imsi,
        phoneNumber = phoneNumber,
        countryCode = countryCode,
        isActive = isActive == "1",
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 