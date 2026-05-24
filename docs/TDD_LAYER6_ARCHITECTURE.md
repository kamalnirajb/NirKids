# TDD Layer 6 — Testing Architecture Patterns

**Difficulty:** Advanced | **Time:** ~90 min | **Prerequisites:** Layers 1-5 complete

Now that you know how to test each layer individually, this layer covers testing the **patterns** that hold your app together: Dependency Injection with Hilt, Clean Architecture boundaries, and multi-layer integration tests.

## Why Architecture Tests Matter

Without architecture tests, it's easy for your codebase to drift into "big ball of mud":
- ViewModels directly create Repository instances
- Use cases bypass the repository and call DAOs
- UI code depends on data models instead of domain models

Architecture tests prevent this drift.

## TDD Practice 1: Architecture Test — Clean Architecture Boundaries

Create: `app/src/test/java/com/nirkids/app/architecture/ArchitectureTest.kt`

This test verifies that your layers don't leak into each other. Use the **ArchUnit** library (add to `libs.versions.toml`):

```toml
archunit = "1.4.0"

[versions]
archunit = "1.4.0"

[libraries]
archunit-junit = { group = "com.github.sonyflake", name = "archunit-junit4-kotlin2", version.ref = "archunit" }
```

And in `app/build.gradle.kts`:
```kotlin
testImplementation(libs.archunit.junit)
```

```kotlin
package com.nirkids.app.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRule
import org.junit.Test

class ArchitectureTest {

    private val classes = ClassFileImporter().importPackages("com.nirkids.app")

    @Test
    fun `view models should only depend on use cases`() {
        val rule: ArchRule = com.tngtech.archunit.library.dependencies.rules.DependencyRules
            .classes()
            .that().resideInAPackage("..ui..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "..ui..", "androidx..", "kotlinx..")

        rule.check(classes)
    }

    @Test
    fun `use cases should only depend on repository interfaces`() {
        val rule: ArchRule = com.tngtech.archunit.library.dependencies.rules.DependencyRules
            .classes()
            .that().resideInAPackage("..domain.usecase..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "..repository..", "kotlinx..")

        rule.check(classes)
    }

    @Test
    fun `data layer should not depend on presentation layer`() {
        val rule: ArchRule = com.tngtech.archunit.library.dependencies.rules.DependencyRules
            .classes()
            .that().resideInAPackage("..data..")
            .should().notDependOnAnyClassesThat()
            .resideInAnyPackage("..ui..", "..model..")

        rule.check(classes)
    }
}
```

## TDD Practice 2: Hilt Testing — Providing Test Doubles

When your code uses Hilt DI, unit tests can't easily inject mocks. Two approaches:

### Approach A: Constructor Injection (Recommended for Testing)

Keep your ViewModel's `@Inject constructor` but create a **secondary constructor** for testing:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlphabetUseCase: GetAlphabetUseCase,
    private val getProgressUseCase: GetProgressUseCase,
    private val seedDataUseCase: SeedDataUseCase
) : ViewModel() {
    // ...
}
```

In tests, create the ViewModel **directly** with mock dependencies (as shown in Layer 3). No Hilt needed.

### Approach B: Hilt Android Test (For Instrumented Tests)

For instrumented (androidTest) tests that need Hilt:

```kotlin
package com.nirkids.app.ui.screens

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenHiltTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // ... tests here
}
```

## TDD Practice 3: Testing Dependency Injection Graph

Verify that Hilt can actually bind all your dependencies:

```kotlin
package com.nirkids.app.di

import com.nirkids.app.ui.main.viewmodel.HomeViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AppModuleTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var viewModel: HomeViewModel  // Should be injectable

    @Test
    fun `Hilt can provide HomeViewModel`() {
        hiltRule.inject()
        assert(viewModel != null)
    }
}
```

## Architecture Testing Checklist

- [ ] Domain layer has **no** Android dependencies (no Context, no View classes)
- [ ] Data layer imports **interfaces** from domain, not implementations
- [ ] Presentation layer imports **domain models**, not data models
- [ ] Each layer depends only on the layer below it
- [ ] Use cases are **interfaces** (for easier testing and swapping)
- [ ] ViewModel constructors are injectable (not nested)

## The Test Pyramid for NirKids

```
        ┌─────────────┐
       │  UI Tests    │  ← Few (slow, device-dependent)
      │  (Compose/Espresso) │
     ├─────────────┤
    │   Integration   │  ← Some (in-memory DB, real queries)
    │     Tests       │
   ├─────────────┤
  │    Unit Tests     │  ← Many (fast, JVM-only)
  │  (ViewModel,      │
  │   UseCase, Repo)  │
 └─────────────┴───────────────
```

**Rule of thumb:** 70% unit tests, 20% integration tests, 10% UI tests.

---

**Next step:** [Practical TDD Workflow](./TDD_PRACTICAL_WORKFLOW.md)
