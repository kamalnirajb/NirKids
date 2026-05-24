# Layer 04 — Jetpack Navigation Compose

> Navigation Compose manages the back stack of Compose screens. It routes between screens, passes arguments, and handles the system back button.

---

## Navigation in NirKids — The Map

NirKids has four screens. Here's the navigation map:

```
    ┌───────────────────────────────────────────┐
    │              MainActivity                  │
    │                                            │
    │  ┌────────────────────────────────────┐    │
    │  │            NavHost                  │    │
    │  │  (startDestination = "home")        │    │
    │  │                                      │    │
    │  │    "home" ──▶ HomeScreen             │    │
    │  │        │                             │    │
    │  │        ├─▶ "alphabet" ──▶ AlphabetScreen     │
    │  │        ├─▶ "pronunciation" ──▶ PronunciationScreen
    │  │        └─▶ "parent_gate" ──▶ ParentGateScreen
    │  │                                      │    │
    │  └────────────────────────────────────┘    │
    └───────────────────────────────────────────┘
```

---

## Destination Constants

```kotlin
// File: com/nirkids/app/ui/main/nav/NavGraph.kt
object NavDestinations {
    const val HOME = "home"
    const val ALPHABET = "alphabet"
    const val PRONUNCIATION = "pronunciation"
    const val PARENT_GATE = "parent_gate"
}
```

**Why constants?** Prevents typos. `"alphabet"` and `"alphabet "` are different routes — constants catch this at compile time.

---

## NavHost Configuration

```kotlin
// File: com/nirkids/app/ui/main/nav/NavGraph.kt
@Composable
fun NavGraph(
    navController: NavHostController,
    ttsHelper: TtsHelper,
    vibrationHelper: VibrationHelper
) {
    NavHost(
        navController = navController,      // manages the back stack
        startDestination = NavDestinations.HOME,  // first screen shown
        route = "main_nav"                  // optional: identifies this NavHost
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(
                onNavigateToAlphabet = { navController.navigate(NavDestinations.ALPHABET) },
                onNavigateToPronunciation = { navController.navigate(NavDestinations.PRONUNCIATION) },
                onNavigateToParentGate = { navController.navigate(NavDestinations.PARENT_GATE) }
            )
        }
        composable(NavDestinations.ALPHABET) {
            AlphabetScreen(
                onNavigateBack = { navController.popBackStack() },
                ttsHelper = ttsHelper,
                vibrationHelper = vibrationHelper
            )
        }
        composable(NavDestinations.PRONUNCIATION) {
            PronunciationScreen(
                onNavigateBack = { navController.popBackStack() },
                ttsHelper = ttsHelper
            )
        }
        composable(NavDestinations.PARENT_GATE) {
            ParentGateScreen(
                onDismiss = { navController.popBackStack() },
                onGateSuccess = { /* parent settings accessed */ }
            )
        }
    }
}
```

---

## Navigation Actions Explained

### Navigate Forward

```kotlin
// From HomeScreen.kt — go to Alphabet screen
onNavigateToAlphabet = {
    navController.navigate(NavDestinations.ALPHABET)
}
```

This pushes a new destination onto the back stack:
```
Before: [HomeScreen]
After:  [HomeScreen, AlphabetScreen]  ← user sees AlphabetScreen
```

### Pop Back (Navigate Back)

```kotlin
// From AlphabetScreen.kt — return to Home
onNavigateBack = {
    navController.popBackStack()
}
```

This removes the current destination:
```
Before: [HomeScreen, AlphabetScreen]
After:  [HomeScreen]  ← user sees HomeScreen
```

### Pop Back to Specific Destination

```kotlin
// Pop to Home and remove everything above it
navController.popBackStack(NavDestinations.HOME, inclusive = false)
```

---

## Passing Arguments Between Screens

### Method 1: Route Parameters (for simple data)

