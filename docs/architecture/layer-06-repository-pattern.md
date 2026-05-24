# Layer 06 — Repository Pattern: Abstraction Between Layers

> The Repository pattern hides data source details behind a clean interface. Code that needs data talks to the interface, not the concrete storage mechanism.

---

## The Problem Repositories Solve

Without a repository, your code looks like this:

```kotlin
// ❌ ViewModel directly accesses Room
class AlphabetViewModel : ViewModel() {
    private val dao = Room.databaseBuilder(...)
        .build()
        .alphabetDao()

    fun loadLetters() {
        viewModelScope.launch {
            val letters = dao.getAllAlphabets()  // ViewModel knows about Room
        }
    }
}
```

**Problems:**
- ViewModel depends on Room (a specific database library)
- Hard to test — need a real Room database in every test
- Can't swap Room for Firebase/SQLite/etc. without rewriting ViewModels
- ViewModel has infrastructure concerns (database setup) mixed with business logic

With a repository:

```kotlin
// ✅ ViewModel talks to an interface
@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase  // talks to IAlphabetRepository
) : ViewModel() {
    // ViewModel knows NOTHING about Room, SQL, or databases
}
```

---

## The Interface — Domain Layer Contract

```kotlin
// File: com/nirkids/app/domain/repository/IAlphabetRepository.kt
// LOCATION: Domain Layer — pure Kotlin, no Android imports
package com.nirkids.app.domain.repository

interface IAlphabetRepository {
    // ── READ operations ──
    fun getAllAlphabets(): Flow<List<Alphabet>>
    suspend fun getAlphabetByLetter(letter: Char): Alphabet?

    // ── PROGRESS operations ──
    fun getAllProgress(): Flow<List<LetterProgress>>
    suspend fun getProgressForLetter(letter: Char): LetterProgress?

    // ── WRITE operations ──
    suspend fun markLetterLearned(letter: Char)
    suspend fun incrementAttempts(letter: Char)

    // ── ADMIN operations ──
    suspend fun saveAllAlphabets(alphabets: List<AlphabetEntity>)
    suspend fun seedInitialData(database: AlphabetsDatabase)
}
```

**What the interface exposes:**
- **What** data operations are available (getAll, getOne, mark, save)
- **No** how the data is stored (Room, Firebase, memory, network)
- **No** SQL queries, HTTP endpoints, or file paths

**What the interface hides:**
- Database schema
- Query optimization
- Connection management
- Caching strategy
- Data migration

---

## The Implementation — Data Layer Concrete

```kotlin
// File: com/nirkids/app/data/repository/AlphabetRepository.kt
// LOCATION: Data Layer — knows Room, converts entities
package com.nirkids.app.data.repository

class AlphabetRepositoryImpl(
    private val database: AlphabetsDatabase  // ← data layer knows Room
) : IAlphabetRepository {

    private val alphabetDao = database.alphabetDao()
    private val progressDao = database.progressDao()

    // ═══════════════════════════════════════════════════
    // READ — from Room to domain models
    // ═══════════════════════════════════════════════════

    override fun getAllAlphabets(): Flow<List<Alphabet>> {
        return alphabetDao.getAllAlphabets()       // Room: Flow<List<AlphabetEntity>>
            .map { entities ->                     // ← CONVERSION
                entities.map { it.toDomainModel() }
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

    override suspend fun getProgressForLetter(letter: Char): LetterProgress? {
        return progressDao.getProgressForLetter(letter.toString())
            ?.toDomainModel()
    }

    // ═══════════════════════════════════════════════════
    // WRITE — domain model changes persist to Room
    // ═══════════════════════════════════════════════════

    override suspend fun markLetterLearned(letter: Char) {
        // Check if alphabet exists, then update progress
        alphabetDao.getAlphabetByLetter(letter.toString())?.let {
            progressDao.updateProgress(letter.toString(), learned = true)
        }
    }

    override suspend fun incrementAttempts(letter: Char) {
        progressDao.getProgressForLetter(letter.toString())?.let {
            progressDao.updateMasteryLevel(letter.toString(), it.masteryLevel + 1)
        }
    }

    // ═══════════════════════════════════════════════════
    // ADMIN — seeding and bulk operations
    // ═══════════════════════════════════════════════════

    override suspend fun saveAllAlphabets(alphabets: List<AlphabetEntity>) {
        alphabetDao.insertAllAlphabets(alphabets)
    }

    override suspend fun seedInitialData(database: AlphabetsDatabase) {
        val existing = alphabetDao.getAllAlphabets().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val allAlphabets = generateDefaultAlphabets()
            alphabetDao.insertAllAlphabets(allAlphabets)

            for (alpha in allAlphabets) {
                progressDao.insertProgress(
                    ProgressEntity(
                        letter = alpha.letter,
                        learned = false,
                        attempts = 0,
                        masteryLevel = 0
                    )
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // CONVERSION — the critical boundary layer
    // ═══════════════════════════════════════════════════

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

---

## The Entity-to-Domain Conversion

This is the **most important pattern** in the repository layer. The conversion methods sit at the boundary between Room and your domain models:

| Aspect | Entity (Room) | Domain Model |
|---|-|---|
| **letter type** | `String` | `Char` |
| **purpose** | Database row | Business concept |
| **methods** | None (pure data) | Computed properties (`displayName`, `statusText`) |
| **lifecycle** | Managed by Room | Managed by use cases |
| **dependencies** | `@Entity`, `@PrimaryKey` | None (pure Kotlin) |

### The Mapping Process

```
Room Query Result              Repository                    UseCase               ViewModel
════════════════════           ════════════                  ══════════            ═════════

