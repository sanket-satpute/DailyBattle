# Sprint 2.2 — App Startup

## Sprint

- **Sprint ID:** 2.2
- **Sprint name:** App Startup
- **Phase:** 2 — Android Application Foundation

## Objective

Implement the generic application startup sequence — dependency initialization reaching the
presentation layer, navigation initialization, and an explicit initial presentation state — without
building the primary app shell, bottom navigation, or any product screen reserved for later phases.

## Documentation analyzed

- `docs/ROADMAP.md` — Phase 2 objective, Sprint 2.2 required steps (startup → dependency
  initialization → navigation initialization → initial state), and listed edge cases.
- `docs/07_TECHNICAL_ARCHITECTURE.md` — section 30/31 (navigation follows `06_NAVIGATION_AND_FLOWS.md`
  and must not bypass official flows), section 66 (app shell/navigation are a later vertical slice
  than Android foundation), section 11/12 (explicit screen state, one source of truth), and section
  33 (dependency injection must preserve inversion).
- `docs/06_NAVIGATION_AND_FLOWS.md` — primary navigation destinations and pending navigation
  decisions (confirming bottom-navigation destinations are not yet approved for implementation).
- `docs/15_ANTIGRAVITY_RULES.md`, `docs/16_DEFINITION_OF_DONE.md` — scope, dependency, and
  verification rules.
- `decisions/TECH-001-005_COMPOSE_AND_APPLICATION_NAMESPACE.md`, `TECH-010_HILT_DEPENDENCY_INJECTION.md`
  — previously approved Compose/Hilt decisions this sprint builds on.
- `audits/SPRINT_1.3_PRESENTATION_STATE_ARCHITECTURE.md`, `audits/SPRINT_1.5_DEPENDENCY_INJECTION_COMPOSITION.md`
  — existing `ScreenState` and Hilt composition-root boundaries reused by this sprint.

## Repository analysis

`MainActivity` still rendered the default Compose project template (`Greeting`). The `navigation`
package was an empty logical boundary (Sprint 1.1). `ScreenState<T>` (Sprint 1.3/1.4) and the Hilt
application/activity roots (Sprint 1.5) already existed and were reused rather than duplicated. No
navigation library, view model, or startup state existed previously.

## Implementation

### Dependency initialization

- Added `AppStartupViewModel`, the first `@HiltViewModel` in the project, constructed with an empty
  `@Inject constructor()` — proving the Hilt graph now reaches the presentation layer, not only the
  `Application`/`Activity` roots.

### Navigation initialization

- Added `AppRoute`, a sealed navigation-destination contract with exactly one approved destination,
  `Startup`. Primary destinations (`Home`/`Battle`/`Friends`/`Me`) are explicitly deferred to the
  Phase 4 app-shell sprint per `07_TECHNICAL_ARCHITECTURE.md` section 66.
  `decisions/SPRINT-2.2_APP_STARTUP_SCOPE.md` records this boundary.
- Added `DailyBattleNavHost`, a `NavHost` registering only the startup destination.

### Initial state

- `AppStartupViewModel` exposes `StateFlow<ScreenState<Unit>>` (Sprint 1.3/1.4 boundary), initialized
  directly to `ScreenState.Success(Unit)` because no asynchronous dependency exists yet to await.
- Added `StartupScreen` (stateless, exhaustively renders all four `ScreenState` variants) and
  `StartupRoute` (Hilt-wired entry point using `hiltViewModel()`/`collectAsStateWithLifecycle()`).

### Application startup

- `MainActivity` now composes `DailyBattleTheme { Scaffold { DailyBattleNavHost(...) } }`, replacing
  the unused Compose project template (`Greeting`/`GreetingPreview`).

## Files changed

### Added

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/navigation/AppRoute.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/navigation/DailyBattleNavHost.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/presentation/startup/AppStartupViewModel.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/presentation/startup/StartupScreen.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/navigation/AppRouteTest.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/presentation/startup/AppStartupViewModelTest.kt`
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/MainActivityStartupTest.kt`
- `decisions/SPRINT-2.2_APP_STARTUP_SCOPE.md`
- `audits/SPRINT_2.2_APP_STARTUP.md` — this report.

### Modified

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/MainActivity.kt` — composes the
  new navigation host instead of the unused template.
- `DailyBattle/app/build.gradle.kts`, `DailyBattle/gradle/libs.versions.toml` — add
  `navigation-compose`, `hilt-navigation-compose`, and `lifecycle-runtime-compose`.

### Deleted

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/navigation/.gitkeep` — the
  package now contains real source files.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| Application startup | PASS | `MainActivity.onCreate` composes the theme and nav host; manual cold/warm start and instrumented tests confirm it reaches `RESUMED`. |
