package com.ultranet.simcardmanager.domain.usecases

import com.ultranet.simcardmanager.data.repository.SimCardRepository

class ActivateSimCardUseCase(
    private val repository: SimCardRepository
) {
    suspend operator fun invoke(simCardId: Long) {
        repository.activateSimCard(simCardId)
    }
} 