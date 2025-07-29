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
                        // Try to load cached data as fallback
                        loadCachedDataAsFallback(result.message)
                    }
                    else -> {
                        // Loading state handled by LiveData
                    }
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                // Handle JSON parsing errors (including BigInt issues)
                loadCachedDataAsFallback("Data format error. Please try again.")
            } catch (e: java.lang.NumberFormatException) {
                // Handle number parsing errors
                loadCachedDataAsFallback("Invalid data format. Please try again.")
            } catch (e: retrofit2.HttpException) {
                // Handle HTTP errors
                val errorMessage = when (e.code()) {
                    404 -> "Telecom plans not found"
                    500 -> "Server error. Please try again later."
                    503 -> "Service temporarily unavailable"
                    else -> "Network error (${e.code()})"
                }
                loadCachedDataAsFallback(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                // Handle no internet connection
                loadCachedDataAsFallback("No internet connection. Please check your network.")
            } catch (e: java.net.SocketTimeoutException) {
                // Handle timeout errors
                loadCachedDataAsFallback("Request timed out. Please try again.")
            } catch (e: Exception) {
                // Handle any other unexpected errors
                val errorMessage = when {
                    e.message?.contains("BigInteger", ignoreCase = true) == true -> 
                        "Data format error. Please try again."
                    e.message?.contains("Expected", ignoreCase = true) == true -> 
                        "Invalid response format. Please try again."
                    else -> "Failed to refresh telecom plans: ${e.message}"
                }
                loadCachedDataAsFallback(errorMessage)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Load cached data as fallback when API fails
     */
    private suspend fun loadCachedDataAsFallback(errorMessage: String) {
        try {
            telecomRepository.getAllTelecomPlans().collectLatest { cachedPlans ->
                if (cachedPlans.isNotEmpty()) {
                    _uiState.value = UiState.Success(cachedPlans)
                    applyFilters()
                    // Show error as toast but still display cached data
                    // The error will be shown in the UI state observer
                } else {
                    _uiState.value = UiState.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(errorMessage)
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