# Sprint 2.4 — Base Test Infrastructure

## Sprint

- **Sprint ID:** 2.4
- **Sprint name:** Base Test Infrastructure
- **Phase:** 2 — Android Application Foundation

## Objective

Establish reusable unit test, integration test, UI test, fixture, fake, and test-data-factory
infrastructure the project can build on, without inventing repository contracts or product data that
do not yet exist.

## Documentation analyzed

- `docs/ROADMAP.md` — Sprint 2.4 required items (unit/integration/UI tests, test fixtures, fake
  repositories, test data factories) and Phase 2 edge cases.
- `docs/12_TESTING_AND_QA.md` — section 7 (Test Pyramid), section 8 (Unit Testing — deterministic
  logic, no invented formulas), section 43 (Test Data categories: `TEST_USER`, etc.), section 44
  (Deterministic Test Data / fixtures).
- `docs/16_DEFINITION_OF_DONE.md` — section 81 (Mock Backend DoD), section 82 (Test Data DoD:
  explicit, reproducible, isolated, non-sensitive).
- `docs/15_ANTIGRAVITY_RULES.md` — section 70 (Mock Data Rule: fakes must be isolated and not
  mistaken for real behavior), section 58 (Dependency Rules).
- `docs/07_TECHNICAL_ARCHITECTURE.md` — section 61 (Testing Architecture).
- `audits/SPRINT_1.2_DOMAIN_BOUNDARY.md`, `audits/SPRINT_2.2_APP_STARTUP.md`,
  `audits/SPRINT_2.3_ENVIRONMENT_CONFIGURATION.md` — existing domain identifiers and the only
  concrete injectable contract (`EnvironmentProvider`) reused by this sprint's fixtures.

## Repository analysis

No repository interface, fake, test-data factory, Hilt test runner/application, or coroutine test
dispatcher rule existed. The `testing` package (Sprint 1.1) was an empty placeholder. The project's
only concrete injectable contract is `EnvironmentProvider` (Sprint 2.3); its only concrete domain data
is the seven opaque identifiers (Sprint 1.2). No repository, use case, or domain entity with fields
exists, so there is nothing concrete to fake as a "repository" yet.

## Implementation

### Instrumentation test infrastructure

- Added `HiltTestRunner` (`androidTest`), swapping the instrumentation application for
  `HiltTestApplication`, and set it as `testInstrumentationRunner`. Added the
  `hilt-android-testing` dependency and `kspAndroidTest(hilt-compiler)` so `@HiltAndroidTest`,
  `HiltAndroidRule`, `@UninstallModules`, and `@BindValue` are available.

### Unit test infrastructure

- Added `MainDispatcherRule` (JVM `test` source set only, not shipped in the app) routing
  `Dispatchers.Main` to a `kotlinx-coroutines-test` dispatcher for future `viewModelScope`-based
  ViewModel tests. Added `kotlinx-coroutines-test` as a `testImplementation`-only dependency.

### Fixtures / fakes / test data factories

- Added `testing/FakeEnvironmentProvider` (main source, reusable from both `test` and `androidTest`)
  — a deterministic `EnvironmentProvider` double defaulting to `AppEnvironment.Testing`.
- Added `testing/TestIdentifiers.kt` — deterministic, explicitly-labeled factory functions
  (`testUserId`, `testBattleId`, etc.) for all seven existing domain identifiers.

### UI / integration test

- Added `EnvironmentProviderFakeBindingUiTest` (`@HiltAndroidTest`, `@UninstallModules(ConfigModule::class)`,
  `HiltAndroidRule`, `@BindValue FakeEnvironmentProvider`, `createAndroidComposeRule<MainActivity>()`)
  proving a UI test can substitute a fake binding end to end and that the app still renders.

### Required consequential fixes

- `HiltTestApplication` only creates its Hilt component once a test's `HiltAndroidRule` runs; any
  instrumented test that touches a Hilt-managed object (an `@AndroidEntryPoint` activity or an
  `@EntryPoint`) now requires `@HiltAndroidTest` + `HiltAndroidRule`. Added this to the pre-existing
  `MainActivityStartupTest` (Sprint 2.2) and `EnvironmentProviderIntegrationTest` (Sprint 2.3), which
  both crashed under the new test runner without it.
- Replaced `ExampleInstrumentedTest.appUsesHiltApplicationCompositionRoot` (asserted
  `application is DailyBattleApplication`) with `appUsesHiltTestApplicationCompositionRoot`
  (asserts `application is HiltTestApplication`), because the instrumentation application is
  necessarily `HiltTestApplication` once a Hilt test runner exists. The production app's actual
  composition root is still verified manually (see Tests) and was not changed.

## Files changed

### Added

- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/HiltTestRunner.kt`
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/core/config/EnvironmentProviderFakeBindingUiTest.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/testing/FakeEnvironmentProvider.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/testing/TestIdentifiers.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/testing/MainDispatcherRule.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/testing/MainDispatcherRuleTest.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/testing/TestIdentifiersTest.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/testing/FakeEnvironmentProviderTest.kt`
- `decisions/SPRINT-2.4_BASE_TEST_INFRASTRUCTURE_SCOPE.md`
- `audits/SPRINT_2.4_BASE_TEST_INFRASTRUCTURE.md` — this report.

### Modified

- `DailyBattle/app/build.gradle.kts` — Hilt test runner + `hilt-android-testing`/`kspAndroidTest`/
  `kotlinx-coroutines-test` dependencies.
