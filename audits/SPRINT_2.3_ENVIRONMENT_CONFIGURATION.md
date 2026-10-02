# Sprint 2.3 — Environment Configuration

## Sprint

- **Sprint ID:** 2.3
- **Sprint name:** Environment Configuration
- **Phase:** 2 — Android Application Foundation

## Objective

Establish a generic, distinguishable Development/Testing/Production environment identity, and
confirm no API URL, secret, credential, or production key is hardcoded anywhere in the project.

## Documentation analyzed

- `docs/ROADMAP.md` — Sprint 2.3 required separation (Development/Testing/Production) and the
  "do not hardcode API URLs/secrets/credentials/production keys" constraint; Phase 2 edge cases
  (cold/warm start, process restart, first launch, corrupted/missing configuration, debug vs release).
- `docs/07_TECHNICAL_ARCHITECTURE.md` — section 34 (Configuration Management: no hardcoded
  environment values, no embedded secrets), section 59 (Build Configuration — build variants are
  `PENDING`), section 60 (Environment Structure — exact environment setup is `PENDING` backend
  decisions), section 35 (Security — no API secrets/credentials/signing secrets in source/APK).
- `docs/08_DATA_AND_API.md` — section 76 (development data must be isolated from production data;
  final environment architecture `PENDING`).
- `docs/12_TESTING_AND_QA.md` — section 62 (Test Environment Separation: distinguish
  Development/Testing/Production; exact architecture `PENDING`).
- `docs/15_ANTIGRAVITY_RULES.md` — section 72 (Environment Rule: dev/testing/production
  configurations must remain distinguishable), section 71 (Hardcoded Data Rule), section 58
  (Dependency Rules).
