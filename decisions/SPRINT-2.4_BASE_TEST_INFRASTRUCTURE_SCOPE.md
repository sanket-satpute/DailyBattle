# Sprint 2.4 — Base test infrastructure scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 2, Sprint 2.4), `docs/12_TESTING_AND_QA.md` sections
7 (Test Pyramid), 8 (Unit Testing), 43/44 (Test Data, Deterministic Test Data), and
`docs/16_DEFINITION_OF_DONE.md` sections 81/82 (Mock Backend DoD, Test Data DoD).

Sprint 2.4 establishes reusable, generic test infrastructure only:

- A Hilt-aware instrumentation test runner (`HiltTestRunner`, swapping the instrumentation
  application for `HiltTestApplication`), enabling `@HiltAndroidTest`/`@UninstallModules`/`@BindValue`.
- `MainDispatcherRule` (JVM unit-test-only) for future `viewModelScope`-based ViewModel tests.
- `FakeEnvironmentProvider` and deterministic `testing/TestIdentifiers.kt` factories, covering the
  only concrete injectable contract (`EnvironmentProvider`, Sprint 2.3) and the only concrete domain
  identifiers (Sprint 1.2) that currently exist.
- One Hilt UI test (`EnvironmentProviderFakeBindingUiTest`) proving end-to-end fake-binding
  substitution in a Compose UI test, since this is the only existing injectable contract.

**No fake repository is introduced** because no repository interface exists yet in the domain layer
(Sprint 1.2 deliberately deferred all repository contracts; `TECH-PENDING-006` through
`TECH-PENDING-009` — persistence, preferences, backend, network client — remain pending). Creating a
repository interface solely to have something to fake would invent an unapproved abstraction. The
fake-binding pattern is demonstrated instead against `EnvironmentProvider` and must be reused for
real repositories once their contracts are approved.

**Consequence of the Hilt test runner change:** switching `testInstrumentationRunner` affects every
instrumented test in the module, not only `@HiltAndroidTest` classes — `applicationContext` is now
`HiltTestApplication` for all instrumented tests. The pre-existing
`ExampleInstrumentedTest.appUsesHiltApplicationCompositionRoot` assertion (`application is
DailyBattleApplication`) is no longer true under test and was replaced with an equivalent assertion
against `HiltTestApplication`, which is the correct and expected composition root for instrumented
tests once Hilt test infrastructure exists.
