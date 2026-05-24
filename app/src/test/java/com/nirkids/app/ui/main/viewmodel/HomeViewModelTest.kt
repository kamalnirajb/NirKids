package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import com.nirkids.app.domain.usecase.SeedDataUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var mockGetProgressUseCase: GetProgressUseCase
    private lateinit var mockSeedDataUseCase: SeedDataUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mockk()
        mockGetProgressUseCase = mockk()
        mockSeedDataUseCase = mockk()

        every { mockGetAlphabetUseCase() } returns flowOf(
            listOf(
                Alphabet('A', "/æ/", "Apple", "🍎", true),
                Alphabet('B', "/b/", "Ball", "⚽", false),
                Alphabet('C', "/k/", "Cat", "🐱", false),
                Alphabet('D', "/d/", "Dog", "🐶", false),
                Alphabet('E', "/i/", "Egg", "🥚", true)
            )
        )
        every { mockGetProgressUseCase() } returns flowOf(
            listOf(
                LetterProgress('A', learned = true, 3, 2),
                LetterProgress('B', learned = true, 2, 1),
                LetterProgress('C', learned = false, 1, 1),
                LetterProgress('D', learned = false, 0, 0),
                LetterProgress('E', learned = false, 0, 0)
            )
        )
        coEvery { mockSeedDataUseCase() } returns Unit

        viewModel = HomeViewModel(mockGetAlphabetUseCase, mockGetProgressUseCase, mockSeedDataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls seedDataUseCase and loads stats correctly`() = runTest(testDispatcher) {
        coVerify(exactly = 1) { mockSeedDataUseCase() }
        
        val state = viewModel.uiState.value
        assertEquals(5, state.totalLetters)
        assertEquals(2, state.learnedCount)
        assertEquals(1, state.inProgressCount)
        assertEquals(2, state.newCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `refreshStats reloads data`() = runTest(testDispatcher) {
        viewModel.refreshStats()
        val state = viewModel.uiState.value
        assertEquals(5, state.totalLetters)
        verify(exactly = 2) { mockGetAlphabetUseCase() }
    }

    @Test
    fun `home stats with all learned`() = runTest(testDispatcher) {
        every { mockGetProgressUseCase() } returns flowOf(
            (65..69).map { LetterProgress(it.toChar(), learned = true, 5, 3) }
        )

        viewModel.refreshStats()
        val state = viewModel.uiState.value
        assertEquals(5, state.learnedCount)
        assertEquals(5, state.totalLetters)
        assertEquals(0, state.newCount)
    }
}