- `docs/00_MASTER_SPEC.md` — section 44 (Known Open Decisions lists "environment configuration" as
  an unresolved technical area; this sprint implements only the roadmap's bounded sub-task).
- `decisions/TECH-002-004_SDK_TOOLCHAIN_AND_PROJECT_LOCATION.md`,
  `audits/SPRINT_2.1_ANDROID_PROJECT.md`, `audits/SPRINT_2.2_APP_STARTUP.md`,
  `audits/SPRINT_1.5_DEPENDENCY_INJECTION_COMPOSITION.md` — existing build/DI boundaries reused.

## Repository analysis

No `core/config` package, environment type, or Hilt `@Module` existed previously (Sprint 1.5
deliberately deferred modules until a concrete binding was needed). `buildFeatures.buildConfig` was
not yet enabled, so no `BuildConfig` class was generated. A full-project search for API URLs,
secrets, keys, and credentials in source found only framework/XML-namespace/documentation URLs and
generated build artifacts — no production value exists to isolate yet.

## Implementation

### Configuration (core layer)

- Added `AppEnvironment` (`Development`/`Testing`/`Production`) — carries no endpoint, credential, or
  secret.
- Added `EnvironmentProvider` contract and `BuildTypeEnvironmentProvider`, which resolves
  `Development`/`Production` from `BuildConfig.DEBUG`. `Testing` is intentionally unreachable from
  this provider (see scope record) because no dedicated test build variant exists; introducing one
  would resolve the still-`PENDING` build-variant decision without approval.
- Enabled `buildFeatures.buildConfig = true` (previously unset) so `BuildConfig.DEBUG` is generated;
  no other build-type/variant change was made.

### Dependency initialization

- Added `ConfigModule`, the project's first Hilt `@Module`, binding `EnvironmentProvider` via
  `BuildConfig.DEBUG` — justified now that a concrete contract exists (Sprint 1.5 deferred modules
  until this point).

### Testing

- Unit tests for `AppEnvironment` distinctness and `BuildTypeEnvironmentProvider` (debug → `Development`,
  non-debug → `Production`).
- `EnvironmentProviderEntryPoint` (a `@EntryPoint` in `core/config`, main source set — Hilt entry
  points must be compiled into the app's own component to be resolvable from the instrumentation
  APK) plus an instrumented `EnvironmentProviderIntegrationTest` proving the real Hilt graph resolves
  the debug instrumentation build to `Development`.

## Files changed

### Added

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/config/AppEnvironment.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/config/EnvironmentProvider.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/config/BuildTypeEnvironmentProvider.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/config/EnvironmentProviderEntryPoint.kt`
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/config/di/ConfigModule.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/core/config/AppEnvironmentTest.kt`
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/core/config/BuildTypeEnvironmentProviderTest.kt`
- `DailyBattle/app/src/androidTest/java/com/sanket_satpute_20/dailybattle/core/config/EnvironmentProviderIntegrationTest.kt`
- `decisions/SPRINT-2.3_ENVIRONMENT_CONFIGURATION_SCOPE.md`
- `audits/SPRINT_2.3_ENVIRONMENT_CONFIGURATION.md` — this report.

### Modified

- `DailyBattle/app/build.gradle.kts` — enabled `buildFeatures.buildConfig` so `BuildConfig.DEBUG` is
  generated and available to `ConfigModule`.

### Deleted

None.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| Distinguishable Development/Testing/Production | PASS | `AppEnvironment` enum defines all three; `EnvironmentProviderIntegrationTest` proves the debug build resolves to `Development` through the real Hilt graph. |
| No hardcoded API URLs/secrets/credentials/production keys | PASS | Project-wide search found none in source (only framework/XML-namespace/doc URLs and generated build output); no such value was introduced by this sprint. |
| Configuration not hardcoded into UI/domain | PASS | `AppEnvironment`/`EnvironmentProvider` live in `core/config`; no UI or domain class reads `BuildConfig` directly. |
| Reuse existing DI boundary instead of ad hoc singleton | PASS | `ConfigModule` uses the existing Hilt `SingletonComponent` established in Sprint 1.5; no manual object graph was introduced. |
| No premature resolution of pending build-variant/backend decisions | PASS | `decisions/SPRINT-2.3_ENVIRONMENT_CONFIGURATION_SCOPE.md` records that `Testing` is not wired to a build variant and no API endpoint was added. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + unit tests | PASS | `:app:testDebugUnitTest` — 15/15 tests passed (3 new: `AppEnvironmentTest`, 2× `BuildTypeEnvironmentProviderTest`). |
| Release build | PASS | `:app:assembleRelease` succeeded; confirms `BuildConfig.DEBUG = false` compiles correctly for the release variant. |
| Connected instrumentation | PASS | `:app:connectedDebugAndroidTest` — 5/5 tests passed on moto g85 5G (Android 16), including the new `EnvironmentProviderIntegrationTest`. |
| Static analysis | PASS | `:app:lintDebug` — zero errors; 12 pre-existing warnings unchanged. |
| Manual cold start (post-change) | PASS | `adb am force-stop` + `am start` after reinstalling the debug build: `MainActivity` reached `ResumedActivity`. |
| Debug vs release configuration | PASS | Both `:app:assembleDebug` and `:app:assembleRelease` succeed; `BuildTypeEnvironmentProviderTest` unit-covers both branches since release is not installable without a signing config (pre-existing, unrelated to this sprint). |
| Corrupted/missing local configuration | NOT APPLICABLE | No persisted local configuration exists yet (`TECH-PENDING-007` preferences storage is still pending); nothing to corrupt or miss. |

## Edge cases verified

- Debug build resolves to `AppEnvironment.Development` via the real, on-device Hilt graph.
- `BuildTypeEnvironmentProvider` resolves both `isDebugBuild = true` and `= false` deterministically.
- Release variant compiles and packages successfully with `buildConfig` enabled (no regression from
  the new build feature).
- Cold start still reaches `RESUMED` after introducing the project's first Hilt `@Module`.
- A project-wide search for API URLs/secrets/credentials found none introduced or pre-existing.

## Dependencies

New dependencies: None. `buildFeatures.buildConfig = true` is a built-in AGP feature flag, not an
external dependency.

## Deviations

None.

## Pending / blocked items

Concrete per-environment configuration (API base URLs, backend endpoints) remains blocked on
`TECH-PENDING-008` (backend provider/architecture) and `TECH-PENDING-009` (network client). The
exact build-variant/flavor architecture remains `PENDING` per `07_TECHNICAL_ARCHITECTURE.md` section
59; `AppEnvironment.Testing` is therefore not yet reachable outside test code. Local
preferences/configuration persistence (`TECH-PENDING-007`) remains pending, so the "corrupted/missing
configuration" edge case stays not applicable until that sprint.

## Self-audit

1. **Only requested sprint?** Yes — only the environment-identity boundary and a hardcoded-secret
   audit were implemented.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No; no API client, backend URL, or build flavor was added.
4. **Anything removed?** No.
5. **Unrelated functionality modified?** No.
6. **Unapproved dependency introduced?** No; only a built-in AGP build feature flag was enabled.
7. **Pending decision silently resolved?** No; build-variant and backend/environment decisions
   remain explicitly pending per the new scope record.
8. **All relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — every changed/added file is limited to the environment
    boundary, its tests, the one-line build feature flag, and this report/scope record.

## Final status

READY FOR NEXT SPRINT
