package com.nirkids.app.ui.main.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import com.nirkids.app.domain.usecase.MarkLetterLearnedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
        mockGetAlphabetUseCase = mock(GetAlphabetUseCase::class.java)
        mockGetProgressUseCase = mock(GetProgressUseCase::class.java)
        mockMarkLetterLearnedUseCase = mock(MarkLetterLearnedUseCase::class.java)

        `when`(mockGetAlphabetUseCase()).thenReturn(flowOf(
            listOf(
                Alphabet('A', '/æ/', "Apple", "🍎", true),
                Alphabet('B', '/b/', "Ball", "⚽", false)
            )
        ))
        `when`(mockGetProgressUseCase()).thenReturn(flowOf(
            listOf(
                LetterProgress('A', learned = true, 3, 2),
                LetterProgress('B', learned = false, 1, 1)
            )
        ))

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
    fun `init loads all letters and progress`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.allLetters.size)
        assertEquals('A', state.allLetters[0].letter)
    }

    @Test
    fun `selectLetter sets selected letter`() = runTest(testDispatcher) {
        viewModel.selectLetter('A')
        val state = viewModel.uiState.value
        assertNotNull(state.selectedLetter)
        assertEquals('A', state.selectedLetter!!.letter)
    }

    @Test
    fun `selectLetter sets current progress`() = runTest(testDispatcher) {
        viewModel.selectLetter('B')
        val state = viewModel.uiState.value
        assertNotNull(state.currentProgress)
        assertEquals('B', state.currentProgress!!.letter)
        assertFalse(state.currentProgress!!.learned)
    }

    @Test
    fun `playPronunciation sets isPlaying true`() = runTest(testDispatcher) {
        viewModel.selectLetter('A')
        viewModel.playPronunciation()
        val state = viewModel.uiState.value
        assertTrue(state.isPlaying)
    }

    @Test
    fun `dismissPlaying sets isPlaying false`() = runTest(testDispatcher) {
        viewModel.selectLetter('A')
        viewModel.playPronunciation()
        viewModel.dismissPlaying()
        val state = viewModel.uiState.value
        assertFalse(state.isPlaying)
    }
}
