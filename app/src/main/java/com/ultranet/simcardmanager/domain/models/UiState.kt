package com.ultranet.simcardmanager.domain.models

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isEmpty(): Boolean = this is Empty
    
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getErrorMessageOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
} 