package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.repository.IAlphabetRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.*

@RunWith(RobolectricTestRunner::class)
class GetProgressUseCaseTest {

    private lateinit var useCase: GetProgressUseCase
    private lateinit var mockRepository: IAlphabetRepository

    private val testProgress = listOf(
        LetterProgress('A', learned = true, attempts = 3, masteryLevel = 2),
        LetterProgress('B', learned = false, attempts = 1, masteryLevel = 1)
    )

    @Before
    fun setup() {
        mockRepository = mock(IAlphabetRepository::class.java)
        useCase = GetProgressUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runBlocking {
        `when`(mockRepository.getAllProgress()).thenReturn(flowOf(testProgress))

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals('A', result[0].letter)
        assertTrue(result[0].learned)
        assertEquals('B', result[1].letter)
        assertFalse(result[1].learned)
        verify(mockRepository, times(1)).getAllProgress()
    }

    @Test
    fun `invoke returns empty list when no progress`() = runBlocking {
        `when`(mockRepository.getAllProgress()).thenReturn(flowOf(emptyList()))

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }
}
