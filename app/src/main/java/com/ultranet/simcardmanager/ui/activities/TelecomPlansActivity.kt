package com.ultranet.simcardmanager.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultranet.simcardmanager.data.api.TelecomRetrofitClient
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.TelecomRepository
import com.ultranet.simcardmanager.databinding.ActivityTelecomPlansBinding
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import com.ultranet.simcardmanager.ui.adapters.TelecomPlanAdapter

class TelecomPlansActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTelecomPlansBinding
    private lateinit var telecomPlanAdapter: TelecomPlanAdapter
    private lateinit var viewModel: TelecomPlansViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityTelecomPlansBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this)
        val repository = TelecomRepository(
            database.telecomPlanDao(),
            TelecomRetrofitClient.telecomApiService
        )
        viewModel = TelecomPlansViewModel(repository)
    }
    
    private fun setupRecyclerView() {
        telecomPlanAdapter = TelecomPlanAdapter(
            onPlanClick = { plan ->
                // Handle plan click - show details
                Toast.makeText(this, "Selected: ${plan.name}", Toast.LENGTH_SHORT).show()
            },
            onPlanSelect = { plan ->
                // Handle plan selection
                viewModel.selectPlan(plan)
                Toast.makeText(this, "Plan selected: ${plan.name}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.recyclerViewTelecomPlans.apply {
            layoutManager = LinearLayoutManager(this@TelecomPlansActivity)
            adapter = telecomPlanAdapter
        }
    }
    
    private fun setupObservers() {
        // Observe UI state
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is com.ultranet.simcardmanager.domain.models.UiState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvEmptyState.visibility = android.view.View.GONE
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.VISIBLE
                    binding.tvEmptyState.visibility = android.view.View.GONE
                    telecomPlanAdapter.submitList(state.data)
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvEmptyState.visibility = android.view.View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is com.ultranet.simcardmanager.domain.models.UiState.Empty -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.recyclerViewTelecomPlans.visibility = android.view.View.GONE
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                }
            }
        }
        
        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }
        
        viewModel.selectedPlan.observe(this) { selectedPlan ->
            selectedPlan?.let { plan ->
                binding.tvSelectedPlan.text = "Selected: ${plan.name} - $${String.format("%.2f", plan.price)}"
                binding.tvSelectedPlan.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.tvSelectedPlan.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshTelecomPlans()
        }
        
        binding.btnLoadTopPlans.setOnClickListener {
            viewModel.getTopPlans(3)
        }
        
//        binding.btnClearFilters.setOnClickListener {
//            viewModel.clearFilters()
//        }
        
        binding.btnClearSelection.setOnClickListener {
            viewModel.clearSelection()
        }
    }
} 