package com.nirkids.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import com.nirkids.app.domain.usecase.MarkLetterLearnedUseCase
import com.nirkids.tracevalidator.TraceValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlphabetUiState(
    val selectedLetter: Alphabet? = null,
    val allLetters: List<Alphabet> = emptyList(),
    val currentProgress: LetterProgress? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val markLetterLearnedUseCase: MarkLetterLearnedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlphabetUiState())
    val uiState: StateFlow<AlphabetUiState> = _uiState

    init {
        loadAllLetters()
    }

    private fun loadAllLetters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAlphabetUseCase().onStart {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }.catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }.collect { alphabets ->
                _uiState.value = _uiState.value.copy(
                    allLetters = alphabets,
                    isLoading = false
                )
                TraceValidator.logEvent("letters_loaded", mapOf("count" to alphabets.size.toString()))
            }
        }
    }

    fun selectLetter(letter: Char) {
        viewModelScope.launch {
            getAlphabetUseCase().collect { alphabets ->
                val selected = alphabets.find { it.letter == letter }
                selected?.let { alpha ->
                    _uiState.value = _uiState.value.copy(
                        selectedLetter = alpha,
                        isLoading = true
                    )
                }
            }
            getProgressUseCase().collect { progresses ->
                val progress = progresses.find { it.letter == letter }
                _uiState.value = _uiState.value.copy(
                    currentProgress = progress,
                    isLoading = false
                )
            }
        }
    }

    suspend fun markAsLearned(letter: Char) {
        markLetterLearnedUseCase(letter)
        TraceValidator.logEvent("letter_learned", mapOf("letter" to letter.toString()))
        loadAllLetters()
        // Reload progress
        getProgressUseCase().collect { progresses ->
            val progress = progresses.find { it.letter == letter }
            _uiState.value = _uiState.value.copy(currentProgress = progress)
        }
    }

    fun playPronunciation() {
        _uiState.value.selectedLetter?.let {
            _uiState.value = _uiState.value.copy(isPlaying = true)
            // TTS will be handled by TtsHelper injected in screen
        }
    }

    fun dismissPlaying() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }
}
