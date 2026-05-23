package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.repository.IAlphabetRepository

class MarkLetterLearnedUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(letter: Char) {
        repository.markLetterLearned(letter)
        repository.incrementAttempts(letter)
    }
}
