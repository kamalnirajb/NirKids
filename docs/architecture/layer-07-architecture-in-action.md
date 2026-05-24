# Layer 07 — Architecture in Action: Complete Feature Walkthrough

> This document traces a **complete feature** through all layers of NirKids. Every file, every method call, every data transformation. Follow along and see how Clean Architecture + MVVM + Compose + Hilt work together.

---

## The Feature: "Tap a Letter on Home → View Letter Details"

This single user action flows through **every** layer. Let's trace it step by step.

---

## Step 1: User Taps Letter Card (UI Layer — Compose)

**File:** `HomeScreen.kt`
**Layer:** UI (Compose)

```kotlin
// ════ USER INTERACTION — what the user does ════
User taps the "A" card

// ════ COMPOSE CODE — the composable ════
@Composable
fun LetterMiniCard(
    letter: Char,
    color: Color,
    onClick: () -> Unit   // ← callback parameter
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(BorderStroke(2.dp, color.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),  // ← onClick is called when tapped
        contentAlignment = Alignment.Center
    ) {
        Text(letter.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
```

**What happens:**
1. User taps the card
2. Compose calls `onClick` callback
3. This triggers navigation to the Alphabet screen

---

## Step 2: Navigate to Alphabet Screen (Navigation)

**File:** `NavGraph.kt`
**Layer:** UI (Navigation)

```kotlin
// In HomeScreen composable:
HomeScreen(
    onNavigateToAlphabet = { navController.navigate(NavDestinations.ALPHABET) },
    // ...
)

// In NavGraph:
composable(NavDestinations.ALPHABET) {
    AlphabetScreen(
        onNavigateBack = { navController.popBackStack() },
        ttsHelper = ttsHelper,
        vibrationHelper = vibrationHelper
    )
}
```

**What happens:**
1. NavHost pushes `AlphabetScreen` onto the back stack
2. `AlphabetScreen` is created as a Composable
3. `hiltViewModel()` creates (or retrieves) the `AlphabetViewModel`

---

## Step 3: ViewModel Initializes (ViewModel Layer)

**File:** `AlphabetViewModel.kt`
**Layer:** ViewModel

```kotlin
@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,   // ← Hilt injects
    private val getProgressUseCase: GetProgressUseCase,   // ← Hilt injects
    private val markLetterLearnedUseCase: MarkLetterLearnedUseCase  // ← Hilt injects
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlphabetUiState())
    val uiState: StateFlow<AlphabetUiState> = _uiState

    init {
        loadAllLetters()  // ← called immediately when ViewModel is created
    }

    private fun loadAllLetters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Call UseCase → goes to Domain Layer
            getAlphabetUseCase()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
                .collect { alphabets ->
                    _uiState.value = _uiState.value.copy(
                        allLetters = alphabets,  // ← state update
                        isLoading = false
                    )
                    TraceValidator.logEvent("letters_loaded", mapOf("count" to alphabets.size.toString()))
                }
        }
    }

    // ════ THE KEY METHOD — user taps a letter ════
    fun selectLetter(letter: Char) {
        viewModelScope.launch {
            // Get the selected letter
            getAlphabetUseCase().collect { alphabets ->
                val selected = alphabets.find { it.letter == letter }
                selected?.let { alpha ->
                    _uiState.value = _uiState.value.copy(
                        selectedLetter = alpha,  // ← update state
                        isLoading = true
                    )
                }
            }

            // Get progress for this letter
            getProgressUseCase().collect { progresses ->
                val progress = progresses.find { it.letter == letter }
                _uiState.value = _uiState.value.copy(
                    currentProgress = progress,  // ← update state
                    isLoading = false
                )
            }
        }
    }

    // ════ ANOTHER KEY METHOD — mark as learned ════
    suspend fun markAsLearned(letter: Char) {
        markLetterLearnedUseCase(letter)  // ← call UseCase
        TraceValidator.logEvent("letter_learned", mapOf("letter" to letter.toString()))
        loadAllLetters()  // ← refresh
        getProgressUseCase().collect { progresses ->
            val progress = progresses.find { it.letter == letter }
            _uiState.value = _uiState.value.copy(currentProgress = progress)
        }
    }
}
```

**Key observations:**
- ViewModel **never** creates repositories or databases
- ViewModel **only** uses UseCase interfaces (from Domain Layer)
- State changes (`_uiState.value = ...`) trigger Compose recomposition
- `viewModelScope` ensures coroutines survive config changes

---

## Step 4: UseCase Orchestrates (Domain Layer)

**File:** `GetAlphabetUseCase.kt`
**Layer:** Domain

```kotlin
class GetAlphabetUseCase(
    private val repository: IAlphabetRepository  // ← interface, not implementation
) {
    operator fun invoke(): Flow<List<Alphabet>> {
        return repository.getAllAlphabets()  // ← delegates to repository
    }
}
```

**This is a pass-through UseCase.** Some UseCases do more work — let's see `MarkLetterLearnedUseCase`:

```kotlin
class MarkLetterLearnedUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(letter: Char) {
        // Business logic: mark learned AND increment attempts
        repository.markLetterLearned(letter)
        repository.incrementAttempts(letter)
    }
}
```

