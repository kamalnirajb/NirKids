package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetRandomLetterUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PronunciationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var mockGetRandomLetterUseCase: GetRandomLetterUseCase
    private lateinit var viewModel: PronunciationViewModel

    private val testLetter = Alphabet('A', "/æ/", "Apple", "🍎", true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mockk()
        mockGetRandomLetterUseCase = mockk()

        coEvery { mockGetRandomLetterUseCase() } returns testLetter

        viewModel = PronunciationViewModel(mockGetAlphabetUseCase, mockGetRandomLetterUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads a letter`() {
        val state = viewModel.uiState.value
        assertNotNull(state.currentLetter)
        assertEquals('A', state.currentLetter!!.letter)
    }

    @Test
    fun `loadNextLetter loads new letter`() = runTest(testDispatcher) {
        val newLetter = Alphabet('B', "/b/", "Ball", "⚽", false)
        coEvery { mockGetRandomLetterUseCase() } returns newLetter

        viewModel.loadNextLetter()
        val state = viewModel.uiState.value
        assertNotNull(state.currentLetter)
        assertEquals('B', state.currentLetter!!.letter)
    }

    @Test
    fun `checkPronunciation returns GREAT for perfect match`() {
        val result = viewModel.checkPronunciation("a")
        assertEquals(FeedbackType.GREAT, result)
    }

    @Test
    fun `checkPronunciation returns TRY_AGAIN for wrong input`() {
        val result = viewModel.checkPronunciation("xyz")
        assertEquals(FeedbackType.TRY_AGAIN, result)
    }

    @Test
    fun `togglePracticeMode toggles mode`() {
        assertFalse(viewModel.uiState.value.isPracticeMode)
        viewModel.togglePracticeMode()
        assertTrue(viewModel.uiState.value.isPracticeMode)
    }
}
