package com.ultranet.simcardmanager.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.ultranet.simcardmanager.domain.models.SimCardInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelephonyRepository(private val context: Context) {
    
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    fun hasRequiredPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getSimCardInfo(): List<SimCardInfo> = withContext(Dispatchers.IO) {
        if (!hasRequiredPermissions()) {
            return@withContext getMockSimCardInfo()
        }
        
        return@withContext try {
            getRealSimCardInfo()
        } catch (e: Exception) {
            e.printStackTrace()
            getMockSimCardInfo()
        }
    }
    
    private fun getRealSimCardInfo(): List<SimCardInfo> {
        val simCards = mutableListOf<SimCardInfo>()
        
        // For Android 5.0+ (API 22+), we can get multiple SIM cards
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as android.telephony.SubscriptionManager
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val subscriptions = subscriptionManager.activeSubscriptionInfoList
                subscriptions?.forEach { subscriptionInfo ->
                    val simCard = SimCardInfo(
                        slotNumber = subscriptionInfo.simSlotIndex,
                        carrierName = subscriptionInfo.carrierName?.toString(),
                        simState = getSimStateString(telephonyManager.simState),
                        networkType = getNetworkTypeString(telephonyManager.dataNetworkType)
                    )
                    simCards.add(simCard)
                }
            }
        } else {
            // Fallback for older Android versions
            val simCard = SimCardInfo(
                slotNumber = 0,
                carrierName = telephonyManager.networkOperatorName,
                simState = getSimStateString(telephonyManager.simState),
                networkType = getNetworkTypeString(telephonyManager.dataNetworkType)
            )
            simCards.add(simCard)
        }
        
        return if (simCards.isEmpty()) getMockSimCardInfo() else simCards
    }
    
    private fun getMockSimCardInfo(): List<SimCardInfo> {
        return listOf(
            SimCardInfo(
                slotNumber = 0,
                carrierName = "Mock Carrier 1",
                simState = "READY",
                networkType = "4G"
            ),
            SimCardInfo(
                slotNumber = 1,
                carrierName = "Mock Carrier 2",
                simState = "READY",
                networkType = "3G"
            )
        )
    }
    
    private fun getSimStateString(simState: Int): String {
        return when (simState) {
            TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN_REQUIRED"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK_REQUIRED"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "NETWORK_LOCKED"
            TelephonyManager.SIM_STATE_READY -> "READY"
            TelephonyManager.SIM_STATE_NOT_READY -> "NOT_READY"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM_DISABLED"
            TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "CARD_IO_ERROR"
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "CARD_RESTRICTED"
            else -> "UNKNOWN"
        }
    }
    
    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "UNKNOWN"
        }
    }
} 