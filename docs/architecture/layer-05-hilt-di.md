# Layer 05 — Hilt Dependency Injection

> Hilt (built on Dagger) provides dependency injection to Android apps. It handles creating and wiring objects for you, so ViewModels don't manually construct their dependencies.

---

## What Is Dependency Injection?

Dependencies are objects your code needs to do its job. Instead of creating them manually, you **inject** them.

```
Without DI (bad):             With DI (good):
┌───────┐                    ┌───────┐
│VM     │                    │VM     │
│creates │                    │receives│
│repo    │◀── dependency     │repo    │
│manually│                    │(injected)│
└───────┘                    └───────┘

Hard to test.                     Easy to test.
Tightly coupled.                  Loosely coupled.
```

---

## Hilt Setup — Application Level

### 1. Application Class

```kotlin
// File: com/nirkids/app/NirKidsApp.kt
@HiltAndroidApp  // generates Hilt components
class NirKidsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppTraceValidator.initialize(this)
    }
}
```

`@HiltAndroidApp` must be on **exactly one** `Application` subclass in your app. It generates the `Hilt_NirKidsApp` class that ties Hilt to your app lifecycle.

### 2. AndroidManifest.xml

```xml
<application
    android:name=".NirKidsApp"
    ... >
    <activity
        android:name=".ui.main.MainActivity"
        android:exported="true">
        <!-- ... -->
    </activity>
</application>
```

---

## Hilt at the Module Level — Providing Dependencies

```kotlin
// File: com/nirkids/app/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlphabetsDatabase(@ApplicationContext app: Context): AlphabetsDatabase {
        return Room.databaseBuilder(
            app,
            AlphabetsDatabase::class.java,
            "alphabets_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAlphabetRepository(database: AlphabetsDatabase): IAlphabetRepository {
        // Database depends on nothing (context is provided)
        // Repository depends on database
        return AlphabetRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideGetAlphabetUseCase(repository: IAlphabetRepository): GetAlphabetUseCase {
        return GetAlphabetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRandomLetterUseCase(repository: IAlphabetRepository): GetRandomLetterUseCase {
        return GetRandomLetterUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkLetterLearnedUseCase(repository: IAlphabetRepository): MarkLetterLearnedUseCase {
        return MarkLetterLearnedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetProgressUseCase(repository: IAlphabetRepository): GetProgressUseCase {
        return GetProgressUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideParentGateValidateUseCase(): ParentGateValidateUseCase {
        // No dependencies — just returns new instance
        return ParentGateValidateUseCase()
    }

    @Provides
    @Singleton
    fun provideSeedDataUseCase(
        repository: IAlphabetRepository,
        database: AlphabetsDatabase
    ): SeedDataUseCase {
        return SeedDataUseCase(repository, database)
    }
}
```

### Understanding the Provider Chain

Hilt builds a dependency graph. Here's how it resolves `AlphabetViewModel`:

```
AlphabetViewModel
    └── needs → GetAlphabetUseCase
        └── needs → IAlphabetRepository
            └── needs → AlphabetsDatabase
                └── needs → Context (provided by @ApplicationContext)

    └── needs → GetProgressUseCase
        └── needs → IAlphabetRepository (already resolved above)

    └── needs → MarkLetterLearnedUseCase
        └── needs → IAlphabetRepository (already resolved above)
```

**Key:** Hilt resolves each dependency **once** and reuses it (because of `@Singleton`). The database is created once. The repository is created once. Use cases are created once.

---

## Hilt at the ViewModel Level — Injecting Dependencies

```kotlin
// File: com/nirkids/app/ui/main/viewmodel/HomeViewModel.kt
@HiltViewModel  // tells Hilt this is a Hilt-injectable ViewModel
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val seedDataUseCase: SeedDataUseCase
) : ViewModel() {
    // Hilt automatically provides all three parameters
    // No manual instantiation needed!
}
```

**How it works:**
1. `@HiltViewModel` marks the class as Hilt-managed
2. `@Inject constructor` tells Hilt to use this constructor for injection
3. Each parameter type (e.g., `GetAlphabetUseCase`) must have a `@Provides` function in a module (or another `@Inject` constructor)
4. Hilt generates the code to wire everything together

---

## Hilt at the Activity Level

```kotlin
// File: com/nirkids/app/ui/main/MainActivity.kt
@AndroidEntryPoint  // injects into Activity
class MainActivity : ComponentActivity() {
    // Can inject dependencies directly if needed
    // @Inject lateinit var someService: SomeService
}
```

`@AndroidEntryPoint` enables dependency injection in Activities, Fragments, Services, BroadcastReceivers, and ContentProviders.

---

## Hilt at the Compose Level

