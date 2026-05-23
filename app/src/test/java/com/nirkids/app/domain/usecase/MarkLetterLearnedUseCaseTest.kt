package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.*

@RunWith(RobolectricTestRunner::class)
class MarkLetterLearnedUseCaseTest {

    private lateinit var useCase: MarkLetterLearnedUseCase
    private lateinit var mockRepository: IAlphabetRepository

    @Before
    fun setup() {
        mockRepository = mock(IAlphabetRepository::class.java)
        useCase = MarkLetterLearnedUseCase(mockRepository)
    }

    @Test
    fun `invoke marks letter learned and increments attempts`() = runBlocking {
        `when`(mockRepository.markLetterLearned('A')).thenAnswer { null }
        `when`(mockRepository.incrementAttempts('A')).thenAnswer { null }

        useCase('A')

        verify(mockRepository, times(1)).markLetterLearned('A')
        verify(mockRepository, times(1)).incrementAttempts('A')
    }
}
