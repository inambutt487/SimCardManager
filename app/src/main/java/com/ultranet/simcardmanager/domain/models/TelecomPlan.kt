package com.ultranet.simcardmanager.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "telecom_plans")
data class TelecomPlan(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val data: String, // e.g., "5GB", "Unlimited"
    @SerializedName("carrier_name")
    val carrierName: String?,
    @SerializedName("plan_type")
    val planType: String?,
    @SerializedName("contract_length")
    val contractLength: Int?,
    val features: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
) {
    // Custom constructor to handle string-to-number conversions from API
    constructor(
        id: String,
        name: String,
        price: String,
        data: String,
        carrierName: String?,
        planType: String?,
        contractLength: String?,
        features: String?,
        createdAt: String?,
        updatedAt: String?
    ) : this(
        id = id,
        name = name,
        price = price.toDoubleOrNull() ?: 0.0,
        data = data,
        carrierName = carrierName,
        planType = planType,
        contractLength = contractLength?.toIntOrNull(),
        features = features,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 