```kotlin
// In any Composable screen
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()  // ← Hilt provides it
) {
    val uiState by viewModel.uiState.collectAsState()
    // ...
}
```

`hiltViewModel()` creates (or retrieves) a ViewModel scoped to the Composable's lifecycle. It automatically resolves the ViewModel's constructor dependencies from Hilt.

---

## Scopes — When Are Objects Created and Destroyed?

| Scope | Lifetime | Use Case |
|---|-|---|
| `@Singleton` | App lifetime | Database, Repository, UseCases |
| `@ActivityRetainedScoped` | Activity retained | Configuration-change survivors |
| `@ActivityScoped` | Activity alive | Activity-scoped services |
| `@ViewModelScoped` | ViewModel alive | ViewModel itself |
| `@ComposableScoped` | Composable recomposition | Ephemeral Compose objects |

In NirKids, **everything** in `AppModule` uses `@Singleton` because:
- The database should be created once per app
- The repository should share one database instance
- Use cases are stateless — one instance is fine
- `ParentGateValidateUseCase` is stateless — no DI needed

---

## Provider Function Rules

### Rule 1: One `@Provides` per type

```kotlin
@Provides
@Singleton
fun provideRepository(database: AlphabetsDatabase): IAlphabetRepository {
    return AlphabetRepositoryImpl(database)
}
```

The return type (`IAlphabetRepository`) is what Hilt provides. Any code requesting `IAlphabetRepository` gets an `AlphabetRepositoryImpl`.

### Rule 2: Parameters are auto-injected

```kotlin
@Provides
@Singleton
fun provideGetAlphabetUseCase(
    repository: IAlphabetRepository  // ← Hilt finds the @Provides for this
): GetAlphabetUseCase {
    return GetAlphabetUseCase(repository)
}
```

Hilt looks for a provider for `IAlphabetRepository` automatically.

### Rule 3: Context qualifiers

```kotlin
@Provides
@Singleton
fun provideAlphabetsDatabase(
    @ApplicationContext app: Context  // ← tells Hilt which Context to use
): AlphabetsDatabase {
    return Room.databaseBuilder(app, ...)
}
```

Without `@ApplicationContext`, Hilt might provide the wrong Context (Activity Context vs Application Context).

---

## Hilt in Unit Tests

In unit tests, you bypass Hilt entirely — this is actually **better** for testing:

```kotlin
class HomeViewModelTest {
    private val getAlphabetUseCase = mockk<GetAlphabetUseCase>()
    private val getProgressUseCase = mockk<GetProgressUseCase>()
    private val seedDataUseCase = mockk<SeedDataUseCase>()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        // Create ViewModel directly with mocks — no Hilt needed
        viewModel = HomeViewModel(
            getAlphabetUseCase,
            getProgressUseCase,
            seedDataUseCase
        )
    }
}
```

This is why the constructor pattern matters: if you used `@Inject constructor` with complex parameters, testing would be harder. Hilt is for production wiring; manual construction is for testing.

---

## Hilt Component Diagram

```
Hilt_NirKidsApp (SingletonComponent)
├── AppModule
│   ├── @Singleton → AlphabetsDatabase
│   ├── @Singleton → AlphabetRepositoryImpl
│   ├── @Singleton → GetAlphabetUseCase
│   ├── @Singleton → GetProgressUseCase
│   ├── @Singleton → MarkLetterLearnedUseCase
│   ├── @Singleton → GetRandomLetterUseCase
│   ├── @Singleton → ParentGateValidateUseCase
│   └── @Singleton → SeedDataUseCase
│
├── MainActivity (@AndroidEntryPoint)
│   └── reads from SingletonComponent
│
├── HomeViewModel (@HiltViewModel)
│   └── reads from SingletonComponent
│
├── AlphabetViewModel (@HiltViewModel)
│   └── reads from SingletonComponent
│
└── PronunciationViewModel (@HiltViewModel)
    └── reads from SingletonComponent
```

---

## Common Hilt Mistakes

❌ **Forgetting `@HiltAndroidApp` on Application**

```kotlin
class NirKidsApp : Application() {
    // MISSING @HiltAndroidApp → injection doesn't work
}
```

✅ **Add the annotation**

```kotlin
@HiltAndroidApp
class NirKidsApp : Application() { }
```

❌ **Mixing @Inject and @Provides for the same type**

```kotlin
// BAD — this type is provided in two places
@Provides fun provideRepo(db: Db): IAlphabetRepository { ... }
class SomeClass @Inject constructor(private val repo: IAlphabetRepository) { ... }
```

✅ **Provide it once, or use @Inject constructor directly**

---

*Next: [Layer 06 — Repository Pattern](./layer-06-repository-pattern.md)*
