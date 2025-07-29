package com.ultranet.simcardmanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultranet.simcardmanager.data.repository.TelephonyRepository
import com.ultranet.simcardmanager.domain.models.SimCardInfo
import kotlinx.coroutines.launch

/**
 * SimCardViewModel - ViewModel Layer (MVVM Architecture)
 * 
 * RESPONSIBILITIES:
 * - Manages UI state and business logic
 * - Handles data operations through Repository
 * - Provides LiveData for reactive UI updates
 * - Survives configuration changes (screen rotation, etc.)
 * - Coordinates between View and Model layers
 * 
 * MVVM PATTERN:
 * - VIEWMODEL: This class acts as the mediator between View and Model
 * - VIEW: Activities/Fragments observe this ViewModel's LiveData
 * - MODEL: TelephonyRepository handles actual data operations
 * 
 * TELECOM CHALLENGES ADDRESSED:
 * - Async SIM card information retrieval
 * - Permission state management
 * - Error handling for telephony operations
 * - Loading state management for better UX
 * 
 * PERFORMANCE CONSIDERATIONS:
 * - Uses coroutines for async operations
 * - LiveData for lifecycle-aware data updates
 * - ViewModelScope for automatic cancellation
 * - MutableLiveData for internal state management
 * 
 * MEMORY MANAGEMENT:
 * - ViewModel survives configuration changes
 * - Automatic cleanup when ViewModel is destroyed
 * - No memory leaks due to lifecycle awareness
 */
class SimCardViewModel(
    private val telephonyRepository: TelephonyRepository
) : ViewModel() {
    
    /**
     * LiveData for SIM cards information
     * MVVM: View observes this for UI updates
     * Performance: Only updates UI when data actually changes
     */
    private val _simCards = MutableLiveData<List<SimCardInfo>>()
    val simCards: LiveData<List<SimCardInfo>> = _simCards
    
    /**
     * LiveData for loading state
     * UX: Shows progress indicator during data loading
     */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    /**
     * LiveData for error state
     * UX: Shows error messages to user
     */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    /**
     * LiveData for permission state
     * Telecom Challenge: Tracks permission availability for features
     */
    private val _hasPermissions = MutableLiveData<Boolean>()
    val hasPermissions: LiveData<Boolean> = _hasPermissions
    
    /**
     * Initialize ViewModel and load initial data
     * Performance: Automatic data loading on ViewModel creation
     */
    init {
        loadSimCards()
    }
    
    /**
     * Load SIM cards information from Repository
     * Telecom Challenge: Handles async telephony operations
     * Performance: Uses coroutines for non-blocking operations
     */
    fun loadSimCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasPermissions.value = telephonyRepository.hasRequiredPermissions()
            
            try {
                // Get SIM card information from Repository (Model layer)
                val simCards = telephonyRepository.getSimCardInfo()
                _simCards.value = simCards
                _error.value = null
            } catch (e: Exception) {
                // Handle errors and provide user feedback
                _error.value = e.message ?: "Failed to load SIM cards"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh SIM cards data
     * Telecom Feature: Allows manual refresh of SIM information
     */
    fun refreshSimCards() {
        loadSimCards()
    }
    
    /**
     * Clear error state
     * UX: Allows user to dismiss error messages
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Get SIM card by slot number
     * Telecom Feature: Access specific SIM slot information
     * Performance: Efficient lookup from cached data
     */
    fun getSimCardBySlot(slotNumber: Int): SimCardInfo? {
        return _simCards.value?.find { it.slotNumber == slotNumber }
    }
    
    /**
     * Get active SIM cards only
     * Telecom Feature: Filter for ready/active SIM cards
     * Performance: Efficient filtering of cached data
     */
    fun getActiveSimCards(): List<SimCardInfo> {
        return _simCards.value?.filter { it.simState == "READY" } ?: emptyList()
    }
} 