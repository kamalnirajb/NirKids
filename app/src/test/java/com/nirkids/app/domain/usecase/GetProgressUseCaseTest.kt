package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetProgressUseCaseTest {

    private lateinit var useCase: GetProgressUseCase
    private lateinit var mockRepository: IAlphabetRepository

    private val testProgress = listOf(
        LetterProgress('A', learned = true, attempts = 3, masteryLevel = 2),
        LetterProgress('B', learned = false, attempts = 1, masteryLevel = 1)
    )

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetProgressUseCase(mockRepository)
    }

    @Test
    fun `invoke returns flow from repository`() = runBlocking {
        every { mockRepository.getAllProgress() } returns flowOf(testProgress)

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals('A', result[0].letter)
        assertTrue(result[0].learned)
    }
}
