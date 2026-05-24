package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import com.nirkids.app.domain.usecase.MarkLetterLearnedUseCase
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
class AlphabetViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var mockGetProgressUseCase: GetProgressUseCase
    private lateinit var mockMarkLetterLearnedUseCase: MarkLetterLearnedUseCase
    private lateinit var viewModel: AlphabetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mockk()
        mockGetProgressUseCase = mockk()
        mockMarkLetterLearnedUseCase = mockk()

        every { mockGetAlphabetUseCase() } returns flowOf(
            listOf(
                Alphabet('A', "/æ/", "Apple", "🍎", true),
                Alphabet('B', "/b/", "Ball", "⚽", false)
            )
        )
        every { mockGetProgressUseCase() } returns flowOf(
            listOf(
                LetterProgress('A', learned = true, 3, 2),
                LetterProgress('B', learned = false, 1, 1)
            )
        )

        viewModel = AlphabetViewModel(
            mockGetAlphabetUseCase,
            mockGetProgressUseCase,
            mockMarkLetterLearnedUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads all letters and progress`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.allLetters.size)
        assertEquals('A', state.allLetters[0].letter)
    }

    @Test
    fun `selectLetter sets selected letter`() = runTest {
        viewModel.selectLetter('A')
        val state = viewModel.uiState.value
        assertNotNull(state.selectedLetter)
        assertEquals('A', state.selectedLetter!!.letter)
    }

    @Test
    fun `markAsLearned calls use case and reloads`() = runTest {
        coEvery { mockMarkLetterLearnedUseCase('B') } returns Unit
        
        viewModel.markAsLearned('B')
        
        coVerify(exactly = 1) { mockMarkLetterLearnedUseCase('B') }
    }
}
