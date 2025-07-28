package com.ultranet.simcardmanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultranet.simcardmanager.domain.models.SimCard
import com.ultranet.simcardmanager.domain.usecases.ActivateSimCardUseCase
import com.ultranet.simcardmanager.domain.usecases.GetAllSimCardsUseCase
import com.ultranet.simcardmanager.domain.usecases.InsertSimCardUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val getAllSimCardsUseCase: GetAllSimCardsUseCase,
    private val insertSimCardUseCase: InsertSimCardUseCase,
    private val activateSimCardUseCase: ActivateSimCardUseCase
) : ViewModel() {
    
    private val _simCards = MutableLiveData<List<SimCard>>()
    val simCards: LiveData<List<SimCard>> = _simCards
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadSimCards()
    }
    
    fun loadSimCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getAllSimCardsUseCase().collectLatest { simCards ->
                    _simCards.value = simCards
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun addSimCard(simCard: SimCard) {
        viewModelScope.launch {
            try {
                insertSimCardUseCase(simCard)
                loadSimCards() // Reload the list
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun activateSimCard(simCardId: Long) {
        viewModelScope.launch {
            try {
                activateSimCardUseCase(simCardId)
                loadSimCards() // Reload the list
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 