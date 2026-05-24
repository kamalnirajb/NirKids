# Layer 02 — MVVM Pattern: ViewModel as the Bridge

> MVVM (Model-View-ViewModel) separates UI logic from presentation. ViewModels hold UI state and expose it reactively. Views observe state and dispatch user actions.

---

## What Is MVVM?

MVVM stands for **Model-View-ViewModel**. It's the standard architecture pattern for Android apps.

```
┌──────────┐         observes         ┌───────────┐
│  VIEW    │  ←─────────────────────  │  VIEWMODEL│
│ (Compose)│    state flows down      │ (State)   │
└────┬─────┘                          └────┬──────┘
     │                                      │
     │  user actions go up                 │ invokes
     │                                      ▼
     │                          ┌─────────────────┐
     └─────────────────────────▶│  MODELS/USE CASES│
                                │  (Domain Layer)  │
                                └─────────────────┘
```

### The Three Roles

| Role | What it does | In NirKids |
|---|-|---|
| **Model** | Data + business logic | `Alphabet`, `LetterProgress`, UseCases, Repository |
| **View** | UI display + user input | Compose screens (`HomeScreen`, `AlphabetScreen`) |
| **ViewModel** | State management + navigation logic | `HomeViewModel`, `AlphabetViewModel`, etc. |

---

## ViewModel Anatomy — The NirKids Blueprint

Every ViewModel in NirKids follows the same structure. Let's examine `HomeViewModel` as the template:

```kotlin
// File: com/nirkids/app/ui/main/viewmodel/HomeViewModel.kt

// ═══════════════════════════════════════════════════
// STEP 1: Define UI State
// ═══════════════════════════════════════════════════
// This data class describes EVERYTHING the UI needs to display.
// When this changes, the UI automatically updates.
data class HomeUiState(
    val totalLetters: Int = 0,
    val learnedCount: Int = 0,
    val inProgressCount: Int = 0,
    val newCount: Int = 0,
    val allLetters: List<Alphabet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ═══════════════════════════════════════════════════
// STEP 2: Create ViewModel with Injected Dependencies
// ═══════════════════════════════════════════════════
// - @HiltViewModel + @Inject constructor → Hilt DI
// - Only depends on USE CASES (domain layer interfaces)
// - Never depends on Repository directly
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val seedDataUseCase: SeedDataUseCase
) : ViewModel() {

    // ═══════════════════════════════════════════════════
    // STEP 3: Create MutableStateFlow for State
    // ═══════════════════════════════════════════════════
    // Private mutable flow (only ViewModel can modify)
    private val _uiState = MutableStateFlow(HomeUiState())

    // Public immutable flow (UI can only observe)
    val uiState: StateFlow<HomeUiState> = _uiState

    // ═══════════════════════════════════════════════════
    // STEP 4: Initialize in init block
    // ═══════════════════════════════════════════════════
    init {
        viewModelScope.launch {
            seedDataUseCase()   // seed database on first launch
            loadStats()         // load initial stats
        }
    }

    // ═══════════════════════════════════════════════════
    // STEP 5: Load data — the ViewModel's job is to
    //         orchestrate use cases and update state
    // ═══════════════════════════════════════════════════
    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Chain Flows with onStart/catch for error handling
            getAlphabetUseCase()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { alphabets ->
                    // Nested collect — get progress after alphabets load
                    getProgressUseCase().collect { progresses ->
                        // Calculate derived state
                        val learned = progresses.count { it.learned }
                        val inProgress = progresses.count { !it.learned && it.masteryLevel > 0 }
                        val new = progresses.count { !it.learned && it.masteryLevel == 0 }

                        // UPDATE STATE — this triggers UI recomposition
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
                            "learned" to learned.toString()
                        ))
                    }
                }
        }
    }

    // ═══════════════════════════════════════════════════
    // STEP 6: Public functions for user actions
    // ═══════════════════════════════════════════════════
    fun refreshStats() {
        loadStats()
    }
}
```

---

## State Management Patterns

### Pattern 1: Loading → Success/Error

