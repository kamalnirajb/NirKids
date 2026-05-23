# 📚 NirKids - Learn ABCs the Fun Way!

> A secure, kid-friendly Android app to help children learn the alphabet, pronunciation, and support kids with hearing or speaking disabilities.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen" />
  <img src="https://img.shields.io/badge/Kotlin-2.1.x-blue" />
  <img src="https://img.shields.io/badge/TargetSDK-35-orange" />
  <img src="https://img.shields.io/badge/MVVM%20%7C%20Clean%20Arch%20%7C%20TDD-purple" />
</p>

---

## 🌈 Features

### For Kids
- **26 Letter Cards** — Colorful, large, tap-to-explore alphabet cards with emoji illustrations
- **Phonetic Pronunciation** — Text-to-speech (TTS) powered pronunciation for every letter
- **Progress Tracking** — Visual mastery levels: 🆕 New → 🔤 Practicing → 🌟 Learned → ✅ Mastered
- **Pronunciation Practice** — Interactive practice mode where kids type what they hear and get instant feedback
- **Accessibility Built-In** — Color-coded vowels/consonants, vibration feedback, large text, high-contrast mode

### For Parents
- **ParentGate** — Math puzzle gate prevents kids from accidentally accessing settings
- **Secure PIN Option** — Secondary PIN verification as an alternative gate
- **Lockout Protection** — After 3 wrong attempts, 60-second lockout timer
- **Progress Dashboard** — At-a-glance stats on learned, practicing, and new letters

### For Developers
- **TraceValidator** — Custom audit trail framework tracking all data changes and user interactions
- **Test Driven Development** — Unit tests for all ViewModels, UseCases, and Repository
- **Clean Architecture** — Separated data, domain, and UI layers
- **Modern Tech Stack** — Jetpack Compose, Hilt DI, Room, Navigation Component

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                         │
│  (Jetpack Compose + Material 3)                     │
│                                                     │
│  ┌────────────┐ ┌─────────────┐ ┌───────────────┐  │
│  │  Home      │ │  Alphabet   │ │  Pronunciation│  │
│  │  Screen    │ │  Screen     │ │  Screen       │  │
│  └────────────┘ └─────────────┘ └───────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │  ViewModels (State management)              │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                       │
│  (Pure Business Logic)                              │
│                                                     │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────┐  │
│  │  GetAlphabet │ │ GetRandom    │ │ MarkLetter │  │
│  │  UseCase     │ │ Letter UC    │ │ Learned UC │  │
│  └──────────────┘ └──────────────┘ └────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │  ParentGateValidateUseCase                    │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                   Data Layer                        │
│  (Room Database + Repository)                       │
│                                                     │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────┐  │
│  │  Room DB     │ │   DAOs       │ │ Repository │  │
│  └──────────────┘ └──────────────┘ └────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Design Patterns
- **MVVM** — Model-View-ViewModel for clear separation of concerns
- **Clean Architecture** — Domain models don't depend on Android or Room
- **Repository Pattern** — Abstract data sources behind interfaces
- **Dependency Injection** — Hilt (Dagger) for singleton and scoped bindings
- **UseCase Pattern** — Single-responsibility business logic classes

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.1+ |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Jetpack Navigation Compose |
| **DI** | Hilt (Dagger) |
| **Local DB** | Room |
| **TTS** | Android TextToSpeech API |
| **Architecture** | MVVM + Clean Architecture |
| **Testing** | JUnit 4, MockK, Robolectric, Espresso |
| **Audit** | TraceValidator (custom) |
| **Build** | Gradle + Version Catalogs |

---

## 📁 Project Structure

