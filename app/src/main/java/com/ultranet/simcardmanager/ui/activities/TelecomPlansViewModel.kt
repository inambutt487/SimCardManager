package com.ultranet.simcardmanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultranet.simcardmanager.data.repository.TelecomRepository
import com.ultranet.simcardmanager.domain.models.TelecomPlan
import com.ultranet.simcardmanager.domain.models.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TelecomPlansViewModel(
    private val telecomRepository: TelecomRepository
) : ViewModel() {
    
    private val _uiState = MutableLiveData<UiState<List<TelecomPlan>>>()
    val uiState: LiveData<UiState<List<TelecomPlan>>> = _uiState
    
    private val _selectedPlan = MutableLiveData<TelecomPlan?>()
    val selectedPlan: LiveData<TelecomPlan?> = _selectedPlan
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    
    private val _filteredPlans = MutableLiveData<List<TelecomPlan>>()
    val filteredPlans: LiveData<List<TelecomPlan>> = _filteredPlans
    
    private var currentCarrierFilter: String? = null
    private var currentPriceFilter: Double? = null
    
    init {
        loadTelecomPlans()
        insertMockDataIfNeeded()
    }
    
    fun loadTelecomPlans() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                telecomRepository.getAllTelecomPlans().collectLatest { plans ->
                    if (plans.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(plans)
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load telecom plans")
            }
        }
    }
    
    fun refreshTelecomPlans() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = telecomRepository.syncTelecomPlansFromApi()
                when (result) {
                    is com.ultranet.simcardmanager.domain.models.ApiResponse.Success -> {
                        if (result.data.isEmpty()) {
                            _uiState.value = UiState.Empty
                        } else {
                            _uiState.value = UiState.Success(result.data)
                            applyFilters()
                        }
                    }
                    is com.ultranet.simcardmanager.domain.models.ApiResponse.Error -> {
                        _uiState.value = UiState.Error(result.message)
                    }
                    else -> {
                        // Loading state handled by LiveData
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to refresh telecom plans")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun selectPlan(plan: TelecomPlan) {
        _selectedPlan.value = plan
    }
    
    fun clearSelection() {
        _selectedPlan.value = null
    }
    
    fun filterByCarrier(carrier: String?) {
        currentCarrierFilter = carrier
        applyFilters()
    }
    
    fun filterByMaxPrice(maxPrice: Double?) {
        currentPriceFilter = maxPrice
        applyFilters()
    }
    
    fun clearFilters() {
        currentCarrierFilter = null
        currentPriceFilter = null
        applyFilters()
    }
    
    private fun applyFilters() {
        val allPlans = _uiState.value?.getDataOrNull() ?: emptyList()
        var filtered = allPlans
        
        // Apply carrier filter
        currentCarrierFilter?.let { carrier ->
            if (carrier.isNotEmpty()) {
                filtered = filtered.filter { 
                    it.name.contains(carrier, ignoreCase = true) 
                }
            }
        }
        
        // Apply price filter
        currentPriceFilter?.let { maxPrice ->
            filtered = filtered.filter { it.price <= maxPrice }
        }
        
        _filteredPlans.value = filtered
    }
    
    fun getPlanById(id: String) {
        viewModelScope.launch {
            try {
                val plan = telecomRepository.getTelecomPlanById(id)
                _selectedPlan.value = plan
            } catch (e: Exception) {
                // Handle error silently for plan details
            }
        }
    }
    
    fun loadPlansByCarrier(carrier: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                telecomRepository.getTelecomPlansByCarrier(carrier).collectLatest { plans ->
                    if (plans.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(plans)
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load plans for carrier")
            }
        }
    }
    
    fun getTopPlans(limit: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                telecomRepository.getTopTelecomPlans(limit).collectLatest { plans ->
                    if (plans.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(plans)
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load top plans")
            }
        }
    }
    
    private fun insertMockDataIfNeeded() {
        viewModelScope.launch {
            try {
                // Check if we have any plans, if not, insert mock data
                telecomRepository.getAllTelecomPlans().collectLatest { plans ->
                    if (plans.isEmpty()) {
                        telecomRepository.insertMockTelecomPlans()
                    }
                }
            } catch (e: Exception) {
                // If there's an error, insert mock data anyway
                telecomRepository.insertMockTelecomPlans()
            }
        }
    }
    
    fun clearError() {
        // Reload data to clear error state
        loadTelecomPlans()
    }
    
    fun getSelectedPlanPrice(): Double {
        return _selectedPlan.value?.price ?: 0.0
    }
    
    fun getSelectedPlanData(): String {
        return _selectedPlan.value?.data ?: ""
    }
    
    fun isPlanSelected(): Boolean {
        return _selectedPlan.value != null
    }
} 