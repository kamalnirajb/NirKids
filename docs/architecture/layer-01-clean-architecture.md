# Layer 01 — Clean Architecture: Three Layers Explained

> Clean Architecture separates your app into concentric circles of responsibility. Code in an inner circle knows nothing about outer circles.

---

## The Three Layers of NirKids

NirKids follows a **three-layer** architecture (a simplified version of Robert C. Martin's Clean Architecture):

```
┌─────────────────────────────────────┐
│         UI Layer (Outer)            │  ← User-facing
│  Compose Screens + ViewModels       │
├─────────────────────────────────────┤
│         Domain Layer (Middle)       │  ← Business rules
│  UseCases + Domain Models + Repos   │
├─────────────────────────────────────┤
│         Data Layer (Inner)          │  ← Data storage
│  Repository Impl + Room + Entities  │
└─────────────────────────────────────┘
```

### Rules of Dependency

```
UI Layer    → may depend on → Domain Layer
Data Layer  → may depend on → Domain Layer
Domain Layer → may depend on → NOTHING (pure Kotlin)
UI Layer    → may NOT depend on → Data Layer
Data Layer  → may NOT depend on → UI Layer
```

---

## Layer 0: Domain Layer — The Heart

**Location:** `app/src/main/java/com/nirkids/app/domain/`

This layer contains **pure Kotlin** — zero Android imports, zero framework dependencies. It defines what the app *does*, not *how* it does it.

### Domain Models — Pure Data Classes

```kotlin
// Location: com/nirkids/app/domain/model/Alphabet.kt
// Import: NOTHING Android-related
package com.nirkids.app.domain.model

data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
) {
    val displayName: String
        get() = "$letter — $exampleWord"
    val phoneticBreakdown: List<String>
        get() = phonetic.split("/")
            .filter { it.isNotBlank() }
            .map { it.trim() }
}
```

**Key insight:** This `Alphabet` class works anywhere — Android, iOS, web backend, unit tests. It has no Android dependencies.

```kotlin
// Location: com/nirkids/app/domain/model/LetterProgress.kt
data class LetterProgress(
    val letter: Char,
    val learned: Boolean = false,
    val attempts: Int = 0,
    val masteryLevel: Int = 0
) {
    val masteryPercent: Int
        get() = if (attempts > 0) (masteryLevel * 100 / 10).coerceIn(0, 100) else 0

    val statusText: String
        get() = when {
            learned && masteryLevel >= 3 -> "✅ Mastered"
            learned                      -> "🌟 Learned"
            masteryLevel >= 2            -> "📚 Almost there"
            masteryLevel >= 1            -> "🔤 Practicing"
            else                         -> "🆕 New"
        }
}
```

**Business logic lives here!** The `statusText` property encodes the progression rule:
- `🆕 New` → 0 mastery
- `🔤 Practicing` → 1 attempt
- `📚 Almost there` → 2 attempts
- `🌟 Learned` → mastered but not fully
- `✅ Mastered` → learned + 3+ mastery

### Repository Interfaces — Contracts, Not Implementations

```kotlin
// Location: com/nirkids/app/domain/repository/IAlphabetRepository.kt
package com.nirkids.app.domain.repository

interface IAlphabetRepository {
    fun getAllAlphabets(): Flow<List<Alphabet>>
    suspend fun getAlphabetByLetter(letter: Char): Alphabet?
    fun getAllProgress(): Flow<List<LetterProgress>>
    suspend fun getProgressForLetter(letter: Char): LetterProgress?
    suspend fun markLetterLearned(letter: Char)
    suspend fun incrementAttempts(letter: Char)
    suspend fun saveAllAlphabets(alphabets: List<AlphabetEntity>)
    suspend fun seedInitialData(database: AlphabetsDatabase)
}
```

**Notice:** The interface is in the domain layer but references Room types (`AlphabetEntity`, `AlphabetsDatabase`) in some methods. In a stricter architecture, you'd use domain models everywhere and keep Room types isolated to the data layer. This is a pragmatic compromise in NirKids.

### Use Cases — Single Responsibility Business Logic

Each use case encapsulates one piece of business logic:

```kotlin
// Location: com/nirkids/app/domain/usecase/GetAlphabetUseCase.kt
class GetAlphabetUseCase(
    private val repository: IAlphabetRepository  // depends on INTERFACE, not implementation
) {
    operator fun invoke(): Flow<List<Alphabet>> {
        return repository.getAllAlphabets()  // delegate to repository
    }
}

// Location: com/nirkids/app/domain/usecase/MarkLetterLearnedUseCase.kt
class MarkLetterLearnedUseCase(
    private val repository: IAlphabetRepository
) {
    suspend operator fun invoke(letter: Char) {
        repository.markLetterLearned(letter)
        repository.incrementAttempts(letter)  // two operations in one use case
    }
}

// Location: com/nirkids/app/domain/usecase/ParentGateValidateUseCase.kt
// This use case has NO dependencies at all — pure logic
class ParentGateValidateUseCase {
    data class GateConfig(
        val maxOperand: Int = 99,
        val minOperand: Int = 1,
        val attemptsBeforeLock: Int = 3,
        val lockDurationMs: Long = 60_000L
    )

    fun generateQuestion(config: GateConfig = GateConfig()): ParentGateState {
        val operators = listOf("+", "-")
        val operator = operators[Random.nextInt(operators.size)]
        val a = Random.nextInt(config.minOperand, config.maxOperand + 1)
        val b = Random.nextInt(config.minOperand, config.maxOperand + 1)

        val correctAnswer = when (operator) {
            "+" -> a + b
            "-" -> {
                val (larger, smaller) = if (a >= b) Pair(a, b) else Pair(b, a)
                larger - smaller  // subtraction always non-negative
            }
            else -> 0
        }

        return ParentGateState(
            operandA = a, operandB = b, operator = operator,
            correctAnswer = correctAnswer,
            attemptsRemaining = config.attemptsBeforeLock,
            isVerified = false, isLocked = false
        )
    }

    fun validateAnswer(state: ParentGateState, userAnswer: Int): ParentGateState {
        if (state.isLocked) return state.copy(isLocked = true)
        return if (userAnswer == state.correctAnswer) {
            state.copy(isVerified = true)
        } else {
            val newAttempts = state.attemptsRemaining - 1
            if (newAttempts <= 0) {
                state.copy(attemptsRemaining = 0, isLocked = true,
                           lockTimeMs = System.currentTimeMillis())
            } else {
                state.copy(attemptsRemaining = newAttempts)
            }
        }
    }
}
```

**Key insight about `ParentGateValidateUseCase`:** It depends on **nothing**. No repository, no database, no context. This is pure business logic — the exact kind of code that should be unit-tested with zero setup.

---

## Layer 1: Data Layer — The Concrete Implementation

**Location:** `app/src/main/java/com/nirkids/app/data/`

This layer knows about Room, databases, and how to persist data. It implements the contracts defined in the domain layer.

### Entities — Room's Schema

```kotlin
// Location: com/nirkids/app/data/model/AlphabetEntity.kt
// This is a ROOM ENTITY — only the data layer knows about Room
@Entity(tableName = "alphabet")
data class AlphabetEntity(
    @PrimaryKey val letter: String,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
)
```

**Notice the difference from `Alphabet` (domain model):**
- `AlphabetEntity.letter` is a `String` (Room needs strings)
- `Alphabet.letter` is a `Char` (domain preference)
- No computed properties — entities are dumb data

```kotlin
// Location: com/nirkids/app/data/model/ProgressEntity.kt
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val letter: String,
    val learned: Boolean = false,
    val attempts: Int = 0,
    val masteryLevel: Int = 0
)
```

### Database — Room Setup

```kotlin
// Location: com/nirkids/app/data/local/AlphabetsDatabase.kt
@Database(
    entities = [AlphabetEntity::class, ProgressEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AlphabetsDatabase : RoomDatabase() {
    abstract fun alphabetDao(): AlphabetDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AlphabetsDatabase? = null

        fun getInstance(database: Context): AlphabetsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    database,
                    AlphabetsDatabase::class.java,
                    "alphabets_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### DAOs — SQL Queries

```kotlin
// Location: com/nirkids/app/data/local/AlphabetDao.kt
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

### Repository — The Bridge Between Layers

This is the **most important file** in the data layer. It converts between entities and domain models:

```kotlin
// Location: com/nirkids/app/data/repository/AlphabetRepository.kt
class AlphabetRepositoryImpl(
    private val database: AlphabetsDatabase  // data layer knows Room
) : IAlphabetRepository {  // implements domain layer interface

    private val alphabetDao = database.alphabetDao()
    private val progressDao = database.progressDao()

    override fun getAllAlphabets(): Flow<List<Alphabet>> {
        return alphabetDao.getAllAlphabets()
            .map { entities ->  // <-- THE CONVERSION LAYER
                entities.map { it.toDomainModel() }
            }
    }

    override suspend fun getAlphabetByLetter(letter: Char): Alphabet? {
        return alphabetDao.getAlphabetByLetter(letter.toString())
            ?.toDomainModel()
    }

    // ... other methods ...

    // <-- Private conversion methods (implementation detail) -->
    private fun AlphabetEntity.toDomainModel(): Alphabet {
        return Alphabet(
            letter = this.letter.firstOrNull() ?: ' ',
            phonetic = this.phonetic,
            exampleWord = this.exampleWord,
            imageUrl = this.imageUrl,
            isVowel = this.isVowel
        )
    }

    private fun ProgressEntity.toDomainModel(): LetterProgress {
        return LetterProgress(
            letter = this.letter.firstOrNull() ?: ' ',
            learned = this.learned,
            attempts = this.attempts,
            masteryLevel = this.masteryLevel
        )
    }
}
```

**The conversion methods (`toDomainModel()`) are the critical boundary.** They sit in the data layer but produce domain objects. This is the only place where Room types touch domain types.

---

## Layer 2: UI Layer — The Presentation

**Location:** `app/src/main/java/com/nirkids/app/ui/`

This layer knows about Compose, ViewModels, navigation, and everything Android UI. It **does not** know about Room entities or database internals.

### Activity — The Entry Point

```kotlin
// Location: com/nirkids/app/ui/main/MainActivity.kt
@AndroidEntryPoint  // Hilt injects here
class MainActivity : ComponentActivity() {

    private val ttsHelper by lazy { TtsHelper(applicationContext) }
    private val vibrationHelper by lazy { VibrationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NirKidsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        ttsHelper = ttsHelper,
                        vibrationHelper = vibrationHelper
                    )
                }
            }
        }
    }
}
```

**Key points:**
- `@AndroidEntryPoint` tells Hilt to inject dependencies
- `setContent { ... }` is the Compose entry point
- `TtsHelper` and `VibrationHelper` are created manually (not Hilt-injected) — they need the Activity context
- `NavGraph` orchestrates all screens

---

## The Dependency Graph — Visual Summary

```
                    ┌─────────────────┐
                    │  IAlphabetRepo   │  ← Interface (Domain)
                    │  domain.model.*  │
                    └───┬───────────┬───┘
                        │           │
              depends on│           │implements
                        ▼           ▼
         ┌──────────────────┐  ┌───────────────────────┐
         │ UseCases         │  │ AlphabetRepository    │
         │ GetAlphabet      │  │ Impl (Data Layer)     │
         │ GetProgress      │  │  - knows Room         │
         │ MarkLetterLearned│  │  - does conversion    │
         └──────────────────┘  └───────────────────────┘
                        │
              invokes   │
                        ▼
         ┌──────────────────┐
         │ AlphabetViewModel│  ← UI Layer
         │ (StateFlow)      │
         └────────┬─────────┘
                  │
           observes│
                  ▼
         ┌──────────────────┐
         │ HomeScreen       │  ← Compose
         │ AlphabetScreen   │
         │ PronunciationScreen│
         └──────────────────┘
```

---

## Why This Architecture Matters

| Concern | How Clean Architecture Handles It |
|---|---|
| **Testing** | Domain layer has zero Android dependencies → test on JVM with zero setup |
| **Swappable DB** | Replace Room with Firebase? Write a new `IAlphabetRepository` implementation |
| **Reusability** | Domain models work in any app, any platform |
| **Maintainability** | Each layer has a single responsibility → changes in one layer don't break others |
| **Parallel Teams** | UI team and data team work independently on their layers |

---

## Common Mistakes Beginners Make

❌ **Letting ViewModels create repositories directly**

```kotlin
// BAD — ViewModel depends on concrete Room types
class AlphabetViewModel : ViewModel() {
    private val database = Room.databaseBuilder(...)
    private val dao = database.alphabetDao()
}
```

✅ **Inject dependencies through constructor**

```kotlin
// GOOD — ViewModel only knows about interfaces
@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase
) : ViewModel()
```

❌ **Putting business logic in ViewModels**

```kotlin
// BAD — Calculation in ViewModel
fun loadStats() {
    getAlphabetUseCase().collect { alphabets ->
        getProgressUseCase().collect { progresses ->
            val learned = progresses.count { it.learned }
            // This calculation belongs in a UseCase!
        }
    }
}
```

✅ **Put business logic in UseCases**

```kotlin
// GOOD — ViewModel just collects and displays
// The calculation logic is in GetAlphabetUseCase + GetProgressUseCase
```

---

*Next: [Layer 02 — MVVM Pattern](./layer-02-mvvm-pattern.md)*