```
NirKids/
├── settings.gradle.kts              # Include modules
├── build.gradle.kts                 # Project-level build config
├── gradle/
│   └── libs.versions.toml           # Version catalog (all dependencies)
├── app/
│   ├── build.gradle.kts             # App-level build config
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml  # App manifest
│       │   ├── java/com/nirkids/app/
│       │   │   ├── NirKidsApp.kt         # Application class
│       │   │   ├── di/
│       │   │   │   └── AppModule.kt    # Hilt dependency modules
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AlphabetsDatabase.kt  # Room DB
│       │   │   │   │   ├── AlphabetDao.kt        # Alphabet DAO
│       │   │   │   │   └── ProgressDao.kt        # Progress DAO
│       │   │   │   ├── model/
│       │   │   │   │   ├── AlphabetEntity.kt     # Room entity
│       │   │   │   │   └── ProgressEntity.kt     # Progress entity
│       │   │   │   └── repository/
│       │   │   │       └── AlphabetRepository.kt  # Data impl
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── Alphabet.kt           # Domain model
│       │   │   │   │   ├── LetterProgress.kt      # Progress model
│       │   │   │   │   └── ParentGateState.kt     # Gate state
│       │   │   │   ├── repository/
│       │   │   │   │   └── IAlphabetRepository.kt # Repository interface
│       │   │   │   └── usecase/
│       │   │   │       ├── GetAlphabetUseCase.kt
│       │   │   │       ├── GetRandomLetterUseCase.kt
│       │   │   │       ├── MarkLetterLearnedUseCase.kt
│       │   │   │       ├── GetProgressUseCase.kt
│       │   │   │       └── ParentGateValidateUseCase.kt
│       │   │   ├── ui/
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt            # Color palette
│       │   │   │   │   ├── Type.kt             # Typography
│       │   │   │   │   └── Theme.kt            # App theme
│       │   │   │   ├── main/
│       │   │   │   │   ├── MainActivity.kt     # Entry point
│       │   │   │   │   ├── nav/
│       │   │   │   │   │   └── NavGraph.kt     # Navigation
│       │   │   │   │   ├── screens/
│       │   │   │   │   │   ├── HomeScreen.kt
│       │   │   │   │   │   ├── AlphabetScreen.kt
│       │   │   │   │   │   ├── PronunciationScreen.kt
│       │   │   │   │   │   └── ParentGateScreen.kt
│       │   │   │   │   ├── viewmodel/
│       │   │   │   │   │   ├── HomeViewModel.kt
│       │   │   │   │   │   ├── AlphabetViewModel.kt
│       │   │   │   │   │   ├── PronunciationViewModel.kt
│       │   │   │   │   │   └── ParentGateViewModel.kt
│       │   │   │   │   └── components/
│       │   │   │   │       └── LetterCard.kt
│       │   │   │   └── utils/
│       │   │   │       ├── TtsHelper.kt            # Text-to-speech
│       │   │   │       ├── VibrationHelper.kt      # Haptic feedback
│       │   │   │       └── AccessibilityHelper.kt  # A11y utilities
│       │   │   └── validator/
│       │   │       └── TraceValidator.kt     # Audit trail
│       │   └── res/                          # Resources
│       ├── test/                             # Unit tests
│       └── androidTest/                      # UI tests
├── tracevalidator/                         # TraceValidator library module
│   ├── build.gradle.kts
│   └── src/main/java/com/nirkids/tracevalidator/
├── proguard-rules.pro                      # Obfuscation rules
└── README.md                               # This file
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Hedgehog or later)
- **JDK 17+**
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-org/nirkids.git
cd nirkids

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumentation tests
./gradlew connectedAndroidTest
```

### Open in Android Studio
1. Launch Android Studio
2. **File > Open** → Navigate to `./NirKids`
3. Wait for Gradle sync to complete
4. Connect an Android device or start an emulator (API 26+)
5. Run with the green ▶️ button

---

## 🔐 ParentGate Deep Dive

The ParentGate feature prevents children from accidentally accessing parent settings. It uses two verification methods:

### Method 1: Math Puzzle
- Randomly generated arithmetic problems (addition or subtraction)
- Numbers in range: 1–99 (configurable)
- 3 attempts allowed before 60-second lockout

### Method 2: PIN Entry
- 4-digit numeric PIN as a secondary verification method
- Entered via an on-screen number pad
- Can be configured in parent settings

### Security Notes
- Parent PIN should be stored using **Android Keystore System** (TBD)
- Lockout timer prevents brute-force attacks
- All gate interactions are logged via **TraceValidator** for audit purposes

---

## ♿ Accessibility Features

| Feature | Description |
|---------|-------------|
| **TTS Pronunciation** | Every letter is spoken aloud via Android's TextToSpeech API |
| **Vibration Feedback** | Haptic feedback on every letter tap for hearing-impaired users |
| **Color-Coded Letters** | Vowels = Red/Pink, Consonants = Blue/Green for visual differentiation |
| **Large Text** | Letter cards use 48–120sp font sizes for readability |
| **High Contrast Mode** | Available in theme configuration |
| **Phonetic Breakdown** | Step-by-step phonetic segments for each letter |
| **Sign Language Hints** | ASL hand position hints for key letters |

