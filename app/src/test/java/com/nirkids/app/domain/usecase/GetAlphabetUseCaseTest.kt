package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetAlphabetUseCaseTest {

    private lateinit var useCase: GetAlphabetUseCase
    private lateinit var mockRepository: IAlphabetRepository

    private val testAlphabets = listOf(
        Alphabet('A', "/æ/", "Apple", "🍎", true),
        Alphabet('B', "/b/", "Ball", "⚽", false)
    )

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetAlphabetUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runBlocking {
        every { mockRepository.getAllAlphabets() } returns flowOf(testAlphabets)

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals('A', result[0].letter)
        assertEquals('B', result[1].letter)
    }
}
