package com.nirkids.app.domain.usecase

import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetRandomLetterUseCaseTest {

    private lateinit var useCase: GetRandomLetterUseCase
    private lateinit var repository: IAlphabetRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetRandomLetterUseCase(repository)
    }

    @Test
    fun `invoke returns random alphabet from list`() = runBlocking {
        val testLetters = listOf(
            Alphabet('A', "/æ/", "Apple", "🍎", true),
            Alphabet('B', "/b/", "Ball", "⚽", false),
            Alphabet('C', "/k/", "Cat", "🐱", false)
        )
        every { repository.getAllAlphabets() } returns flowOf(testLetters)

        val result = useCase()

        assertNotNull(result)
        assertTrue(testLetters.contains(result))
    }

    @Test
    fun `invoke returns null when list is empty`() = runBlocking {
        every { repository.getAllAlphabets() } returns flowOf(emptyList())

        val result = useCase()

        assertNull(result)
    }
}
