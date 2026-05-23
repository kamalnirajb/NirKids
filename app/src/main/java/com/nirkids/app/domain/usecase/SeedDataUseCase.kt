package com.nirkids.app.domain.usecase

import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.domain.repository.IAlphabetRepository

class SeedDataUseCase(
    private val repository: IAlphabetRepository,
    private val database: AlphabetsDatabase
) {
    suspend operator fun invoke() {
        repository.seedInitialData(database)
    }
}
