package com.ultranet.simcardmanager.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ultranet.simcardmanager.domain.models.SimCard

class TelephonyManagerHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "TelephonyManagerHelper"
    }
    
    private val telephonyManager: TelephonyManager? = try {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get TelephonyManager service", e)
        null
    }
    
    /**
     * Check if device supports telephony features
     */
    fun isTelephonySupported(): Boolean {
        return PermissionUtils.isTelephonySupported(context)
    }
    
    /**
     * Get device compatibility information
     */
    fun getDeviceCompatibilityInfo(): PermissionUtils.DeviceCompatibilityInfo {
        return PermissionUtils.getDeviceCompatibilityInfo(context)
    }
    
    fun hasRequiredPermissions(): Boolean {
        return PermissionUtils.hasRequiredPermissions(context)
    }
    
    fun getSimCards(): List<SimCard> {
        if (!hasRequiredPermissions()) {
            Log.w(TAG, "Required permissions not granted")
            return emptyList()
        }
        
        if (!isTelephonySupported()) {
            Log.w(TAG, "Device does not support telephony features")
            return emptyList()
        }
        
        if (telephonyManager == null) {
            Log.e(TAG, "TelephonyManager is null")
            return emptyList()
        }
        
        val simCards = mutableListOf<SimCard>()
        
        try {
            // For Android 5.0+ (API 22+), we can get multiple SIM cards
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                getSimCardsModern(simCards)
            } else {
                // Fallback for older Android versions
                getSimCardsLegacy(simCards)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception while accessing SIM cards", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while getting SIM cards", e)
        }
        
        return simCards
    }
    
    private fun getSimCardsModern(simCards: MutableList<SimCard>) {
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as android.telephony.SubscriptionManager
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val subscriptions = subscriptionManager.activeSubscriptionInfoList
                subscriptions?.forEach { subscriptionInfo ->
                    try {
                        val simCard = SimCard(
                            slotNumber = subscriptionInfo.simSlotIndex,
                            carrierName = subscriptionInfo.carrierName?.toString() ?: "Unknown",
                            simState = getSimStateString(subscriptionInfo.simSlotIndex),
                            networkType = getNetworkTypeString(),
                            iccid = subscriptionInfo.iccId ?: "",
                            imsi = getImsiSafely(subscriptionInfo.simSlotIndex),
                            phoneNumber = subscriptionInfo.number ?: "",
                            countryCode = subscriptionInfo.countryIso ?: "",
                            isActive = subscriptionInfo.isOpportunistic == false,
                            createdAt = null,
                            updatedAt = null
                        )
                        simCards.add(simCard)
                        Log.d(TAG, "Added SIM card for slot ${subscriptionInfo.simSlotIndex}: ${simCard.carrierName}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing subscription info for slot ${subscriptionInfo.simSlotIndex}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing SubscriptionManager", e)
        }
    }
    
    private fun getSimCardsLegacy(simCards: MutableList<SimCard>) {
        try {
            val simCard = SimCard(
                slotNumber = 0,
                carrierName = telephonyManager?.networkOperatorName ?: "Unknown",
                simState = getSimStateString(0),
                networkType = getNetworkTypeString(),
                iccid = getIccidSafely(),
                imsi = getImsiSafely(0),
                phoneNumber = getPhoneNumberSafely(),
                countryCode = telephonyManager?.networkCountryIso ?: "",
                isActive = true,
                createdAt = null,
                updatedAt = null
            )
            simCards.add(simCard)
            Log.d(TAG, "Added legacy SIM card: ${simCard.carrierName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating legacy SIM card", e)
        }
    }
    
    fun getActiveSimCard(): SimCard? {
        return getSimCards().find { it.isActive }
    }
    
    fun getSimCardBySlot(slotNumber: Int): SimCard? {
        return getSimCards().find { it.slotNumber == slotNumber }
    }
    
    /**
     * Get SIM state string safely
     */
    private fun getSimStateString(slotIndex: Int): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when (telephonyManager?.createForSubscriptionId(slotIndex)?.simState) {
                    TelephonyManager.SIM_STATE_READY -> "READY"
                    TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
                    TelephonyManager.SIM_STATE_PIN_REQUIRED -> "LOCKED"
                    TelephonyManager.SIM_STATE_PUK_REQUIRED -> "LOCKED"
                    TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "LOCKED"
                    TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN"
                    else -> "UNKNOWN"
                }
            } else {
                when (telephonyManager?.simState) {
                    TelephonyManager.SIM_STATE_READY -> "READY"
                    TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
                    TelephonyManager.SIM_STATE_PIN_REQUIRED -> "LOCKED"
                    TelephonyManager.SIM_STATE_PUK_REQUIRED -> "LOCKED"
                    TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "LOCKED"
                    TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN"
                    else -> "UNKNOWN"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SIM state for slot $slotIndex", e)
            "UNKNOWN"
        }
    }
    
    /**
     * Get network type string safely
     */
    private fun getNetworkTypeString(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (telephonyManager?.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSDPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
                    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
                    else -> "UNKNOWN"
                }
            } else {
                when (telephonyManager?.networkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                    TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSDPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
                    TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
                    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO"
                    TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
                    else -> "UNKNOWN"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network type", e)
            "UNKNOWN"
        }
    }
    
    /**
     * Get ICCID safely
     */
    private fun getIccidSafely(): String {
        return try {
            telephonyManager?.simSerialNumber ?: ""
        } catch (e: SecurityException) {
            Log.w(TAG, "Security exception getting ICCID", e)
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ICCID", e)
            ""
        }
    }
    
    /**
     * Get IMSI safely
     */
    private fun getImsiSafely(slotIndex: Int): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                telephonyManager?.createForSubscriptionId(slotIndex)?.subscriberId ?: ""
            } else {
                telephonyManager?.subscriberId ?: ""
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Security exception getting IMSI for slot $slotIndex", e)
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IMSI for slot $slotIndex", e)
            ""
        }
    }
    
    /**
     * Get phone number safely
     */
    private fun getPhoneNumberSafely(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager?.line1Number ?: ""
                } else {
                    ""
                }
            } else {
                telephonyManager?.line1Number ?: ""
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Security exception getting phone number", e)
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone number", e)
            ""
        }
    }
} 