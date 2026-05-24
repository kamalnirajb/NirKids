# Unit and Mock Testing Guide

This guide covers how to write effective unit tests using MockK for mocking dependencies.

## Why Mock?
Mocking allows you to isolate the class under test by replacing its dependencies with controlled "mock" objects. This ensures your test only fails if the class itself has a bug, not its dependencies.

## Using MockK

### Basic Setup
In your `testImplementation` dependencies, we use `libs.mockk.android` and `libs.mockk.agent`.

### Example: Testing a ViewModel
```kotlin
class MyViewModelTest {
    // Mock the repository
    private val repository = mockk<MyRepository>()
    
    // The class under test
    private lateinit var viewModel: MyViewModel

    @Before
    fun setup() {
        viewModel = MyViewModel(repository)
    }

    @Test
    fun `when data is requested, repository is called`() {
        // 1. Arrange: Define behavior for the mock
        coEvery { repository.getData() } returns flowOf(listOf("Item 1"))

        // 2. Act: Trigger the action
        viewModel.loadData()

        // 3. Assert: Verify the result and interactions
        coVerify { repository.getData() }
        assertEquals(listOf("Item 1"), viewModel.uiState.value.items)
    }
}
```

## Using Robolectric
If your unit test needs access to Android resources or simple Android classes (like `Context`, `Intent`, or `Bundle`) without an emulator, use Robolectric.

### Example
```kotlin
@RunWith(RobolectricTestRunner::class)
class MyAndroidUnitTest {
    @Test
    fun `test using context`() {
        val context = RuntimeEnvironment.getApplication()
        val appName = context.getString(R.string.app_name)
        assertEquals("NirKids", appName)
    }
}
```

## Best Practices
1. **Name tests clearly:** Use backticks for descriptive names: `` `should return error when network fails` ``.
2. **One assertion per test:** Ideally, each test should verify one specific behavior.
3. **Don't mock everything:** Use real objects for simple data classes or utilities; mock complex dependencies like Databases, Network clients, or Repositories.
