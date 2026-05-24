package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class MarkLetterLearnedUseCaseTest {

    private lateinit var useCase: MarkLetterLearnedUseCase
    private lateinit var mockRepository: IAlphabetRepository

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = MarkLetterLearnedUseCase(mockRepository)
    }

    @Test
    fun `invoke marks letter learned and increments attempts`() = runBlocking {
        coEvery { mockRepository.markLetterLearned('A') } returns Unit
        coEvery { mockRepository.incrementAttempts('A') } returns Unit

        useCase('A')

        coVerify(exactly = 1) { mockRepository.markLetterLearned('A') }
        coVerify(exactly = 1) { mockRepository.incrementAttempts('A') }
    }
}
