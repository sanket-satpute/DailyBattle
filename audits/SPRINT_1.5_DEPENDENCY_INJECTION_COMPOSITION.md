# Sprint 1.5 — Dependency Injection / Composition

## Sprint

- **Sprint ID:** 1.5
- **Sprint name:** Dependency Injection / Composition
- **Phase:** 1 — Architecture Foundation

## Objective

Establish Hilt as the approved Android dependency-injection boundary without creating bindings for
unapproved repository, service, or configuration contracts.

## Documentation analyzed

- `docs/ROADMAP.md` — Sprint 1.5 scope and verification targets.
- `docs/07_TECHNICAL_ARCHITECTURE.md` — dependency inversion, UI/data separation, Hilt decision,
  configuration constraints, and `TECH-PENDING-010`.
- `docs/12_TESTING_AND_QA.md` — deterministic testing and fake/mock data constraints.
- `docs/15_ANTIGRAVITY_RULES.md` — dependency review, scope control, and pending-decision rules.
- `docs/16_DEFINITION_OF_DONE.md` — dependency, architecture, testability, and evidence checks.
- `decisions/TECH-001-005_COMPOSE_AND_APPLICATION_NAMESPACE.md` — prior Compose/package decision.
- `decisions/TECH-010_HILT_DEPENDENCY_INJECTION.md` — explicit user approval and dependency record.

## Repository analysis

The single `:app` module already has the approved logical layers but contains no repository
interfaces, data-source implementations, network client, database, authentication provider, service,
environment configuration, ViewModel, or feature use case. `MainActivity` is the only Android entry
point. Therefore Hilt's application and activity components are necessary; Hilt modules or binding
interfaces would be premature.

## Implementation

### Composition

- Added `DailyBattleApplication`, annotated with `@HiltAndroidApp`, and registered it in the
  manifest. This creates the application-level Hilt component.
- Annotated `MainActivity` with `@AndroidEntryPoint`, establishing the existing UI entry point in
  Hilt without injecting any feature dependency.
- Added the Hilt Gradle plugin, runtime, compiler, and KSP integration.
- Updated Java source/target compatibility to 17, required by the documented current Hilt/KSP setup.

### Boundaries and deferrals

- No service locator, ad-hoc global singleton, manual object graph, `@Module`, repository binding,
  network client, database, mock service, fake repository, environment value, secret, or feature
  behavior was added.
- Future concrete repository/service bindings and their test replacements remain deferred until their
  contracts are approved. Hilt will provide the replacement mechanism when those bindings exist.

## Files changed

### Added

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/DailyBattleApplication.kt` — Hilt
  application composition root.
- `decisions/TECH-010_HILT_DEPENDENCY_INJECTION.md` — resolves `TECH-PENDING-010` and records
  dependency review.
- `audits/SPRINT_1.5_DEPENDENCY_INJECTION_COMPOSITION.md` — this report.

### Modified

- `DailyBattle/gradle/libs.versions.toml` — Hilt and KSP versions, libraries, and plugins.
- `DailyBattle/build.gradle.kts` and `DailyBattle/app/build.gradle.kts` — apply Hilt/KSP and add
  Hilt compiler integration.
- `DailyBattle/app/src/main/AndroidManifest.xml` — declares `DailyBattleApplication`.
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/MainActivity.kt` — Hilt Android
  entry point.
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/ExampleInstrumentedTest.kt`
  — asserts the installed app uses the Hilt application root.

### Deleted

- None.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| `TECH-PENDING-010` resolved using Hilt | PASS | Explicit user approval recorded in `TECH-010_HILT_DEPENDENCY_INJECTION.md`; Hilt plugin/runtime/compiler are configured. |
| Application-level DI setup | PASS | `DailyBattleApplication` has `@HiltAndroidApp` and is manifest-registered. |
| Android composition entry point | PASS | `MainActivity` has `@AndroidEntryPoint`; Hilt generated component tasks pass. |
| Correct dependency direction | PASS | No domain code imports Android/Hilt; no UI-created network/database/client exists. |
| Testability / fake repositories / mock services | NOT APPLICABLE | No repository or service contract exists to inject, fake, or mock. Creating one would exceed sprint scope. |
| Environment configuration | NOT APPLICABLE | No approved environment/configuration contract exists; no values or secrets were introduced. |
| No unnecessary modules or unrelated architecture | PASS | The implementation contains no Hilt `@Module` or feature binding. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + unit tests | PASS | `:app:testDebugUnitTest :app:assembleDebug` passed; 10 local unit tests passed. |
| Connected instrumentation | PASS | `:app:connectedDebugAndroidTest` passed 2 tests on attached moto g85 5G (Android 16). |
| Hilt application runtime | PASS | Instrumentation test verifies `applicationContext is DailyBattleApplication`. |
| Manual startup | PASS | Debug APK installed and cold-started on the connected device; `MainActivity` launched with `Status: ok`. |
| Process recreation | PASS | Two force-stop/cold-start cycles both launched successfully with new app process IDs. |
| Static analysis | PASS | `:app:lintDebug` passed with zero errors; 12 pre-existing warnings remain. |

## Edge cases verified

- Hilt generation compiles for main, unit-test, and instrumentation-test source sets.
- Application initialization works from a cold start.
- Repeated process recreation does not prevent Hilt application initialization.
- An unavailable future dependency is not represented by a fake/default production binding.
- No concrete UI/data dependency is constructed at the Android UI entry point.

## Dependencies

| Dependency | Version | Purpose | Security / maintenance |
| --- | --- | --- | --- |
| Hilt Gradle plugin and runtime/compiler | 2.60.1 | Approved Android DI framework and generated component code. | Apache-2.0; no permissions, network behavior, secret handling, or data collection. |
| Kotlin Symbol Processing Gradle plugin | 2.3.12 | Required Hilt compiler integration because built-in Kotlin rejects KAPT. | Apache-2.0; build-time source generation only. |

## Deviations

None. KAPT was attempted during implementation but was rejected by the existing AGP built-in Kotlin
configuration; the supported KSP integration replaced it before acceptance. No KAPT change remains.

## Pending / blocked items

`TECH-PENDING-010` is resolved. All other technical and data decisions, including repository
contracts, services, persistence, network, authentication, and environment configuration, remain
pending and were not resolved by this sprint.

## Self-audit

1. **Only requested sprint?** Yes.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No.
4. **Anything removed?** No.
5. **Unrelated functionality modified?** No.
6. **Unapproved dependency introduced?** No; Hilt is user-approved and KSP is technically required by the existing built-in Kotlin setup.
7. **Pending decision silently resolved?** No; only `TECH-PENDING-010` was resolved by explicit user approval.
8. **All relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — changes are limited to Hilt/KSP build integration, the
    application and activity entry points, one instrumentation assertion, the DI decision record,
    and this audit.

## Final status

Ready for checkpoint and push.
