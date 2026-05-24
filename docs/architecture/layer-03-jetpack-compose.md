# Layer 03 — Jetpack Compose: Declarative UI

> Jetpack Compose lets you describe your UI with Kotlin code instead of XML layouts. Compose figures out what to update when state changes — you just declare what the UI should look like for any given state.

---

## Compose Basics — The "What, Not How" Principle

In traditional Android development, you **manually** find views and update them:

```kotlin
// Traditional (imperative) — tell Android HOW to build the UI
val textView = findViewById<TextView>(R.id.title)
textView.text = "Hello"
textView.visibility = View.VISIBLE
```

In Compose, you **declare** what the UI should look like:

```kotlin
// Compose (declarative) — tell Android WHAT the UI should be
@Composable
fun Greeting(name: String) {
    Text(text = name)  // Compose handles finding, creating, and updating the view
}
```

---

## Compose Building Blocks in NirKids

### 1. Composable Functions — The Units of UI

Every `@Composable` function is a reusable UI building block:

```kotlin
// From HomeScreen.kt — screen-level composables
@Composable
fun HomeScreen(
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(uiState, ...)
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("📚 NirKids") }) }
    ) { paddingValues ->
        Column(...) {
            // UI content
        }
    }
}
```

**Rule:** `@Composable` functions can only be called from other `@Composable` functions (or via `LaunchedEffect`, etc.). They are not regular Kotlin functions — they are UI templates.

### 2. Layout Composables — Structuring Your UI

```kotlin
// Column — stacks children vertically
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text("Welcome to NirKids!")
    Spacer(modifier = Modifier.height(8.dp))
    Text("Learn your ABCs!")
}

// Row — arranges children horizontally
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    StatCard(label = "Learned ✅", value = 5)
    StatCard(label = "Total 📊", value = 26)
}

// Box — overlays children
Box(contentAlignment = Alignment.Center) {
    Image(/* ... */)  // background
    Text("Overlay text")  // foreground
}
```

### 3. Lazy Layouts — Rendering Large Lists

```kotlin
// LazyVerticalGrid — grid of letters (from AlphabetScreen.kt)
LazyVerticalGrid(
    columns = GridCells.Fixed(4),  // 4 columns
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.padding(16.dp)
) {
    items(alphabetList, key = { it.letter }) { alphabet ->
        LetterCard(
            letter = alphabet.letter,
            color = letterColors[alphabet.letter - 'A' % 7],
            onClick = { onLetterTapped(alphabet.letter) }
        )
    }
}
```

**Why `key = { it.letter }`?** Compose uses the key to track which items changed between recompositions. Without a stable key, Compose has to rebuild everything.

### 4. Material 3 Components

```kotlin
// TopAppBar — app bar (from HomeScreen.kt)
TopAppBar(
    title = {
        Text("📚 NirKids", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    )
)

// Card — content container
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("🌈 Welcome to NirKids!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

// CircularProgressIndicator — loading indicator
if (uiState.isLoading) {
    CircularProgressIndicator(modifier = Modifier.size(48.dp))
}

// LinearProgressIndicator — progress bar
val progress = uiState.learnedCount.toFloat() / uiState.totalLetters
LinearProgressIndicator(
    progress = progress,
    modifier = Modifier.fillMaxWidth().height(12.dp),
    color = MasteryGreen,
    trackColor = MaterialTheme.colorScheme.surfaceVariant
)
```

---

## State in Compose — The Most Important Concept

### Remembered Mutable State

```kotlin
@Composable
fun AlphabetScreenContent(...) {
    var showLetterDetails by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Alphabet?>(null) }

    // When showLetterDetails changes, this Composable recomposes
    if (showLetterDetails) {
        LetterDetailView(selectedLetter!!)
    } else {
        LetterGrid(onLetterTapped = {
            selectedLetter = it
            showLetterDetails = true  // triggers recomposition
        })
    }
}
```

**Key:** `remember { mutableStateOf(false) }` creates state that survives recompositions. `by remember` is Kotlin property delegation — it lets you write `showLetterDetails = true` instead of `showLetterDetails.value = true`.

### Collecting State from ViewModel

```kotlin
@Composable
fun HomeScreen(...) {
    val uiState by viewModel.uiState.collectAsState()
    // uiState automatically updates when ViewModel emits new state
}
```