```kotlin
// HomeViewModel uses this pattern everywhere
_getAlphabetUseCase()
    .onStart {
        _uiState.value = _uiState.copy(isLoading = true, error = null)
    }
    .catch { e ->
        _uiState.value = _uiState.copy(isLoading = false, error = e.message)
    }
    .collect { data ->
        _uiState.value = _uiState.copy(data = data, isLoading = false)
    }
```

### Pattern 2: Simple Boolean State Toggle

```kotlin
// AlphabetViewModel — play pronunciation
fun playPronunciation() {
    _uiState.value.selectedLetter?.let {
        _uiState.value = _uiState.copy(isPlaying = true)
    }
}

fun dismissPlaying() {
    _uiState.value = _uiState.copy(isPlaying = false)
}
```

### Pattern 3: Computed State

```kotlin
// PronunciationViewModel — compute feedback based on input
fun checkPronunciation(userInput: String): FeedbackType {
    val letter = _uiState.value.currentLetter ?: return FeedbackType.NONE
    val cleanInput = userInput.trim().lowercase()
    val expected = letter.letter.lowercase()

    return if (cleanInput == expected) {
        // Perfect match
        _uiState.value.copy(
            feedbackMessage = "Perfect! 🎉",
            feedbackType = FeedbackType.GREAT
        ).let { _uiState.value = it; it.feedbackType }
    } else if (cleanInput.startsWith(expected)) {
        // Partial match
        _uiState.value.copy(
            feedbackMessage = "Great start! ✨",
            feedbackType = FeedbackType.SUCCESS
        ).let { _uiState.value = it; it.feedbackType }
    } else {
        // No match
        _uiState.value.copy(
            feedbackMessage = "Try again! 💪",
            feedbackType = FeedbackType.TRY_AGAIN
        ).let { _uiState.value = it; it.feedbackType }
    }
}
```

### Pattern 4: Sealed Class for Result Types

```kotlin
// Domain model used as a generic result wrapper
// File: com/nirkids/app/domain/model/Resource.kt
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

**Use this pattern when you need to represent one of several mutually exclusive states.** In NirKids, `Resource` is the domain model. The ViewModels use inline `isLoading`/`error` fields instead of wrapping everything in `Resource` — both approaches are valid.

---

## ViewModel-to-Screen Communication

### State Flows Down, Actions Flow Up

```
┌──────────────────────────────────────────────────────────────┐
│                    HomeViewModel                             │
│                                                              │
│  _uiState (MutableStateFlow)  ← ViewModel modifies           │
│  uiState (StateFlow)          → UI observes                │
│                                                              │
│  loadStats()                  → ViewModel internal         │
│  refreshStats()               → called by UI (public)      │
└──────────────────────────────┬───────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   UI (Compose)       │
                    │                      │
│  HomeScreenContent(    │
│    uiState = uiState,  │  ← observe state
│    onNavigateAlphabet = { nav(...) }   │  ← callback
│    onNavigatePronunciation = { ... }   │  ← callback
│    onNavigateParentGate = { ... }      │  ← callback
│  )                     │
└──────────────────────────────────────────────────────────────┘
```

### State Hoisting Pattern

NirKids uses a two-level Composable pattern:

```kotlin
// ══════════════════════════════════════════════════════════
// LEVEL 1: "Screen" Composable — handles ViewModel integration
// ══════════════════════════════════════════════════════════
@Composable
fun HomeScreen(
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()  // Hilt auto-injects
) {
    val uiState by viewModel.uiState.collectAsState()  // subscribe to state

    // Delegate to pure content Composable
    HomeScreenContent(
        uiState = uiState,
        onNavigateToAlphabet = onNavigateToAlphabet,
        onNavigateToPronunciation = onNavigateToPronunciation,
        onNavigateToParentGate = onNavigateToParentGate
    )
}

