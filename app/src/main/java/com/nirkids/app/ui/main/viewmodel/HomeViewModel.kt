package com.nirkids.app.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nirkids.app.domain.model.LetterProgress
import com.nirkids.app.domain.usecase.GetAlphabetUseCase
import com.nirkids.app.domain.usecase.GetProgressUseCase
import com.nirkids.tracevalidator.TraceValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalLetters: Int = 0,
    val learnedCount: Int = 0,
    val inProgressCount: Int = 0,
    val newCount: Int = 0,
    val allLetters: List<com.nirkids.app.domain.model.Alphabet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val seedDataUseCase: com.nirkids.app.domain.usecase.SeedDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            seedDataUseCase()
            loadStats()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAlphabetUseCase().onStart {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }.catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }.collect { alphabets ->
                getProgressUseCase().collect { progresses ->
                    val learned = progresses.count { it.learned }
                    val inProgress = progresses.count { !it.learned && it.masteryLevel > 0 }
                    val new = progresses.count { !it.learned && it.masteryLevel == 0 }
                    _uiState.value = _uiState.value.copy(
                        totalLetters = alphabets.size,
                        learnedCount = learned,
                        inProgressCount = inProgress,
                        newCount = new,
                        allLetters = alphabets,
                        isLoading = false
                    )
                    TraceValidator.logEvent("home_stats_loaded", mapOf(
                        "total" to alphabets.size.toString(),
                        "learned" to learned.toString(),
                        "in_progress" to inProgress.toString()
                    ))
                }
            }
        }
    }

    fun refreshStats() {
        loadStats()
    }
}
