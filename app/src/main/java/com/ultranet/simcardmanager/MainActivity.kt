package com.ultranet.simcardmanager

import android.Manifest
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
import com.ultranet.simcardmanager.data.api.TelecomRetrofitClient
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.SimSwitchRepository
import com.ultranet.simcardmanager.data.repository.TelecomRepository
import com.ultranet.simcardmanager.data.repository.TelephonyRepository
import com.ultranet.simcardmanager.databinding.ActivityMainBinding
import com.ultranet.simcardmanager.domain.models.SimCardInfo
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import com.ultranet.simcardmanager.ui.activities.SimCardViewModel
import com.ultranet.simcardmanager.ui.activities.TelecomPlansViewModel
import com.ultranet.simcardmanager.ui.adapters.SimCardInfoAdapter
import com.ultranet.simcardmanager.ui.adapters.TelecomPlanAdapter
import com.ultranet.simcardmanager.ui.fragments.SimSwitchDialogFragment
import com.ultranet.simcardmanager.utils.BalanceSyncWorker
import com.ultranet.simcardmanager.utils.PermissionUtils

/**
 * MainActivity - Streamlined SIM Card Manager
 * 
 * FEATURES:
 * 1. Permission check on launch
 * 2. Display SIM details (slot, carrier, state, network)
 * 3. Show telecom plans with Retrofit API and Room caching
 * 4. Plan selection capability
 * 5. "Switch SIM" button with confirmation dialog
 * 6. Log SimSwitchEvent to Room database
 * 7. Trigger WorkManager balance sync
 * 8. Handle offline with cached data
 * 
 * PERMISSION HANDLING:
 * - Bulletproof READ_PHONE_STATE permission handling
 * - Runtime request → rationale dialog → mock data fallback → Settings redirect
 * - App never crashes due to permissions
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var simCardInfoAdapter: SimCardInfoAdapter
    private lateinit var telecomPlanAdapter: TelecomPlanAdapter
    private lateinit var simCardViewModel: SimCardViewModel
    private lateinit var telecomPlansViewModel: TelecomPlansViewModel
    private lateinit var simSwitchRepository: SimSwitchRepository
    private lateinit var workManager: WorkManager
    
    /**
     * Permission launcher for READ_PHONE_STATE
     * Bulletproof permission handling with fallback
     */
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    
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
        
        // Initialize permission launcher after binding setup
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handlePermissionResult(isGranted)
        }
        
        setupComponents()
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        
        // Delay permission check to ensure activity is fully initialized
        binding.main.post {
            checkPermissionOnLaunch()
        }
    }
    
    /**
     * Setup all components including ViewModels and Repositories
     */
    private fun setupComponents() {
        // Setup SIM card components
        val telephonyRepository = TelephonyRepository(this)
        simCardViewModel = SimCardViewModel(telephonyRepository)
        
        // Setup telecom plans components
        val database = AppDatabase.getDatabase(this)
        val telecomRepository = TelecomRepository(
            database.telecomPlanDao(),
            TelecomRetrofitClient.telecomApiService
        )
        telecomPlansViewModel = TelecomPlansViewModel(telecomRepository)
        
        // Setup SIM switch components
        simSwitchRepository = SimSwitchRepository(database.simSwitchDao())
        workManager = WorkManager.getInstance(applicationContext)
    }
    
    /**
     * Setup RecyclerViews for SIM cards and telecom plans
     */
    private fun setupRecyclerViews() {
        // SIM Cards RecyclerView
        simCardInfoAdapter = SimCardInfoAdapter(
            onSimCardClick = { simCardInfo ->
                Toast.makeText(this, "SIM: ${simCardInfo.carrierName}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewSimCards.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = simCardInfoAdapter
        }
        
        // Telecom Plans RecyclerView
        telecomPlanAdapter = TelecomPlanAdapter(
            onPlanClick = { plan ->
                Toast.makeText(this, "Plan: ${plan.name}", Toast.LENGTH_SHORT).show()
            },
            onPlanSelect = { plan ->
                telecomPlansViewModel.selectPlan(plan)
                Toast.makeText(this, "Selected: ${plan.name}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewTelecomPlans.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = telecomPlanAdapter
        }
    }
    
    /**
     * Setup LiveData observers for UI updates
     */
    private fun setupObservers() {
        // SIM Cards observers
        simCardViewModel.simCards.observe(this) { simCards ->
            simCardInfoAdapter.submitList(simCards)
            binding.tvSimEmptyState.visibility = if (simCards.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        simCardViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarSim.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        simCardViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                simCardViewModel.clearError()
            }
        }
        
        // Telecom Plans observers
        telecomPlansViewModel.uiState.observe(this) { state ->
            when (state) {
                is com.ultranet.simcardmanager.domain.models.UiState.Loading -> {
                    binding.progressBarPlans.visibility = android.view.View.VISIBLE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvPlansEmptyState.visibility = android.view.View.GONE
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Success -> {
                    binding.progressBarPlans.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.VISIBLE
                    binding.tvPlansEmptyState.visibility = android.view.View.GONE
                    telecomPlanAdapter.submitList(state.data)
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Error -> {
                    binding.progressBarPlans.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvPlansEmptyState.visibility = android.view.View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Empty -> {
                    binding.progressBarPlans.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvPlansEmptyState.visibility = android.view.View.VISIBLE
                }
            }
        }
        
        telecomPlansViewModel.selectedPlan.observe(this) { selectedPlan ->
            selectedPlan?.let { plan ->
                binding.tvSelectedPlan.text = "Selected: ${plan.name} - $${String.format("%.2f", plan.price)}"
                binding.tvSelectedPlan.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.tvSelectedPlan.visibility = android.view.View.GONE
            }
        }
    }
    
    /**
     * Setup click listeners for buttons
     */
    private fun setupClickListeners() {
        // Switch SIM button
        binding.btnSwitchSim.setOnClickListener {
            showSimSwitchDialog()
        }
        
        // Refresh telecom plans
        binding.btnRefreshPlans.setOnClickListener {
            telecomPlansViewModel.refreshTelecomPlans()
        }
        
        // Clear plan selection
        binding.btnClearSelection.setOnClickListener {
            telecomPlansViewModel.clearSelection()
        }
    }
    
    /**
     * Check permission on app launch
     * Bulletproof permission handling
     */
    private fun checkPermissionOnLaunch() {
        android.util.Log.d("MainActivity", "checkPermissionOnLaunch: hasPermission=${hasPhoneStatePermission()}")
        
        if (hasPhoneStatePermission()) {
            // Permission granted, load data
            android.util.Log.d("MainActivity", "Permission already granted, loading data")
            loadData()
        } else {
            // Always request permission on first launch
            android.util.Log.d("MainActivity", "Permission not granted, requesting permission")
            Toast.makeText(this, "Requesting phone permission...", Toast.LENGTH_SHORT).show()
            requestPermission()
        }
    }
    
    /**
     * Check if READ_PHONE_STATE permission is granted
     */
    private fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, 
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Handle missing permission with bulletproof flow
     */
    private fun handleMissingPermission() {
        android.util.Log.d("MainActivity", "handleMissingPermission: shouldShowRationale=${shouldShowPermissionRationale()}, isPermanentlyDenied=${isPermissionPermanentlyDenied()}")
        
        if (shouldShowPermissionRationale()) {
            // Show rationale dialog
            android.util.Log.d("MainActivity", "Showing permission rationale dialog")
            showPermissionRationaleDialog()
        } else if (isPermissionPermanentlyDenied()) {
            // Show settings redirect dialog
            android.util.Log.d("MainActivity", "Showing settings redirect dialog")
            showSettingsRedirectDialog()
        } else {
            // Request permission directly
            android.util.Log.d("MainActivity", "Requesting permission directly")
            requestPermission()
        }
    }
    
    /**
     * Check if should show permission rationale
     */
    private fun shouldShowPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this, 
            Manifest.permission.READ_PHONE_STATE
        )
    }
    
    /**
     * Check if permission is permanently denied
     */
    private fun isPermissionPermanentlyDenied(): Boolean {
        return !hasPhoneStatePermission() && !shouldShowPermissionRationale()
    }
    
    /**
     * Show permission rationale dialog
     */
    private fun showPermissionRationaleDialog() {
        android.util.Log.d("MainActivity", "Showing permission rationale dialog")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Phone State permission is required to access SIM card information, including carrier details and network status.")
            .setPositiveButton("Grant Permission") { _, _ ->
                android.util.Log.d("MainActivity", "User clicked Grant Permission")
                requestPermission()
            }
            .setNegativeButton("Cancel") { _, _ ->
                android.util.Log.d("MainActivity", "User clicked Cancel, loading mock data")
                // Load with mock data
                loadDataWithMockSim()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Show settings redirect dialog
     */
    private fun showSettingsRedirectDialog() {
        android.util.Log.d("MainActivity", "Showing settings redirect dialog")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Phone State permission has been permanently denied. Please enable it in Settings to access SIM card information.")
            .setPositiveButton("Open Settings") { _, _ ->
                android.util.Log.d("MainActivity", "User clicked Open Settings")
                openAppSettings()
            }
            .setNegativeButton("Continue") { _, _ ->
                android.util.Log.d("MainActivity", "User clicked Continue, loading mock data")
                // Load with mock data
                loadDataWithMockSim()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Request READ_PHONE_STATE permission
     */
    private fun requestPermission() {
        android.util.Log.d("MainActivity", "Requesting READ_PHONE_STATE permission")
        Toast.makeText(this, "Permission dialog should appear now", Toast.LENGTH_SHORT).show()
        permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
    }
    
    /**
     * Handle permission request result
     */
    private fun handlePermissionResult(isGranted: Boolean) {
        android.util.Log.d("MainActivity", "handlePermissionResult: isGranted=$isGranted")
        
        if (isGranted) {
            // Permission granted, load real data
            android.util.Log.d("MainActivity", "Permission granted, loading real data")
            loadData()
            Toast.makeText(this, "Permission granted! Loading SIM information...", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied, check if permanently denied
            android.util.Log.d("MainActivity", "Permission denied, checking if permanently denied")
            if (isPermissionPermanentlyDenied()) {
                // Show settings redirect dialog
                android.util.Log.d("MainActivity", "Permission permanently denied, showing settings dialog")
                showSettingsRedirectDialog()
            } else {
                // Show rationale dialog for next time
                android.util.Log.d("MainActivity", "Permission denied but not permanently, showing rationale")
                showPermissionRationaleDialog()
            }
        }
    }
    
    /**
     * Load data with real SIM information
     */
    private fun loadData() {
        simCardViewModel.refreshSimCards()
        telecomPlansViewModel.loadTelecomPlans()
    }
    
    /**
     * Load data with mock SIM information (fallback)
     */
    private fun loadDataWithMockSim() {
        // Load telecom plans (works offline)
        telecomPlansViewModel.loadTelecomPlans()
        
        // Show mock SIM data
        val mockSimCards = listOf(
            SimCardInfo(
                slotNumber = 0,
                carrierName = "Mock Carrier (Permission Required)",
                simState = "READY",
                networkType = "4G"
            )
        )
        simCardInfoAdapter.submitList(mockSimCards)
        binding.tvSimEmptyState.visibility = android.view.View.GONE
    }
    
    /**
     * Open app settings
     */
    private fun openAppSettings() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
    
    /**
     * Show SIM switch confirmation dialog
     */
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
    
    /**
     * Perform SIM switching operation
     */
    private fun performSimSwitch() {
        val currentSimCards = simCardViewModel.simCards.value ?: emptyList()
        
        if (currentSimCards.isEmpty()) {
            Toast.makeText(this, "No SIM cards detected", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Simulate SIM switching
        val currentSlot = currentSimCards.firstOrNull()?.slotNumber ?: 0
        val nextSlot = if (currentSlot == 0) 1 else 0
        
        val currentSim = currentSimCards.firstOrNull()?.carrierName ?: "Unknown"
        val nextSim = if (currentSlot == 0) "Secondary SIM" else "Primary SIM"
        
        // Log switch event and trigger balance sync
        lifecycleScope.launch {
            try {
                val eventId = simSwitchRepository.logSimSwitch(
                    oldSim = currentSim,
                    newSim = nextSim,
                    oldSimSlot = currentSlot,
                    newSimSlot = nextSlot,
                    switchReason = "Manual switch by user"
                )
                
                // Trigger WorkManager balance sync
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
}