| Dependency initialization | PASS | `AppStartupViewModel` is constructed by Hilt (`@HiltViewModel`), the first presentation-layer Hilt binding. |
| Navigation initialization | PASS | `DailyBattleNavHost` registers `AppRoute.Startup` as the sole, start destination. |
| Initial state | PASS | `AppStartupViewModel.state` is explicit `ScreenState<Unit>`, resolved to `Success(Unit)`; unit test asserts it. |
| No primary/product navigation invented | PASS | Only `Startup` route exists; `decisions/SPRINT-2.2_APP_STARTUP_SCOPE.md` records the deferral of `Home`/`Battle`/`Friends`/`Me`. |
| Reuse of existing architecture boundaries | PASS | Reuses `ScreenState` (1.3/1.4) and the Hilt composition root (1.5) rather than duplicating them. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + unit tests | PASS | `:app:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL`; includes `AppRouteTest` and `AppStartupViewModelTest`. |
| Connected instrumentation | PASS | `:app:connectedDebugAndroidTest` — 4/4 tests passed on moto g85 5G (Android 16), including the two new `MainActivityStartupTest` cases (cold start to `RESUMED`, survives `recreate()`). |
| Static analysis | PASS | `:app:lintDebug` — zero errors; 12 pre-existing warnings unchanged (no new lint issues introduced). |
| Manual cold start | PASS | `adb am force-stop` + `am start`: `dumpsys activity activities` shows `MainActivity` as `ResumedActivity`. |
| Manual warm start | PASS | Backgrounded via `KEYCODE_HOME` then relaunched: same task (`t46543`) resumed without recreating the process. |
| Manual process restart / first launch | PASS | Force-stopped (kills process) then relaunched: a new task/activity record (`t46544`) reached `RESUMED`; no `FATAL`/`AndroidRuntime` entries in logcat. |
| Debug vs release configuration | NOT APPLICABLE | Environment/configuration separation is explicitly Sprint 2.3 scope; this sprint adds no environment-specific value. |
| Corrupted/missing local configuration | NOT APPLICABLE | No persisted local configuration exists yet to corrupt or miss; introduced by a later persistence/configuration sprint. |

## Edge cases verified

- Cold start reaches a resumed `MainActivity` after a full force-stop.
- Warm start (background via Home, then relaunch) resumes the existing task without creating a
  duplicate activity instance.
- Process restart (force-stop, relaunch) creates a new process/activity record and still reaches
  `RESUMED` with no crash in logcat.
- `MainActivity.recreate()` (configuration-change-style recreation) still resolves to `RESUMED`.
- The `ScreenState` `when` in `StartupScreen` is exhaustive over all four variants even though only
  `Success` is currently reachable, so a future `Loading`/`Error` emission cannot silently fall
  through unhandled.

## Dependencies

| Dependency | Version | Purpose | Security / maintenance |
| --- | --- | --- | --- |
| `androidx.navigation:navigation-compose` | 2.10.2 (latest stable) | Required to implement the roadmap's "navigation initialization" step for the approved Compose UI. | AndroidX, Apache-2.0; no network/permission/data behavior. |
| `androidx.hilt:hilt-navigation-compose` | 1.4.0 (latest stable) | Standard integration providing `hiltViewModel()` so the Compose nav host can obtain Hilt-constructed view models. | AndroidX, Apache-2.0; build/runtime wiring only. |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.11.0 (matches existing `lifecycle-runtime-ktx` version already in use) | Provides `collectAsStateWithLifecycle()` used directly in `StartupScreen`. | AndroidX, Apache-2.0. |

## Deviations

None.

## Pending / blocked items

Primary navigation destinations (`Home`/`Battle`/`Friends`/`Me`), the bottom navigation bar, and all
`NAV-PENDING-*` items remain unresolved and are explicitly out of this sprint's scope (Phase 4).
Environment/configuration separation remains Sprint 2.3 scope. Fixture-based test infrastructure
(fakes, test data factories) remains Sprint 2.4 scope. All `TECH-PENDING-*` items other than those
already resolved in prior sprints remain pending.

## Self-audit

1. **Only requested sprint?** Yes — only the startup/DI/navigation-initialization/initial-state
   sequence was implemented.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No; no primary destinations, product screens, or env config were
   added.
4. **Anything removed?** Only the unused Compose project template (`Greeting`/`GreetingPreview`) and
   a placeholder `.gitkeep`, both superseded by real sprint content.
5. **Unrelated functionality modified?** No.
6. **Unapproved dependency introduced?** No; all three additions are standard AndroidX integrations
   technically required for Compose navigation + Hilt + lifecycle-aware state collection, versioned
   to match the project's existing Compose/Hilt/lifecycle generation.
7. **Pending decision silently resolved?** No; navigation framework choice follows directly from the
   already-approved Compose decision (TECH-001/005) and introduces no product-navigation decision.
8. **All relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — every changed/added/deleted file is limited to the startup
    sequence, its tests, dependency wiring, and this report/scope record.

## Final status

READY FOR NEXT SPRINT
