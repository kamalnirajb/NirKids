package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
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
class GetAlphabetUseCaseTest {

    private lateinit var useCase: GetAlphabetUseCase
    private lateinit var mockRepository: IAlphabetRepository

    private val testAlphabets = listOf(
        Alphabet('A', '/æ/', "Apple", "🍎", true),
        Alphabet('B', '/b/', "Ball", "⚽", false)
    )

    @Before
    fun setup() {
        mockRepository = mock(IAlphabetRepository::class.java)
        useCase = GetAlphabetUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runBlocking {
        `when`(mockRepository.getAllAlphabets()).thenReturn(flowOf(testAlphabets))

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals('A', result[0].letter)
        assertEquals('B', result[1].letter)
        verify(mockRepository, times(1)).getAllAlphabets()
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runBlocking {
        `when`(mockRepository.getAllAlphabets()).thenReturn(flowOf(emptyList()))

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }
}
