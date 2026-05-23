package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetRandomLetterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PronunciationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var mockGetRandomLetterUseCase: GetRandomLetterUseCase
    private lateinit var viewModel: PronunciationViewModel

    private val testLetter = Alphabet('A', '/æ/', "Apple", "🍎", true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mock(GetAlphabetUseCase::class.java)
        mockGetRandomLetterUseCase = mock(GetRandomLetterUseCase::class.java)

        `when`(mockGetRandomLetterUseCase()).thenReturn(testLetter)

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
        assertFalse(state.phoneticBreakdown.isEmpty())
    }

    @Test
    fun `loadNextLetter loads new letter`() = runTest(testDispatcher) {
        val newLetter = Alphabet('B', '/b/', "Ball", "⚽", false)
        `when`(mockGetRandomLetterUseCase()).thenReturn(newLetter)

        viewModel.loadNextLetter()
        val state = viewModel.uiState.value
        assertNotNull(state.currentLetter)
        assertEquals('B', state.currentLetter!!.letter)
        assertFalse(state.isLoading)
    }

    @Test
    fun `checkPronunciation returns SUCCESS for correct letter`() = runTest(testDispatcher) {
        val result = viewModel.checkPronunciation("a")
        assertEquals(FeedbackType.SUCCESS, result)
    }

    @Test
    fun `checkPronunciation returns SUCCESS for matching first char`() = runTest(testDispatcher) {
        val result = viewModel.checkPronunciation("ap")
        assertEquals(FeedbackType.SUCCESS, result)
    }

    @Test
    fun `checkPronunciation returns TRY_AGAIN for wrong input`() = runTest(testDispatcher) {
        val result = viewModel.checkPronunciation("xyz")
        assertEquals(FeedbackType.TRY_AGAIN, result)
    }

    @Test
    fun `checkPronunciation is case insensitive`() = runTest(testDispatcher) {
        val result = viewModel.checkPronunciation("A")
        assertEquals(FeedbackType.SUCCESS, result)
    }

    @Test
    fun `checkPronunciation trims whitespace`() = runTest(testDispatcher) {
        val result = viewModel.checkPronunciation("  a  ")
        assertEquals(FeedbackType.SUCCESS, result)
    }

    @Test
    fun `checkPronunciation returns NONE for null letter`() = runTest(testDispatcher) {
        viewModel.uiState.value = viewModel.uiState.value.copy(currentLetter = null)
        val result = viewModel.checkPronunciation("a")
        assertEquals(FeedbackType.NONE, result)
    }

    @Test
    fun `clearFeedback clears feedback`() = runTest(testDispatcher) {
        viewModel.uiState.value = viewModel.uiState.value.copy(
            feedbackMessage = "test",
            feedbackType = FeedbackType.SUCCESS
        )
        viewModel.clearFeedback()

        val state = viewModel.uiState.value
        assertNull(state.feedbackMessage)
        assertEquals(FeedbackType.NONE, state.feedbackType)
    }

    @Test
    fun `togglePracticeMode toggles mode`() = runTest(testDispatcher) {
        assertFalse(viewModel.uiState.value.isPracticeMode)
        viewModel.togglePracticeMode()
        assertTrue(viewModel.uiState.value.isPracticeMode)
        viewModel.togglePracticeMode()
        assertFalse(viewModel.uiState.value.isPracticeMode)
    }
}