// ══════════════════════════════════════════════════════════
// LEVEL 2: "Content" Composable — pure UI, testable without ViewModel
// ══════════════════════════════════════════════════════════
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,  // ← plain data class, no ViewModel dependency
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 NirKids") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                // Display stats, progress, buttons using uiState
                StatCard(label = "Learned ✅", value = uiState.learnedCount)
                StatCard(label = "Total 📊", value = uiState.totalLetters)
            }
        }
    }
}
```

**Why two levels?**
- `HomeScreen` handles `hiltViewModel()` and `collectAsState()` (framework concerns)
- `HomeScreenContent` is a pure function — given state, it renders deterministically
- `HomeScreenContent` can be tested independently of ViewModel
- `HomeScreenContent` can be previewed in Android Studio with fake state

---

## ViewModel Comparison — NirKids Has Four Patterns

### Pattern A: Stats + Aggregation (`HomeViewModel`)

```kotlin
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val seedDataUseCase: SeedDataUseCase
) : ViewModel() {
    // Collects TWO flows and computes derived stats
    // State: total counts (learned, inProgress, new)
}
```

### Pattern B: Item Selection (`AlphabetViewModel`)

```kotlin
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val markLetterLearnedUseCase: MarkLetterLearnedUseCase
) : ViewModel() {
    // Manages selected item + list state
    // State: selectedLetter, allLetters, currentProgress
    fun selectLetter(letter: Char) { ... }
    suspend fun markAsLearned(letter: Char) { ... }
}
```

### Pattern C: User Interaction (`PronunciationViewModel`)

```kotlin
class PronunciationViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getRandomLetterUseCase: GetRandomLetterUseCase
) : ViewModel() {
    // Handles user input and provides feedback
    // State: currentLetter, phoneticBreakdown, userInput, feedback
    fun checkPronunciation(userInput: String): FeedbackType { ... }
    fun loadNextLetter() { ... }
}
```

### Pattern D: Business Logic Wrapper (`ParentGateViewModel`)

```kotlin
class ParentGateViewModel @Inject constructor(
    private val parentGateUseCase: ParentGateValidateUseCase
) : ViewModel() {
    // Wraps pure use case logic with UI state
    // State: gateState, userInput, errorMessage, isSuccess
    fun submitAnswer(answer: String) { ... }
    fun verifyPin(pin: String) { ... }
    fun generateNewQuestion() { ... }
}
```

---

## ViewModel Lifecycle in MVVM

```
┌─────────────────────────────────────────────┐
│            Activity Lifecycle                │
│                                              │
│  onCreate() ──▶ ViewModel created            │
│                ──▶ init block runs           │
│                ──▶ loadStats() starts        │
│                                              │
│  onConfigurationChanged ──▶ ViewModel KEPT   │
│                ──▶ flow keeps emitting       │
│                ──▶ UI recomposes with new    │
│                   state                      │
│                                              │
│  onDestroy() ──▶ ViewModel survives           │
│                ──▶ survives config changes   │
│                ──▶ killed when Activity dies │
└─────────────────────────────────────────────┘
```

**Key insight:** ViewModels survive configuration changes (rotation, keyboard, etc.) but are killed when the owning Activity/Fragment is destroyed. This is why they're perfect for holding UI state — you don't need `onSaveInstanceState` or `savedInstanceState`.

---

## Common MVVM Mistakes in NirKids-Style Code

❌ **Direct Repository access in ViewModel**

```kotlin
// BAD
class HomeViewModel : ViewModel() {
    private val db = Room.databaseBuilder(...)
    private val dao = db.alphabetDao()
}
```

✅ **Use UseCases**

```kotlin
// GOOD
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase
) : ViewModel()
```

❌ **Mutable state in Compose that bypasses ViewModel**

```kotlin
// BAD — direct DB read in Composable
@Composable
fun SomeScreen() {
    var data by remember { mutableStateOf(emptyList()) }
    LaunchedEffect(Unit) {
        data = realRepo.getAll()  // ViewModel should do this!
    }
}
```

✅ **All state goes through ViewModel**

```kotlin
// GOOD
@Composable
fun SomeScreen(viewModel: SomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Text(state.items.firstOrNull()?.name ?: "Loading...")
}
```

---

*Next: [Layer 03 — Jetpack Compose](./layer-03-jetpack-compose.md)*
