package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.Flow

class GetProgressUseCase(
    private val repository: IAlphabetRepository
) {
    operator fun invoke(): Flow<List<LetterProgress>> {
        return repository.getAllProgress()
    }
}
