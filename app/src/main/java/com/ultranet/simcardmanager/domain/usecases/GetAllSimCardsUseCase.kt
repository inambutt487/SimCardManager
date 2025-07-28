package com.ultranet.simcardmanager.domain.usecases

import com.ultranet.simcardmanager.data.repository.SimCardRepository
import com.ultranet.simcardmanager.domain.models.SimCard
import kotlinx.coroutines.flow.Flow

class GetAllSimCardsUseCase(
    private val repository: SimCardRepository
) {
    operator fun invoke(): Flow<List<SimCard>> {
        return repository.getAllSimCards()
    }
} 