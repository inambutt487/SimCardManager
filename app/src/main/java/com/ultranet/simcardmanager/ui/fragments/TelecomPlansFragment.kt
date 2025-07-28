package com.ultranet.simcardmanager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ultranet.simcardmanager.data.database.AppDatabase
import com.ultranet.simcardmanager.data.repository.TelecomRepository
import com.ultranet.simcardmanager.databinding.FragmentTelecomPlansBinding
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import com.ultranet.simcardmanager.domain.models.UiState
import com.ultranet.simcardmanager.ui.activities.TelecomPlansViewModel
import com.ultranet.simcardmanager.ui.adapters.TelecomPlanAdapter

class TelecomPlansFragment : Fragment() {
    
    private var _binding: FragmentTelecomPlansBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var telecomPlanAdapter: TelecomPlanAdapter
    private lateinit var viewModel: TelecomPlansViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelecomPlansBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = TelecomRepository(
            database.telecomPlanDao(),
            com.ultranet.simcardmanager.data.api.TelecomRetrofitClient.telecomApiService
        )
        viewModel = TelecomPlansViewModel(repository)
    }
    
    private fun setupRecyclerView() {
        telecomPlanAdapter = TelecomPlanAdapter(
            onPlanClick = { plan ->
                showPlanDetails(plan)
            },
            onPlanSelect = { plan ->
                viewModel.selectPlan(plan)
                telecomPlanAdapter.setSelectedPlan(plan.id)
                showSelectionSnackbar(plan)
            }
        )
        
        binding.recyclerViewTelecomPlans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = telecomPlanAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshTelecomPlans()
        }
    }
    
    private fun setupObservers() {
        // Observe UI state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoadingState()
                }
                is UiState.Success -> {
                    hideLoadingState()
                    showSuccessState(state.data)
                }
                is UiState.Error -> {
                    hideLoadingState()
                    showErrorState(state.message)
                }
                is UiState.Empty -> {
                    hideLoadingState()
                    showEmptyState()
                }
            }
        }
        
        // Observe refreshing state
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }
        
        // Observe selected plan
        viewModel.selectedPlan.observe(viewLifecycleOwner) { plan ->
            plan?.let {
                updateSelectedPlanDisplay(it)
            }
        }
        
        // Observe filtered plans
        viewModel.filteredPlans.observe(viewLifecycleOwner) { plans ->
            telecomPlanAdapter.submitList(plans)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnFilterByCarrier.setOnClickListener {
            showCarrierFilterDialog()
        }
        
        binding.btnFilterByPrice.setOnClickListener {
            showPriceFilterDialog()
        }
        
        binding.btnClearFilters.setOnClickListener {
            viewModel.clearFilters()
            showSnackbar("Filters cleared")
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshTelecomPlans()
        }
    }
    
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewTelecomPlans.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.tvErrorState.visibility = View.GONE
    }
    
    private fun hideLoadingState() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun showSuccessState(plans: List<TelecomPlan>) {
        binding.recyclerViewTelecomPlans.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.tvErrorState.visibility = View.GONE
        
        telecomPlanAdapter.submitList(plans)
    }
    
    private fun showErrorState(message: String) {
        binding.recyclerViewTelecomPlans.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        binding.tvErrorState.visibility = View.VISIBLE
        
        binding.tvErrorState.text = message
        binding.btnRetry.setOnClickListener {
            viewModel.clearError()
        }
        
        showSnackbar(message)
    }
    
    private fun showEmptyState() {
        binding.recyclerViewTelecomPlans.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.tvErrorState.visibility = View.GONE
        
        binding.tvEmptyState.text = "No telecom plans available"
    }
    
    private fun showPlanDetails(plan: TelecomPlan) {
        val message = """
            Plan: ${plan.name}
            Price: $${plan.price}
            Data: ${plan.data}
            Carrier: ${plan.carrierName ?: "Unknown"}
            Type: ${plan.planType ?: "POSTPAID"}
            ${if (plan.contractLength != null) "Contract: ${plan.contractLength} months" else ""}
            ${if (!plan.features.isNullOrEmpty()) "Features: ${plan.features}" else ""}
        """.trimIndent()
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showSelectionSnackbar(plan: TelecomPlan) {
        val message = "Selected: ${plan.name} - $${plan.price}"
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    private fun updateSelectedPlanDisplay(plan: TelecomPlan) {
        binding.tvSelectedPlan.text = "Selected: ${plan.name} - $${plan.price}"
        binding.tvSelectedPlan.visibility = View.VISIBLE
    }
    
    private fun showCarrierFilterDialog() {
        // Simple dialog for carrier filter
        val carriers = listOf("Verizon", "AT&T", "T-Mobile", "Sprint", "Cricket")
        val items = carriers.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter by Carrier")
            .setItems(items) { _, which ->
                val selectedCarrier = carriers[which]
                viewModel.filterByCarrier(selectedCarrier)
                showSnackbar("Filtered by: $selectedCarrier")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showPriceFilterDialog() {
        val priceRanges = listOf("$20", "$30", "$50", "$80", "$100")
        val items = priceRanges.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter by Max Price")
            .setItems(items) { _, which ->
                val maxPrice = priceRanges[which].removePrefix("$").toDoubleOrNull()
                viewModel.filterByMaxPrice(maxPrice)
                showSnackbar("Filtered by max price: $${maxPrice}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 