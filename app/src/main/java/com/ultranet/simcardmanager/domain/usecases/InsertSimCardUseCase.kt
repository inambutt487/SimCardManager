package com.ultranet.simcardmanager.domain.usecases

import com.ultranet.simcardmanager.data.repository.SimCardRepository
import com.ultranet.simcardmanager.domain.models.SimCard

class InsertSimCardUseCase(
    private val repository: SimCardRepository
) {
    suspend operator fun invoke(simCard: SimCard): Long {
        return repository.insertSimCard(simCard)
    }
} 