**What the UseCase adds:** It encapsulates the business rule "when a letter is marked learned, also count the attempt." The ViewModel doesn't know about this rule — it just calls one function.

---

## Step 5: Repository Retrieves Data (Data Layer)

**File:** `AlphabetRepository.kt`
**Layer:** Data (Room)

```kotlin
class AlphabetRepositoryImpl(
    private val database: AlphabetsDatabase
) : IAlphabetRepository {

    private val alphabetDao = database.alphabetDao()
    private val progressDao = database.progressDao()

    // ← Flow that emits when database changes
    override fun getAllAlphabets(): Flow<List<Alphabet>> {
        return alphabetDao.getAllAlphabets()  // Room: Flow<List<AlphabetEntity>>
            .map { entities ->
                entities.map { it.toDomainModel() }  // ← conversion
            }
    }

    override suspend fun getAlphabetByLetter(letter: Char): Alphabet? {
        return alphabetDao.getAlphabetByLetter(letter.toString())
            ?.toDomainModel()
    }

    override fun getAllProgress(): Flow<List<LetterProgress>> {
        return progressDao.getAllProgress()
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }

    override suspend fun markLetterLearned(letter: Char) {
        alphabetDao.getAlphabetByLetter(letter.toString())?.let {
            progressDao.updateProgress(letter.toString(), learned = true)
        }
    }

    override suspend fun incrementAttempts(letter: Char) {
        progressDao.getProgressForLetter(letter.toString())?.let {
            progressDao.updateMasteryLevel(letter.toString(), it.masteryLevel + 1)
        }
    }
}
```

---

## Step 6: Room Queries the Database

**File:** `AlphabetDao.kt`
**Layer:** Data (Room)

```kotlin
@Dao
interface AlphabetDao {
    @Query("SELECT * FROM alphabet ORDER BY letter ASC")
    fun getAllAlphabets(): Flow<List<AlphabetEntity>>

    @Query("SELECT * FROM alphabet WHERE letter = :letter")
    suspend fun getAlphabetByLetter(letter: String): AlphabetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlphabets(alphabets: List<AlphabetEntity>)

    @Query("DELETE FROM alphabet")
    suspend fun deleteAllAlphabets()
}
```

---

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USER ACTION: "Tap A card"                    │
└─────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
╔═════════════════════════════════════════════════════════════════════╗
║  UI LAYER (Compose)                                                  ═════════
║                                                                     ║
║  LetterMiniCard(letter='A')                                          ║
║    └── .clickable { onClick() }                                     ║
║      └── HomeScreen.onNavigateToAlphabet()                          ║
║        └── NavGraph.navigate("alphabet")                            ║
║          └── AlphabetScreen composable created                      ║
║            └── hiltViewModel<AlphabetViewModel>()  ← Hilt DI       ║
║              └── AlphabetViewModel init { loadAllLetters() }        ║
╚═════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
╔═════════════════════════════════════════════════════════════════════╗
║  VIEWMODEL LAYER                                                     ═════════
║                                                                     ║
║  AlphabetViewModel.loadAllLetters()                                  ║
║    └── viewModelScope.launch {                                      ║
║          getAlphabetUseCase().collect { alphabets →                 ║
║            _uiState.copy(allLetters = alphabets)  ← STATE CHANGE    ║
║          }                                                           ║
║          getProgressUseCase().collect { progresses →                ║
║            _uiState.copy(currentProgress = progress) ← STATE CHANGE  ║
║          }                                                           ║
║        }                                                             ║
╚═════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
╔═════════════════════════════════════════════════════════════════════╗
║  DOMAIN LAYER (Pure Kotlin)                                         ═════════
║                                                                     ║
║  GetAlphabetUseCase.invoke()                                         ║
║    └── repository.getAllAlphabets()  ← Flow<List<Alphabet>>        ║
║                                                                     ║
║  GetProgressUseCase.invoke()                                         ║
║    └── repository.getAllProgress()   ← Flow<List<LetterProgress>>  ║
║                                                                     ║
║  ┌───────────────────────────────────────────────────────────┐      ║
║  │  IAlphabetRepository INTERFACE (no Room, no Android)      │      ║
║  │  - getAllAlphabets(): Flow<List<Alphabet>>               │      ║
║  │  - getAlphabetByLetter(Char): Alphabet?                   │      ║
║  └───────────────────────────────────────────────────────────┘      ║
╚═════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
╔═════════════════════════════════════════════════════════════════════╗
║  DATA LAYER (Room)                                                   ═════════
║                                                                     ║
║  AlphabetRepositoryImpl                                              ║
║    └── alphabetDao.getAllAlphabets()                                ║
║      └── Room query: "SELECT * FROM alphabet ORDER BY letter ASC"  ║
║        └── Flow<List<AlphabetEntity>>  ← raw Room data             ║
║        └── .map { it.toDomainModel() }  ← conversion              ║
║          └── Flow<List<Alphabet>>  ← domain model                  ║
╚═════════════════════════════════════════════════════════════════════╝
                                        │
                   data flows back up the stack (bottom to top)
                                        │
                                        ▼
