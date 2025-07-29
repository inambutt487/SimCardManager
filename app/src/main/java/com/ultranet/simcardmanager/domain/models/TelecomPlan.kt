package com.ultranet.simcardmanager.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

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

// Custom deserializer to handle BigInt and other numeric types
class TelecomPlanDeserializer : JsonDeserializer<TelecomPlan> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TelecomPlan {
        val jsonObject = json?.asJsonObject ?: throw IllegalArgumentException("Invalid JSON")
        
        return TelecomPlan(
            id = getStringValue(jsonObject, "id"),
            name = getStringValue(jsonObject, "name"),
            price = getDoubleValue(jsonObject, "price"),
            data = getStringValue(jsonObject, "data"),
            carrierName = getStringValueOrNull(jsonObject, "carrier_name"),
            planType = getStringValueOrNull(jsonObject, "plan_type"),
            contractLength = getIntValueOrNull(jsonObject, "contract_length"),
            features = getStringValueOrNull(jsonObject, "features"),
            createdAt = getStringValueOrNull(jsonObject, "created_at"),
            updatedAt = getStringValueOrNull(jsonObject, "updated_at")
        )
    }
    
    private fun getStringValue(jsonObject: com.google.gson.JsonObject, key: String): String {
        val element = jsonObject.get(key)
        return when {
            element?.isJsonNull == true -> ""
            element?.isJsonPrimitive == true -> element.asString
            else -> ""
        }
    }
    
    private fun getStringValueOrNull(jsonObject: com.google.gson.JsonObject, key: String): String? {
        val element = jsonObject.get(key)
        return when {
            element?.isJsonNull == true -> null
            element?.isJsonPrimitive == true -> element.asString
            else -> null
        }
    }
    
    private fun getDoubleValue(jsonObject: com.google.gson.JsonObject, key: String): Double {
        val element = jsonObject.get(key)
        return when {
            element?.isJsonNull == true -> 0.0
            element?.isJsonPrimitive == true -> {
                when {
                    element.asJsonPrimitive.isNumber -> element.asDouble
                    element.asJsonPrimitive.isString -> element.asString.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
            else -> 0.0
        }
    }
    
    private fun getIntValueOrNull(jsonObject: com.google.gson.JsonObject, key: String): Int? {
        val element = jsonObject.get(key)
        return when {
            element?.isJsonNull == true -> null
            element?.isJsonPrimitive == true -> {
                when {
                    element.asJsonPrimitive.isNumber -> element.asInt
                    element.asJsonPrimitive.isString -> element.asString.toIntOrNull()
                    else -> null
                }
            }
            else -> null
        }
    }
} 