- `DailyBattle/gradle/libs.versions.toml` — version catalog entries for the above.
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/ExampleInstrumentedTest.kt`
  — composition-root assertion updated for the new test application (see above).
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/MainActivityStartupTest.kt`
  — added required `@HiltAndroidTest`/`HiltAndroidRule`.
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/core/config/EnvironmentProviderIntegrationTest.kt`
  — added required `@HiltAndroidTest`/`HiltAndroidRule`.

### Deleted

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/testing/.gitkeep` — package now
  contains real source files.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| Unit test infrastructure | PASS | `MainDispatcherRule` + `MainDispatcherRuleTest`; `kotlinx-coroutines-test` added as `testImplementation`. |
| Integration test infrastructure | PASS | `EnvironmentProviderIntegrationTest` now runs under `@HiltAndroidTest`/`HiltAndroidRule`, proving real Hilt-graph integration testing works. |
| UI test infrastructure | PASS | `EnvironmentProviderFakeBindingUiTest` — Hilt-aware Compose UI test with a substituted fake binding. |
| Test fixtures | PASS | `testing/FakeEnvironmentProvider.kt`, `testing/TestIdentifiers.kt` in the shared `testing` package. |
| Fake repositories | NOT APPLICABLE | No repository interface exists yet (Sprint 1.2 deferred all repository contracts; `TECH-PENDING-006/007/008/009` remain pending). Fake-binding pattern demonstrated instead against the only existing injectable contract (`EnvironmentProvider`); documented in the scope record. |
| Test data factories | PASS | `testing/TestIdentifiers.kt` provides deterministic factories for all seven existing domain identifiers. |
| No invented product data/formulas | PASS | All test data uses the `test-*` naming convention from `12_TESTING_AND_QA.md` section 43; no score, scoring formula, or domain entity was invented. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + unit tests | PASS | `:app:testDebugUnitTest` — 21/21 tests passed (6 new: `MainDispatcherRuleTest`, 3× `TestIdentifiersTest`, 2× `FakeEnvironmentProviderTest`). |
| Release build | PASS | `:app:assembleRelease` succeeded. |
| Connected instrumentation | PASS | `:app:connectedDebugAndroidTest` — 6/6 tests passed on moto g85 5G (Android 16), including the new `EnvironmentProviderFakeBindingUiTest` and the fixed `ExampleInstrumentedTest`/`MainActivityStartupTest`/`EnvironmentProviderIntegrationTest`. |
| Static analysis | PASS | `:app:lintDebug` — zero errors; 12 pre-existing warnings unchanged. |
| Manual cold start (production app, not test APK) | PASS | After `adb force-stop` + `am start` on the reinstalled debug build, `dumpsys activity activities` showed `MainActivity` as `ResumedActivity` — confirms the Hilt test runner change does not affect the real `DailyBattleApplication` composition root. |

## Edge cases verified

- A plain instrumented test (no `@HiltAndroidTest`) that only inspects `applicationContext` type
  still passes under the new test runner.
- A Hilt-managed `@AndroidEntryPoint` activity launch fails fast with a clear Hilt error when
  `@HiltAndroidTest`/`HiltAndroidRule` is missing (discovered and fixed during this sprint) —
  confirmed the fix resolves it rather than masking it.
- `@UninstallModules` + `@BindValue` correctly replaces the production `ConfigModule` binding for one
  UI test without affecting any other test or the production app.
- The production (non-test) app still cold-starts correctly after the instrumentation runner change.
- `MainDispatcherRule` correctly installs and resets `Dispatchers.Main` around each test.
- Test identifier factories are deterministic for the same suffix and distinct for different suffixes.

## Dependencies

| Dependency | Version | Purpose | Security / maintenance |
| --- | --- | --- | --- |
| `com.google.dagger:hilt-android-testing` | 2.60.1 (matches the project's pinned Hilt version) | Provides `HiltTestApplication`, `@HiltAndroidTest`, `HiltAndroidRule`, `@UninstallModules`, `@BindValue`. | Same vendor/license as the already-approved Hilt runtime; test-only (`androidTestImplementation`), not shipped in the production APK. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.11.0 (latest stable) | Provides `TestDispatcher`/`setMain`/`resetMain` for `MainDispatcherRule`. | JetBrains, Apache-2.0; test-only (`testImplementation`), not shipped in the production APK. |

## Deviations

None.

## Pending / blocked items

Fake repositories and product-specific test data factories (`TEST_BATTLE`, `TEST_CHALLENGE`, etc.)
remain blocked until the corresponding repository/domain-entity contracts are approved
(`TECH-PENDING-006` through `TECH-PENDING-009`). The fake-binding pattern established here
(`@UninstallModules` + `@BindValue`) must be reused once those contracts exist.

## Self-audit

1. **Only requested sprint?** Yes — only generic test infrastructure was added; no product feature,
   repository contract, or screen was introduced.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No.
4. **Anything removed?** Only the now-incompatible `appUsesHiltApplicationCompositionRoot` assertion,
   replaced by an equivalent one for the new, correct composition root under test.
5. **Unrelated functionality modified?** No; the three modified instrumented test files were changed
   only because the Hilt test runner change made their previous assumptions invalid — this is a
   directly necessary consequence of the sprint, not scope creep.
6. **Unapproved dependency introduced?** No; both new dependencies are test-only, same-vendor
   (Hilt) or already-evaluated (kotlinx-coroutines) libraries, justified above.
7. **Pending decision silently resolved?** No; no repository contract was invented to have something
   to fake.
8. **All relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — every changed file is limited to test infrastructure, its
    required consequential fixes, and this report/scope record.

## Final status

READY FOR NEXT SPRINT
