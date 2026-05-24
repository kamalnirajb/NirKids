# Unit and Mock Testing Guide

This guide covers how to write effective unit tests using MockK for mocking dependencies and testing coroutines, based on the `AlphabetViewModelTest`, `HomeViewModelTest`, and `PronunciationViewModelTest` in this project.

## Using MockK

### Basic Setup
Ensure `libs.mockk.android` and `libs.mockk.agent` are in your `testImplementation`. Use `UnconfinedTestDispatcher` for immediate execution of coroutines in tests.

### Example: Mocking Use Cases in ViewModels
From `AlphabetViewModelTest.kt`:
```kotlin
@ExperimentalCoroutinesApi
class AlphabetViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockGetAlphabetUseCase: GetAlphabetUseCase
    private lateinit var viewModel: AlphabetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockGetAlphabetUseCase = mockk()

        // Defining behavior for the mock
        every { mockGetAlphabetUseCase() } returns flowOf(
            listOf(Alphabet('A', "/æ/", "Apple", "🍎", true))
        )

        viewModel = AlphabetViewModel(mockGetAlphabetUseCase, ...)
    }

    @Test
    fun `init loads all letters`() = runTest {
        val state = viewModel.uiState.value
        assertEquals('A', state.allLetters[0].letter)
    }
}
```

## Testing Entities and Data
When testing Room entities or Data classes (see `EntitiesTest.kt`):
- Ensure types match exactly. `AlphabetEntity` uses `String` for letters: `AlphabetEntity(letter = "A", ...)`.
- Verify defaults in constructors: `assertFalse(ProgressEntity("A").learned)`.

## Mocking System Services
From `VibrationHelperTest.kt`, using `relaxed = true` for mocks that don't need explicit behavior for every call:
```kotlin
@Test
fun `vibrate method can be called without crash`() {
    val mockContext = mockk<Context>()
    val mockVibrator = mockk<Vibrator>(relaxed = true)
    every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
    
    val helper = VibrationHelper(mockContext)
    helper.vibrate(10) // Should not throw
}
```

## Robolectric and Application Class
For tests requiring `Context` (see `NirKidsAppTest.kt`), explicitly define the application class to avoid `ClassCastException`:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O], application = NirKidsApp::class)
class NirKidsAppTest {
    @Test
    fun `application class instantiates correctly`() {
        val app = RuntimeEnvironment.getApplication() as NirKidsApp
        assertNotNull(app)
    }
}
```