╔═════════════════════════════════════════════════════════════════════╗
║  UI LAYER — RECOMPOSITION                                            ═════════
║                                                                     ║
║  AlphabetScreen composable receives new uiState                     ║
║    └── uiState.allLetters → 26 letters rendered                    ║
║    └── uiState.currentProgress → "🆕 New" badge for 'A'           ║
║    └── uiState.selectedLetter → displayed in detail view           ║
║                                                                     ║
║  Compose AUTOMATICALLY recomposes the affected composables          ║
║  (Home doesn't re-render — only AlphabetScreen does)               ║
╚═════════════════════════════════════════════════════════════════════╝
```

---

## Reverse Flow: Marking a Letter as Learned

This shows the **top-down** flow — action → data change → UI update:

```
User taps "Mark as Learned" button
    │
    ▼
AlphabetScreen (Compose)
    └── onMarkLearned('A')
        └── viewModel.markAsLearned('A')
    │
    ▼
AlphabetViewModel.markAsLearned('A')
    └── markLetterLearnedUseCase('A')
    │
    ▼
MarkLetterLearnedUseCase.invoke('A')
    └── repository.markLetterLearned('A')    → learned = true
    └── repository.incrementAttempts('A')     → masteryLevel + 1
    │
    ▼
AlphabetRepositoryImpl
    └── progressDao.updateProgress("A", learned = true)
    └── progressDao.updateMasteryLevel("A", masteryLevel + 1)
    │
    ▼
Room Database (SQLite)
    UPDATE progress SET learned = 1 WHERE letter = 'A'
    UPDATE progress SET masteryLevel = masteryLevel + 1 WHERE letter = 'A'
    │
    │  Flow emits new data (because Room observed the change)
    ▼
AlphabetRepositoryImpl
    └── progressDao.getAllProgress() → emits new data
    └── .map { it.toDomainModel() }
    │
    ▼
GetProgressUseCase
    └── repository.getAllProgress() → emits new data
    │
    ▼
AlphabetViewModel
    └── collects new progress
    └── _uiState.copy(currentProgress = progress)  ← state update
    │
    ▼
AlphabetScreen (Compose)
    └── uiState.currentProgress changes
    └── Badge updates: "🆕 New" → "🔤 Practicing" → "🌟 Learned"
    └── UI RECOMPOSES automatically
```

---

## Architecture Layer Checklist — Every Layer's Responsibility

| Layer | What It Does | What It Does NOT Do |
|---|-|---|
| **UI (Compose)** | Display state, collect user actions | No business logic, no DB access |
| **ViewModel** | Manage UI state, orchestrate use cases | No Room, no Android framework |
| **UseCase** | Single business rule per class | No UI state, no navigation |
| **Domain Model** | Plain data classes | No Android, no DB annotations |
| **Repository Interface** | Define data contract | No implementation |
| **Repository Impl** | Convert entities, query DB | No business logic |
| **Room** | Persist data | No business logic, no UI |
| **Entity** | DB row structure | No business logic |

---

## Key Takeaways

1. **Data flows bottom-up** (Room → Repository → UseCase → ViewModel → UI)
2. **Actions flow top-down** (UI → ViewModel → UseCase → Repository → Room)
3. **State changes are the only way to update UI** — through StateFlow
4. **Each layer depends only on the layer below it** — not above, not sideways
5. **ViewModels are stateless in construction** — all dependencies injected
6. **Domain models are pure** — no Android, no framework, no database

---

*End of the architecture series. You now understand every layer of NirKids.*

---

## Quick Reference — Where to Find Things

| Concept | File | Package |
|---|-|---|
| App entry | `NirKidsApp.kt` | `com.nirkids.app` |
| DI module | `AppModule.kt` | `com.nirkids.app.di` |
| DB | `AlphabetsDatabase.kt` | `com.nirkids.app.data.local` |
| DAOs | `AlphabetDao.kt`, `ProgressDao.kt` | `com.nirkids.app.data.local` |
| Entities | `AlphabetEntity.kt`, `ProgressEntity.kt` | `com.nirkids.app.data.model` |
| Repository impl | `AlphabetRepository.kt` | `com.nirkids.app.data.repository` |
| Repository interface | `IAlphabetRepository.kt` | `com.nirkids.app.domain.repository` |
| Domain models | `Alphabet.kt`, `LetterProgress.kt`, etc. | `com.nirkids.app.domain.model` |
| UseCases | `GetAlphabetUseCase.kt`, etc. | `com.nirkids.app.domain.usecase` |
| ViewModels | `HomeViewModel.kt`, etc. | `com.nirkids.app.ui.main.viewmodel` |
| Screens | `HomeScreen.kt`, `AlphabetScreen.kt`, etc. | `com.nirkids.app.ui.main.screens` |
| Navigation | `NavGraph.kt` | `com.nirkids.app.ui.main.nav` |
| Activity | `MainActivity.kt` | `com.nirkids.app.ui.main` |
| Theme | `Color.kt`, `Type.kt`, `Theme.kt` | `com.nirkids.app.ui.theme` |
