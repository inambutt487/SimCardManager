package com.ultranet.simcardmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import androidx.work.WorkManager
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.SimSwitchRepository
import com.ultranet.simcardmanager.data.repository.TelephonyRepository
import com.ultranet.simcardmanager.databinding.ActivityMainBinding
import com.ultranet.simcardmanager.domain.models.SimCardInfo
import com.ultranet.simcardmanager.ui.activities.SimCardViewModel
import com.ultranet.simcardmanager.ui.activities.SimSlotsActivity
import com.ultranet.simcardmanager.ui.activities.TelecomPlansActivity
import com.ultranet.simcardmanager.ui.adapters.SimCardInfoAdapter
import com.ultranet.simcardmanager.ui.fragments.SimSwitchDialogFragment
import com.ultranet.simcardmanager.utils.BalanceSyncWorker
import com.ultranet.simcardmanager.utils.PermissionUtils

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var simCardInfoAdapter: SimCardInfoAdapter
    private lateinit var viewModel: SimCardViewModel
    private lateinit var simSwitchRepository: SimSwitchRepository
    private lateinit var workManager: WorkManager
    
    // Permission launcher using ActivityResultContracts
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupNavigationButtons()
        setupSimSwitchComponents()
        checkPermissionsAndCompatibility()
    }
    
    private fun setupViewModel() {
        val telephonyRepository = TelephonyRepository(this)
        viewModel = SimCardViewModel(telephonyRepository)
    }
    
    private fun setupRecyclerView() {
        simCardInfoAdapter = SimCardInfoAdapter(
            onSimCardClick = { simCardInfo ->
                // Handle SIM card click
                Toast.makeText(this, "Selected: ${simCardInfo.carrierName}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewSimCards.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = simCardInfoAdapter
        }
    }
    
    private fun setupNavigationButtons() {
        // Telecom Plans Button
        binding.btnTelecomPlans.setOnClickListener {
            val intent = Intent(this, TelecomPlansActivity::class.java)
            startActivity(intent)
        }
        
        // SIM Slots Button
//        binding.btnSimSlots.setOnClickListener {
//            val intent = Intent(this, SimSlotsActivity::class.java)
//            startActivity(intent)
//        }
        
        // Switch SIM Button
        binding.btnSwitchSim.setOnClickListener {
            showSimSwitchDialog()
        }
    }
    
    private fun setupSimSwitchComponents() {
        val database = AppDatabase.getDatabase(this)
        simSwitchRepository = SimSwitchRepository(database.simSwitchDao())
        workManager = WorkManager.getInstance(applicationContext)
    }
    
    private fun showSimSwitchDialog() {
        val dialog = SimSwitchDialogFragment.newInstance(
            onConfirm = {
                performSimSwitch()
            },
            onCancel = {
                Toast.makeText(this, "SIM switch cancelled", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.show(supportFragmentManager, SimSwitchDialogFragment.TAG)
    }
    
    private fun performSimSwitch() {
        // Get current SIM information
        val currentSimCards = viewModel.simCards.value ?: emptyList()
        
        if (currentSimCards.isEmpty()) {
            Toast.makeText(this, "No SIM cards detected", Toast.LENGTH_SHORT).show()
            return
        }
        
        // For demo purposes, switch between available SIM slots
        val currentSlot = currentSimCards.firstOrNull()?.slotNumber ?: 0
        val nextSlot = if (currentSlot == 0) 1 else 0
        
        val currentSim = currentSimCards.firstOrNull()?.carrierName ?: "Unknown"
        val nextSim = if (currentSlot == 0) "Secondary SIM" else "Primary SIM"
        
        // Log the SIM switch event
        lifecycleScope.launch {
            try {
                val eventId = simSwitchRepository.logSimSwitch(
                    oldSim = currentSim,
                    newSim = nextSim,
                    oldSimSlot = currentSlot,
                    newSimSlot = nextSlot,
                    switchReason = "Manual switch by user"
                )
                
                // Schedule balance sync work
                val workRequest = BalanceSyncWorker.createWorkRequest(
                    simSlot = nextSlot,
                    carrierName = nextSim,
                    switchEventId = eventId
                )
                
                workManager.enqueue(workRequest)
                
                Toast.makeText(
                    this@MainActivity,
                    "SIM switched from $currentSim to $nextSim. Balance sync scheduled.",
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to log SIM switch: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            Toast.makeText(this, "Permissions granted! Loading SIM card information...", Toast.LENGTH_SHORT).show()
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
            .setPositiveButton("Continue") { _, _ ->
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
}