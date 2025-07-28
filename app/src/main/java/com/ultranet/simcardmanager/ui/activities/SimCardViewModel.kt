package com.ultranet.simcardmanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultranet.simcardmanager.data.repository.TelephonyRepository
import com.ultranet.simcardmanager.domain.models.SimCardInfo
import kotlinx.coroutines.launch

class SimCardViewModel(
    private val telephonyRepository: TelephonyRepository
) : ViewModel() {
    
    private val _simCards = MutableLiveData<List<SimCardInfo>>()
    val simCards: LiveData<List<SimCardInfo>> = _simCards
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _hasPermissions = MutableLiveData<Boolean>()
    val hasPermissions: LiveData<Boolean> = _hasPermissions
    
    init {
        loadSimCards()
    }
    
    fun loadSimCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasPermissions.value = telephonyRepository.hasRequiredPermissions()
            
            try {
                val simCards = telephonyRepository.getSimCardInfo()
                _simCards.value = simCards
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load SIM cards"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshSimCards() {
        loadSimCards()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getSimCardBySlot(slotNumber: Int): SimCardInfo? {
        return _simCards.value?.find { it.slotNumber == slotNumber }
    }
    
    fun getActiveSimCards(): List<SimCardInfo> {
        return _simCards.value?.filter { it.simState == "READY" } ?: emptyList()
    }
} 