"SELECT *"  ──▶  List<AlphabetEntity>  ──▶  map {           ──▶  Flow<List<Alphabet>>  ──▶  collect {
                     letter="A"                toDomainModel()         letter='A'                  alphabet ->
                     phonetic="/æ/"            →                      phonetic="/æ/"            Text(alphabet.displayName)
                     exampleWord="Apple"         Alphabet(                 exampleWord="Apple"
                     ...                           'A', "/æ/", ... )          ...
```

---

## Why Repository Over Direct DAO Access?

| Scenario | Direct DAO | Repository |
|---|-|---|
| **Swap DB from Room to SQLite** | Rewrite all ViewModels | Write new Repository, ViewModels unchanged |
| **Add caching** | Hard — scattered across ViewModels | Easy — add cache in one place |
| **Add network layer later** | Messy — mix Room + Retrofit calls | Clean — repository decides data source |
| **Unit test** | Need real Room database | Mock the interface |
| **Change schema** | Update every DAO call site | Update mapper methods only |

---

## Flow vs Suspend — When to Use Each

```kotlin
// Flow — when the UI needs to react to changes
fun getAllAlphabets(): Flow<List<Alphabet>>
// ✓ Room observes the table and emits new data when it changes
// ✓ ViewModel collects once and stays updated automatically

// suspend — when you need a one-time result
suspend fun getAlphabetByLetter(letter: Char): Alphabet?
// ✓ Called once, get result, done
// ✓ No need for reactive updates
```

**Rule of thumb:** Use `Flow` when the data changes over time (the user learns more letters). Use `suspend` for one-shot operations (get one letter, save a result).

---

## Repository Testing — The Interface Makes It Easy

```kotlin
// In tests, you test the UseCase with a mocked Repository
class GetAlphabetUseCaseTest {
    private val mockRepo = mockk<IAlphabetRepository>()  // ← mock the interface

    @Test
    fun `invoke returns alphabets from repository`() = runTest {
        coEvery { mockRepo.getAllAlphabets() } returns flowOf(
            listOf(Alphabet('A', "/æ/", "Apple", "🍎", true))
        )

        val useCase = GetAlphabetUseCase(mockRepo)
        val result = useCase()

        assertEquals(1, result.size)
        assertEquals('A', result[0].letter)
    }
}
```

The repository is **testable in two ways**:

1. **Interface tests** — mock `IAlphabetRepository`, test UseCases in JVM
2. **Implementation tests** — use in-memory Room database, test real queries

---

## Multiple Repositories in One App

An app can have many repositories. Each one follows the same pattern:

```kotlin
// Domain layer
interface IUserRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun updateUser(user: User)
}

// Data layer
class UserRepositoryImpl(
    private val database: UserDatabase
) : IUserRepository {
    override fun getUser(userId: String): Flow<User?> {
        return database.userDao().getById(userId)
            .map { it?.toDomainModel() }
    }
    // ...
}
```

Each repository:
- Has its own interface in the domain layer
- Has one implementation in the data layer
- Manages its own entity-to-domain conversion
- Is bound to a single database table

---

*Next: [Layer 07 — Architecture in Action](./layer-07-architecture-in-action.md)*
