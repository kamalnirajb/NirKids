package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GetRandomLetterUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(): Alphabet? {
        val alphabets = repository.getAllAlphabets().first()
        return if (alphabets.isNotEmpty()) {
            alphabets[Random.nextInt(alphabets.size)]
        } else null
    }
}
