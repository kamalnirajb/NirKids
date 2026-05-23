package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.Flow

class GetAlphabetUseCase(
    private val repository: IAlphabetRepository
) {
    operator fun invoke(): Flow<List<Alphabet>> {
        return repository.getAllAlphabets()
    }
}
