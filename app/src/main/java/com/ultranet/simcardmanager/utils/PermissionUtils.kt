package com.ultranet.simcardmanager.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtils {
    
    private const val READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE
    private const val READ_PHONE_NUMBERS_PERMISSION = Manifest.permission.READ_PHONE_NUMBERS
    
    val REQUIRED_PERMISSIONS = arrayOf(
        READ_PHONE_STATE_PERMISSION,
        READ_PHONE_NUMBERS_PERMISSION
    )
    
    /**
     * Check if all required permissions are granted
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if a specific permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if phone state permission is granted
     */
    fun hasPhoneStatePermission(context: Context): Boolean {
        return hasPermission(context, READ_PHONE_STATE_PERMISSION)
    }
    
    /**
     * Check if phone numbers permission is granted
     */
    fun hasPhoneNumbersPermission(context: Context): Boolean {
        return hasPermission(context, READ_PHONE_NUMBERS_PERMISSION)
    }
    
    /**
     * Check if permission should show rationale
     */
    fun shouldShowPermissionRationale(activity: FragmentActivity, permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * Check if permission is permanently denied (user selected "Don't ask again")
     */
    fun isPermissionPermanentlyDenied(activity: FragmentActivity, permission: String): Boolean {
        return !hasPermission(activity, permission) && 
               !shouldShowPermissionRationale(activity, permission)
    }
    
    /**
     * Check if any required permission is permanently denied
     */
    fun hasAnyPermissionPermanentlyDenied(activity: FragmentActivity): Boolean {
        return REQUIRED_PERMISSIONS.any { isPermissionPermanentlyDenied(activity, it) }
    }
    
    /**
     * Get permission display name for user-friendly messages
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            READ_PHONE_STATE_PERMISSION -> "Phone State"
            READ_PHONE_NUMBERS_PERMISSION -> "Phone Numbers"
            else -> "Unknown Permission"
        }
    }
    
    /**
     * Get permission rationale message
     */
    fun getPermissionRationaleMessage(permission: String): String {
        return when (permission) {
            READ_PHONE_STATE_PERMISSION -> 
                "Phone State permission is required to access SIM card information, including carrier details and network status."
            READ_PHONE_NUMBERS_PERMISSION -> 
                "Phone Numbers permission is required to read phone numbers associated with SIM cards."
            else -> "This permission is required for the app to function properly."
        }
    }
    
    /**
     * Show permission rationale dialog
     */
    fun showPermissionRationaleDialog(
        activity: FragmentActivity,
        permission: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage(getPermissionRationaleMessage(permission))
            .setPositiveButton("Grant Permission") { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton("Cancel") { _, _ ->
                onNegativeClick()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Show settings redirect dialog for permanently denied permissions
     */
    fun showSettingsRedirectDialog(
        activity: FragmentActivity,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage("Some permissions have been permanently denied. Please enable them in Settings to use all features of this app.")
            .setPositiveButton("Open Settings") { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton("Cancel") { _, _ ->
                onNegativeClick()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Open app settings
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
    
    /**
     * Get missing permissions list
     */
    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { !hasPermission(context, it) }
    }
    
    /**
     * Check if device supports telephony features
     */
    fun isTelephonySupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
    
    /**
     * Check if device supports multiple SIM cards
     */
    fun isMultiSimSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION)
        } else {
            // For older devices, assume single SIM
            false
        }
    }
    
    /**
     * Get device compatibility information
     */
    fun getDeviceCompatibilityInfo(context: Context): DeviceCompatibilityInfo {
        return DeviceCompatibilityInfo(
            isTelephonySupported = isTelephonySupported(context),
            isMultiSimSupported = isMultiSimSupported(context),
            androidVersion = Build.VERSION.SDK_INT,
            deviceManufacturer = Build.MANUFACTURER,
            deviceModel = Build.MODEL
        )
    }
    
    /**
     * Data class for device compatibility information
     */
    data class DeviceCompatibilityInfo(
        val isTelephonySupported: Boolean,
        val isMultiSimSupported: Boolean,
        val androidVersion: Int,
        val deviceManufacturer: String,
        val deviceModel: String
    ) {
        fun isFullyCompatible(): Boolean {
            return isTelephonySupported && androidVersion >= Build.VERSION_CODES.M
        }
        
        fun getCompatibilityMessage(): String {
            return when {
                !isTelephonySupported -> "This device does not support telephony features."
                androidVersion < Build.VERSION_CODES.M -> "This device runs an older Android version that may have limited SIM card support."
                !isMultiSimSupported -> "This device supports single SIM card only."
                else -> "This device is fully compatible with all features."
            }
        }
    }
} 