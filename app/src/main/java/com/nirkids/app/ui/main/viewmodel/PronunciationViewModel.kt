package com.nirkids.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetRandomLetterUseCase
import com.nirkids.tracevalidator.TraceValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PronunciationUiState(
    val currentLetter: Alphabet? = null,
    val phoneticBreakdown: List<String> = emptyList(),
    val isPracticeMode: Boolean = false,
    val userInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedbackMessage: String? = null,
    val feedbackType: FeedbackType = FeedbackType.NONE
)

enum class FeedbackType {
    NONE, SUCCESS, TRY_AGAIN, GREAT
}

@HiltViewModel
class PronunciationViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getRandomLetterUseCase: GetRandomLetterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PronunciationUiState())
    val uiState: StateFlow<PronunciationUiState> = _uiState

    init {
        loadNextLetter()
    }

    fun loadNextLetter() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, feedbackMessage = null, feedbackType = FeedbackType.NONE)
            val letter = getRandomLetterUseCase()
            if (letter != null) {
                _uiState.value = _uiState.value.copy(
                    currentLetter = letter,
                    phoneticBreakdown = letter.phoneticBreakdown,
                    isLoading = false,
                    userInput = ""
                )
            }
            TraceValidator.logEvent("pronunciation_letter_shown", mapOf("letter" to (letter?.letter?.toString() ?: "none")))
        }
    }

    fun checkPronunciation(userInput: String): FeedbackType {
        val letter = _uiState.value.currentLetter ?: return FeedbackType.NONE
        val cleanInput = userInput.trim().lowercase()
        val expectedPhonetic = letter.phonetic.replace("/", "").trim().lowercase()
        val expectedLetter = letter.letter.lowercase()

        return if (cleanInput == expectedLetter || cleanInput.startsWith(expectedLetter)) {
            TraceValidator.logEvent("pronunciation_correct", mapOf("letter" to letter.letter.toString()))
            _uiState.value.copy(
                feedbackMessage = if (cleanInput == expectedLetter) "Perfect! 🎉" else "Great start! ✨",
                feedbackType = if (cleanInput == expectedLetter) FeedbackType.GREAT else FeedbackType.SUCCESS
            ).let {
                _uiState.value = it
                it.feedbackType
            }
        } else {
            TraceValidator.logEvent("pronunciation_incorrect", mapOf("letter" to letter.letter.toString()))
            _uiState.value.copy(
                feedbackMessage = "Try again! Think of $letter.exampleWord — $letter.exampleWord starts with $letter. 💪",
                feedbackType = FeedbackType.TRY_AGAIN
            ).let {
                _uiState.value = it
                it.feedbackType
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null, feedbackType = FeedbackType.NONE)
    }

    fun togglePracticeMode() {
        _uiState.value = _uiState.value.copy(isPracticeMode = !_uiState.value.isPracticeMode)
    }
}