```kotlin
// Define route with path parameter
composable("alphabet_detail/{letter}") { backStackEntry ->
    val letter = backStackEntry.arguments?.getString("letter")?.first()
    AlphabetDetailScreen(
        selectedLetter = letter ?: 'A',
        onNavigateBack = { navController.popBackStack() }
    )
}

// Navigate with argument
navController.navigate("alphabet_detail/${selectedLetter}")
```

### Method 2: Navigation Options (for complex data)

```kotlin
// Navigate with a serialized argument
val bundle = bundleOf("letter" to "A", "phonetic" to "/æ/")
navController.navigate("alphabet_detail") {
    popUpTo(NavDestinations.HOME) { inclusive = false }  // remove Home from stack
    launchSingleTop = true  // don't create duplicate
}
```

### Method 3: Dependency Injection (for ViewModels)

In NirKids, ViewModels are injected per-screen. Navigation Compose handles this automatically:

```kotlin
// In NavGraph.kt
composable(NavDestinations.HOME) {
    HomeScreen(
        viewModel: HomeViewModel = hiltViewModel()  // ← Hilt creates unique instance per screen
    )
}
```

Each screen gets its own ViewModel instance. When you navigate away and back, the ViewModel survives (it's bound to the screen's lifecycle, not the back stack entry).

---

## Back Stack Management in NirKids

### Navigation Flow

```
User opens app
├── NavHost shows HomeScreen (startDestination)
│
├── User taps "🔤 Learn Alphabets"
│   └── navController.navigate("alphabet")
│       Back stack: [HomeScreen, AlphabetScreen]
│       User sees: AlphabetScreen
│
├── User taps letter card
│   └── (no navigation — screen updates in place)
│
├── User taps back arrow or system back button
│   └── navController.popBackStack()
│       Back stack: [HomeScreen]
│       User sees: HomeScreen
│
└── User taps "🗣️ Pronunciation" from HomeScreen
    └── navController.navigate("pronunciation")
        Back stack: [HomeScreen, PronunciationScreen]
        User sees: PronunciationScreen
```

### System Back Button

Navigation Compose automatically handles the system back button — it calls `popBackStack()` internally. You don't need to handle it manually.

### Preventing Double Navigation

```kotlin
// If user taps "Next" rapidly, prevent duplicate screens
navController.navigate(route) {
    launchSingleTop = true          // don't add duplicate
    popUpTo(navController.graph.startDestinationId) {
        inclusive = false           // keep the start destination
    }
}
```

---

## Navigation Composition — Where Nav Lives

```kotlin
// MainActivity.kt — the entry point
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NirKidsTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()  // create navigation controller
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

**Key insight:** `rememberNavController()` is scoped to the `setContent` block. It manages the entire app's navigation. Every screen in the NavGraph can navigate through it.

---

## Navigation Patterns Summary

| Pattern | When to Use | Example |
|---|-|---|
| **Simple route string** | No arguments needed | `navigate("home")` |
| **Route with path** | Simple data (IDs, letters) | `navigate("letter/${letter}")` |
| **popUpTo + launchSingleTop** | Reset to home screen | Profile → Settings → reset to Home |
| **popBackStack()** | Go back one screen | Alphabet → Home |
| **hiltViewModel()** | Screen-scoped ViewModel | Auto-injected in each screen |

---

## Common Navigation Mistakes

❌ **Creating NavHostController multiple times**

```kotlin
// BAD — each recomposition creates a new controller
@Composable
fun MyApp() {
    val navController = rememberNavController()  // OK in setContent block
    NavGraph(navController)
}
```

❌ **Not handling null routes**

```kotlin
// BAD
navController.navigate("alphabet")
// If "alphabet" doesn't exist in NavHost, this crashes silently
```

✅ **Always use constants**

```kotlin
// GOOD
navController.navigate(NavDestinations.ALPHABET)
// NavDestination.ALPHABET = "alphabet" — centralized, single source of truth
```

---

*Next: [Layer 05 — Hilt Dependency Injection](./layer-05-hilt-di.md)*