---

## 🧪 Testing (TDD)

This project follows **Test-Driven Development** principles. All ViewModels, UseCases, and Repository implementations are covered by unit tests.

### Run All Tests
```bash
./gradlew test
```

### Run Only ViewModel Tests
```bash
./gradlew testDebugUnitTest --tests "*ViewModelTest"
```

### Run Only UseCase Tests
```bash
./gradlew testDebugUnitTest --tests "*UseCaseTest"
```

### Run UI Tests
```bash
./gradlew connectedAndroidTest
```

---

## 📖 TraceValidator

TraceValidator is a custom audit trail framework that logs all user interactions and data changes within the app.

### Key Classes
- **TraceValidator** — Main entry point, manages log lifecycle
- **TraceEntry** — Single audit record with timestamp, event type, entity, and metadata
- **TraceLog** — Log storage and retrieval (SQLite-backed)

### Usage Example
```kotlin
TraceValidator.logEvent("letter_learned", mapOf(
    "letter" to "A",
    "context" to "HomeScreen"
))
```

---

## 🎨 Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| 🔴 LetterRed | `#FF5252` | Vowels, error states |
| 🟠 LetterOrange | `#FFAB40` | Warnings, attempts |
| 🟡 LetterYellow | `#FFEA00` | Success, praise |
| 🟢 LetterGreen | `#69F0AE` | Mastery, pronunciation |
| 🔵 LetterCyan | `#00E5FF` | Accent, highlights |
| 🔵 LetterBlue | `#448AFF` | Consonants, primary |
| 🟣 LetterPurple | `#B388FF` | ParentGate, secondary |

---

## 📱 Screens Overview

### Home Screen
- Welcome banner
- Learning stats (Learned / Practicing / New / Total)
- Progress bar with percentage
- Alphabet grid preview (first 6 letters)
- Quick access to Alphabet and Pronunciation screens

### Alphabet Screen
- 4-column grid of all 26 letters
- Tap any letter for detailed view
- Color-coded (vowels vs consonants)
- Progress indicators per letter
- Play pronunciation button
- Mark as learned button

### Pronunciation Screen
- Letter with emoji illustration
- Phonetic breakdown in segments
- Listen button (TTS playback)
- Practice mode (type what you hear)
- Feedback: 🎉 Perfect / ✨ Great / 💪 Try Again
- Next letter button

### ParentGate Screen
- Math puzzle with random operands
- Number pad input
- PIN entry option
- Lockout timer display
- Success confirmation screen

---

## 📦 Dependencies

### Core
- **Kotlin** 2.1.x
- **Android Gradle Plugin** 8.x
- **Compose BOM** 2025.x

### Android Libraries
- **Hilt** — Dependency injection
- **Room** — Local database
- **Navigation Compose** — Screen navigation
- **ViewModel + LiveData** — State management
- **WorkManager** — Background tasks

### Testing
- **JUnit 4** — Unit testing framework
- **MockK** — Kotlin mocking library
- **Robolectric** — JVM-based Android testing
- **Espresso** — UI instrumentation testing

---

## 🔮 Future Enhancements

- [ ] Android Keystore PIN encryption for ParentGate
- [ ] Sign language video integration (ASL letters)
- [ ] Multi-child profiles with separate progress
- [ ] Custom letter sets (uppercase + lowercase)
- [ ] Dark mode support
- [ ] Sound effects (letter sounds beyond TTS)
- [ ] Printable alphabet cards export
- [ ] Parent notifications (weekly progress reports)
- [ ] Voice recording comparison for speech therapy
- [ ] Localization (multi-language support)

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

**Code Style:** Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

---

## 📄 License

This project is proprietary. All rights reserved.

---

## 👨‍💻 Built With

- **Created by:** Niraj Kumar
- **Project:** NirKids — Teaching kids their ABCs with love 💛
- **Architecture:** MVVM + Clean Architecture + TDD
- **Security:** TraceValidator audit trail + ParentGate protection

---

<p align="center">
  Made with ❤️ for kids everywhere
</p>