### Coroutines in Compose

```kotlin
@Composable
fun AlphabetScreen(...) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // When user taps "Mark as Learned"
    onMarkLearned = { letter ->
        scope.launch {
            viewModel.markAsLearned(letter)  // suspend function
            // UI updates automatically when uiState changes
        }
    }
}
```

---

## The Composition — How Compose Works

Compose has two phases:

```
Phase 1: COMPOSITION
┌──────────────────────────────────────────┐
│  Composable A                              │
│    ┌───────────────────────────────────┐  │
│    │  Composable B (called once)       │  │
│    │    ┌───────────────────────────┐  │  │
│    │    │  Composable C (called once)│  │  │
│    │    └───────────────────────────┘  │  │
│    └───────────────────────────────────┘  │
└──────────────────────────────────────────┘

Phase 2: RECOMPOSITION (when state changes)
┌──────────────────────────────────────────┐
│  Composable A (recomposed)                 │
│    ┌───────────────────────────────────┐  │
│    │  Composable B (skipped)           │  │  ← no state changed
│    │    ┌───────────────────────────┐  │  │
│    │    │  Composable C (skipped)   │  │  │
│    │    └───────────────────────────┘  │  │
│    │    ┌───────────────────────────┐  │  │
│    │    │  Composable D (rerun!)    │  │  ← state changed here
│    │    └───────────────────────────┘  │  │
│    └───────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

**Key principle:** Compose **re-runs** composables when their state dependencies change. The entire function body is re-executed with the new state — not incremental updates.

---

## Practical Compose Patterns from NirKids

### Pattern 1: Color Mapping

```kotlin
// Letter colors cycle through a palette
val letterColors = listOf(
    LetterRed, LetterOrange, LetterYellow,
    LetterGreen, LetterCyan, LetterBlue, LetterPurple
)

LetterMiniCard(
    letter = alphabet.letter,
    color = colors[index % colors.size],  // wraps around
    onClick = { onNavigateToAlphabet() }
)
```

### Pattern 2: Conditional UI

```kotlin
if (uiState.isLoading) {
    CircularProgressIndicator(modifier = Modifier.size(48.dp))
} else {
    // Show actual content
    Row(...) {
        StatCard(...)
        StatCard(...)
    }
    // Progress bar
    val progress = if (uiState.totalLetters > 0)
        uiState.learnedCount.toFloat() / uiState.totalLetters
    else 0f
    LinearProgressIndicator(progress = progress, ...)
}
```

### Pattern 3: Reusable Card Components

```kotlin
@Composable
fun StatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.8f))
        }
    }
}
```

### Pattern 4: Action Buttons with Icons

```kotlin
@Composable
fun actionButton(
    icon: ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description, fontSize = 14.sp, color = color.copy(alpha = 0.7f))
            }
        }
    }
}
```

### Pattern 5: Navigation Parameters

```kotlin
// NavGraph.kt — pass data through routes
composable(NavDestinations.ALPHABET) {
    AlphabetScreen(
        onNavigateBack = { navController.popBackStack() },
        ttsHelper = ttsHelper,
        vibrationHelper = vibrationHelper
    )
}
```

---

## Compose Previews — Design Without Running the App

```kotlin
@Preview(showBackground = true)
@Preview(name = "With Data")
@Composable
fun HomeScreenPreview() {
    NirKidsTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                totalLetters = 26,
                learnedCount = 5,
                inProgressCount = 10,
                newCount = 11
            ),
            onNavigateToAlphabet = {},
            onNavigateToPronunciation = {},
            onNavigateToParentGate = {}
        )
    }
}
```

---

## Compose + ViewModel = Clean Separation

```
ViewModel                    Compose Screen
═══════════                 ════════════

state: StateFlow<T>  ────▶  collectAsState()
                            ↓
                      Text(uiState.title)
                      Button(onClick = uiState.onAction)

actions: fun doSomething()  ◀───  Button(onClick = viewModel::doSomething)

ViewModel owns state.
Compose displays it.
User input goes back to ViewModel.
```

---

*Next: [Layer 04 — Jetpack Navigation](./layer-04-jetpack-navigation.md)*
