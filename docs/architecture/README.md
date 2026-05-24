# Clean Architecture + MVVM — NirKids Architecture Guide

> The architecture that powers [NirKids](https://github.com/your-org/nirkids) — an Android app helping kids learn the alphabet. This guide teaches you **Clean Architecture**, **MVVM**, and **Jetpack Compose** through real code from the project.

---

## What You'll Learn

| Topic | Document |
|---|---|
| **The Big Picture** | This file — high-level overview |
| **Clean Architecture** | [layer-01-clean-architecture.md](./layer-01-clean-architecture.md) — Three layers, data flow, boundaries |
| **MVVM Pattern** | [layer-02-mvvm-pattern.md](./layer-02-mvvm-pattern.md) — ViewModel as the bridge, state management |
| **Jetpack Compose** | [layer-03-jetpack-compose.md](./layer-03-jetpack-compose.md) — Declarative UI, state hoisting |
| **Jetpack Navigation** | [layer-04-jetpack-navigation.md](./layer-04-jetpack-navigation.md) — NavHost, routes, back stack |
| **Hilt Dependency Injection** | [layer-05-hilt-di.md](./layer-05-hilt-di.md) — @Inject, @Provides, scoping |
| **Repository Pattern** | [layer-06-repository-pattern.md](./layer-06-repository-pattern.md) — Abstraction, data flow |
| **Putting It All Together** | [layer-07-architecture-in-action.md](./layer-07-architecture-in-action.md) — Feature walkthrough |

---

## NirKids App Overview

**NirKids** is a kid-friendly alphabet learning app. It features:

- **26 Letter Cards** — Colorful, large, tap-to-explore alphabet cards with emoji illustrations
- **Phonetic Pronunciation** — Text-to-speech powered pronunciation
- **Progress Tracking** — Visual mastery levels: 🆕 New → 🔤 Practicing → 🌟 Learned → ✅ Mastered
- **Pronunciation Practice** — Interactive practice mode with instant feedback
- **ParentGate** — Math puzzle gate to prevent accidental settings access
- **Accessibility** — Vibration feedback, color-coded vowels/consonants, large text

---

## Architecture at a Glance

```
┌──────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│         (Jetpack Compose + Material 3)                       │
│                                                              │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│   │ HomeScreen   │  │ AlphabetScreen │ │ PronunciationScreen │
│   │ HomeViewModel│  │ AlphabetViewModel │ │ PronunciationViewModel│
│   └──────────────┘  └──────────────┘  └──────────────────┘ │
│                                                              │
│   ┌──────────────────────────────┐    ┌─────────────────┐  │
│   │ NavGraph (Navigation)        │    │ TtsHelper,      │  │
│   │ MainActivity                 │    │ VibrationHelper │  │
│   └──────────────────────────────┘    └─────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                              │
                    observes / receives
                              ▼
┌──────────────────────────────────────────────────────────────┐
│                       Domain Layer                           │
│              (Pure Kotlin — No Android Dependencies)         │
│                                                              │
│   Use Cases (Business Logic):                                │
│   ┌────────────────┐  ┌───────────────────┐  ┌────────────┐ │
│   │ GetAlphabet    │  │ GetRandomLetter   │  │ MarkLetter │ │
│   │ UseCase        │  │ UseCase           │  │ Learned    │ │
│   └────────────────┘  └───────────────────┘  └────────────┘ │
│                                                              │
│   Domain Models:                                             │
│   ┌────────────────┐  ┌───────────────────┐  ┌────────────┐ │
│   │ Alphabet       │  │ LetterProgress    │  │ Resource   │ │
│   └────────────────┘  └───────────────────┘  └────────────┘ │
│                                                              │
│   Repository Interfaces:                                     │
│   ┌──────────────────────────────────────────────────────────┐ │
│   │ IAlphabetRepository                                      │ │
│   └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                              │
                    invokes
                              ▼
┌──────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│              (Room Database + Repository Implementation)     │
│                                                              │
│   Repository Implementation:                                 │
│   ┌──────────────────────────────────────────────────────────┐ │
│   │ AlphabetRepositoryImpl                                   │ │
│   │  - Converts Entity → Domain Model                        │ │
│   │  - Handles database queries                              │ │
│   └──────────────────────────────────────────────────────────┘ │
│                                                              │
│   Room Database:                                             │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│   │ AlphabetDao  │  │ ProgressDao  │  │ AlphabetsDatabase│  │
│   └──────────────┘  └──────────────┘  └──────────────────┘  │
│                                                              │
│   Data Entities:                                             │
│   ┌────────────────┐  ┌───────────────────┐                 │
│   │ AlphabetEntity │  │ ProgressEntity    │                 │
│   └────────────────┘  └───────────────────┘                 │
└──────────────────────────────────────────────────────────────┘
```

---

## Data Flow: A Complete Example

Let's trace a single user action through the architecture — **a kid taps the letter "A" card on the Home Screen**.

### Step-by-Step Data Flow

```
1. USER TAP
   HomeScreen.kt (UI Layer)
   └── onLetterTapped('A')
       └── vibrationHelper.vibrate(30)    // haptic feedback
       └── ttsHelper.speak("A")           // audio feedback

2. NAVIGATION
   NavGraph.kt
   └── navController.navigate("alphabet")
       └── Compose transitions to AlphabetScreen

3. VIEWMODEL INITIALIZATION
   AlphabetViewModel.kt (ViewModel Layer)
   └── @Inject constructor receives:
       ├── GetAlphabetUseCase
       ├── GetProgressUseCase
       └── MarkLetterLearnedUseCase
   └── init { loadAllLetters() }
       └── getAlphabetUseCase() → Flow<List<Alphabet>>

4. DOMAIN LAYER
   GetAlphabetUseCase.kt
   └── repository.getAllAlphabets() → Flow<List<Alphabet>>
       └── calls IAlphabetRepository interface

5. DATA LAYER
   AlphabetRepositoryImpl.kt
   └── alphabetDao.getAllAlphabets() → Flow<List<AlphabetEntity>>
       └── Room query: "SELECT * FROM alphabet ORDER BY letter ASC"
       └── .map { it.toDomainModel() }
           └── converts AlphabetEntity → Alphabet

6. DATA FLOWS BACK UP THE STACK
   Flow<List<Alphabet>>
   └── collected in AlphabetViewModel
       └── _uiState.value = _uiState.copy(allLetters = alphabets)

7. UI REACTS
   AlphabetScreen.kt
   └── uiState.allLetters → rendered as 26 letter cards
```

This is the **bottom-up** flow: Data flows from Room → Repository → UseCase → ViewModel → UI.

### Top-Down: Marking a Letter as Learned

```
1. USER ACTION
   AlphabetScreen → viewModel.markAsLearned('A')

2. VIEWMODEL
   AlphabetViewModel.markAsLearned('A')
   └── markLetterLearnedUseCase('A')
       └── repository.markLetterLearned('A')
           └── progressDao.updateProgress("A", learned = true)
               └── Room: UPDATE progress SET learned = 1 WHERE letter = 'A'

3. REFRESH
   AlphabetViewModel.loadAllLetters()
   └── refreshes the UI state
       └── UI updates: "🌟 Learned" badge appears
```

---

## Key Architectural Principles

### 1. Layer Independence (The Dependency Rule)

```
UI Layer    → depends on →    Domain Layer (interfaces + models)
Data Layer  → depends on →    Domain Layer (interfaces + models)
Domain Layer → depends on →   Nothing (pure Kotlin)
```

**In code:**

```kotlin
// ✅ GOOD — Domain model has NO Android imports
package com.nirkids.app.domain.model
data class Alphabet(
    val letter: Char,
    val phonetic: String,
    val exampleWord: String,
    val imageUrl: String,
    val isVowel: Boolean
)

// ✅ GOOD — Use case only knows about the interface
package com.nirkids.app.domain.usecase
class GetAlphabetUseCase(
    private val repository: IAlphabetRepository  // not AlphabetRepositoryImpl!
)

// ❌ BAD — ViewModel depends on concrete class, not interface
// class AlphabetViewModel(private val realRepo: AlphabetRepositoryImpl)

// ❌ BAD — Domain model depends on Android
// import androidx.room.Entity  // NO! Domain models are pure Kotlin
```

### 2. State Is One-Way

```
ViewModel ──StateFlow──→ Compose UI
         (data flows DOWN)

Compose UI ──→ callback → ViewModel
         (user actions flow UP)
```

Never let UI state flow DOWN to the ViewModel. Never let the ViewModel push directly to UI — always through StateFlow.

### 3. ViewModels Are Stateless in Construction

```kotlin
@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val markLetterLearnedUseCase: MarkLetterLearnedUseCase
) : ViewModel() {
    // No hardcoded values. Everything comes through constructor.
    // This makes testing easy — just pass mock use cases!
}
```

### 4. Repository Abstraction

```kotlin
// Domain layer defines the contract
interface IAlphabetRepository {
    fun getAllAlphabets(): Flow<List<Alphabet>>
    suspend fun getAlphabetByLetter(letter: Char): Alphabet?
}

// Data layer implements it
class AlphabetRepositoryImpl(
    private val database: AlphabetsDatabase  // Data layer knows Room
) : IAlphabetRepository {
    // ...implementation...
}
```

The domain layer never knows Room exists. Swap Room for Firebase? Just write a new implementation.

---

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **UI** | Jetpack Compose | Declarative UI |
| **Navigation** | Jetpack Navigation Compose | Screen routing |
| **State** | StateFlow + ViewModel | Reactive state |
| **DI** | Hilt (Dagger) | Dependency injection |
| **DB** | Room | Local persistence |
| **Coroutines** | kotlinx.coroutines | Async operations |
| **Flow** | kotlinx.coroutines.flow | Reactive streams |
| **Testing** | JUnit 4 + MockK + Robolectric | TDD |

---

## How to Use This Guide

1. **Start with** this overview file to understand the big picture
2. **Read** [layer-01-clean-architecture.md](./layer-01-clean-architecture.md) for layer-by-layer deep dive
3. **Read** [layer-02-mvvm-pattern.md](./layer-02-mvvm-pattern.md) for ViewModel patterns
4. **Read** [layer-03-jetpack-compose.md](./layer-03-jetpack-compose.md) for Compose patterns
5. **Read** [layer-07-architecture-in-action.md](./layer-07-architecture-in-action.md) for a complete feature walkthrough
6. **Explore** the NirKids source code while reading — every concept maps to real code

---

*This guide is written specifically for the [NirKids](https://github.com/your-org/nirkids) codebase. Every example uses actual code from the project.*
