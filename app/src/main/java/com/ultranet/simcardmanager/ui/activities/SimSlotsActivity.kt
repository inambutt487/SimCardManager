package com.ultranet.simcardmanager.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultranet.simcardmanager.data.repository.TelephonyRepository
import com.ultranet.simcardmanager.databinding.ActivitySimSlotsBinding
import com.ultranet.simcardmanager.ui.adapters.SimCardInfoAdapter
import com.ultranet.simcardmanager.utils.PermissionUtils

class SimSlotsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySimSlotsBinding
    private lateinit var simCardInfoAdapter: SimCardInfoAdapter
    private lateinit var viewModel: SimCardViewModel
    
    // Permission launcher using ActivityResultContracts
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySimSlotsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        checkPermissionsAndCompatibility()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupViewModel() {
        val telephonyRepository = TelephonyRepository(this)
        viewModel = SimCardViewModel(telephonyRepository)
    }
    
    private fun setupRecyclerView() {
        simCardInfoAdapter = SimCardInfoAdapter(
            onSimCardClick = { simCardInfo ->
                // Handle SIM card click - show detailed slot information
                val message = "Slot ${simCardInfo.slotNumber}: ${simCardInfo.carrierName}\n" +
                             "Network: ${simCardInfo.networkType}\n" +
                             "State: ${simCardInfo.simState}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
        
        binding.recyclerViewSimSlots.apply {
            layoutManager = LinearLayoutManager(this@SimSlotsActivity)
            adapter = simCardInfoAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.simCards.observe(this) { simCards ->
            simCardInfoAdapter.submitList(simCards)
            binding.tvEmptyState.visibility = if (simCards.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.hasPermissions.observe(this) { hasPermissions ->
            if (!hasPermissions) {
                Toast.makeText(this, "Phone state permission required", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun checkPermissionsAndCompatibility() {
        // Check device compatibility first
        val compatibilityInfo = PermissionUtils.getDeviceCompatibilityInfo(this)
        
        if (!compatibilityInfo.isTelephonySupported) {
            showCompatibilityDialog(compatibilityInfo)
            return
        }
        
        if (!compatibilityInfo.isFullyCompatible()) {
            showLimitedCompatibilityDialog(compatibilityInfo)
        }
        
        // Check permissions
        if (PermissionUtils.hasRequiredPermissions(this)) {
            viewModel.refreshSimCards()
        } else {
            handleMissingPermissions()
        }
    }
    
    private fun handleMissingPermissions() {
        val missingPermissions = PermissionUtils.getMissingPermissions(this)
        
        if (PermissionUtils.hasAnyPermissionPermanentlyDenied(this)) {
            // Show settings redirect dialog
            PermissionUtils.showSettingsRedirectDialog(
                activity = this,
                onPositiveClick = {
                    PermissionUtils.openAppSettings(this)
                },
                onNegativeClick = {
                    Toast.makeText(this, "Some features may not work without required permissions", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            // Check if we should show rationale for any permission
            val permissionsNeedingRationale = missingPermissions.filter { permission ->
                PermissionUtils.shouldShowPermissionRationale(this, permission)
            }
            
            if (permissionsNeedingRationale.isNotEmpty()) {
                // Show rationale for the first permission that needs it
                val permission = permissionsNeedingRationale.first()
                PermissionUtils.showPermissionRationaleDialog(
                    activity = this,
                    permission = permission,
                    onPositiveClick = {
                        requestPermissions()
                    },
                    onNegativeClick = {
                        Toast.makeText(this, "Permission required for full functionality", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Request permissions directly
                requestPermissions()
            }
        }
    }
    
    private fun requestPermissions() {
        permissionLauncher.launch(PermissionUtils.REQUIRED_PERMISSIONS)
    }
    
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            viewModel.refreshSimCards()
            Toast.makeText(this, "Permissions granted! Loading SIM slot information...", Toast.LENGTH_SHORT).show()
        } else {
            val deniedPermissions = permissions.filterValues { !it }.keys.toList()
            val deniedPermissionNames = deniedPermissions.joinToString(", ") { permission ->
                PermissionUtils.getPermissionDisplayName(permission)
            }
            
            Toast.makeText(
                this,
                "Some permissions were denied: $deniedPermissionNames. Some features may not work properly.",
                Toast.LENGTH_LONG
            ).show()
            
            // Check if any permissions are permanently denied
            if (PermissionUtils.hasAnyPermissionPermanentlyDenied(this)) {
                showPermanentlyDeniedDialog()
            }
        }
    }
    
    private fun showCompatibilityDialog(compatibilityInfo: PermissionUtils.DeviceCompatibilityInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Device Not Compatible")
            .setMessage(compatibilityInfo.getCompatibilityMessage())
            .setPositiveButton("OK") { _, _ ->
                // Continue with limited functionality
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showLimitedCompatibilityDialog(compatibilityInfo: PermissionUtils.DeviceCompatibilityInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Limited Compatibility")
            .setMessage(compatibilityInfo.getCompatibilityMessage())
            .setNegativeButton("Continue") { _, _ ->
                // Continue with limited functionality
            }
            .setCancelable(true)
            .show()
    }
    
    private fun showPermanentlyDeniedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Some permissions have been permanently denied. To use all features, please enable them in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                PermissionUtils.openAppSettings(this)
            }
            .setNegativeButton("Continue") { _, _ ->
                // Continue with limited functionality
            }
            .setCancelable(true)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 