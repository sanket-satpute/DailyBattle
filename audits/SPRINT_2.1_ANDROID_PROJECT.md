# Sprint 2.1 — Android Project

## Sprint

- **Sprint ID:** 2.1
- **Sprint name:** Android Project
- **Phase:** 2 — Android Application Foundation

## Objective

Verify the existing Daily Battle Android project shell against the approved Android SDK/toolchain
and project-location contracts, then establish a clean checkpoint without adding product behavior.

## Documentation analyzed

- `docs/ROADMAP.md` — Phase 2 objective and Sprint 2.1 requirements.
- `docs/00_MASTER_SPEC.md` — Android implementation readiness and pending architecture status.
- `docs/07_TECHNICAL_ARCHITECTURE.md` — application/layer boundaries and pending SDK/toolchain
  decisions.
- `docs/12_TESTING_AND_QA.md` — testing governance and evidence expectations.
- `docs/15_ANTIGRAVITY_RULES.md` — pending-decision, scope, dependency, and Git rules.
- `docs/16_DEFINITION_OF_DONE.md` — dependency, architecture, verification, and pending-decision
  definition of done.
- `decisions/TECH-001-005_COMPOSE_AND_APPLICATION_NAMESPACE.md` — previously approved Compose
  and application identity.
- `decisions/TECH-010_HILT_DEPENDENCY_INJECTION.md` — previously approved Hilt composition.
- `audits/SPRINT_0.2_ANDROID_DEVELOPMENT_ENVIRONMENT.md` and
  `audits/SPRINT_0.3_GIT_DEVELOPMENT_BASELINE.md` — pre-existing project/environment evidence and
  recorded location deviation.
- `decisions/TECH-002-004_SDK_TOOLCHAIN_AND_PROJECT_LOCATION.md` — current explicit approvals.

## Repository analysis

The approved Android Gradle project already exists at `DailyBattle/`. It has one `:app` application
module, a manifest, Gradle wrapper/version catalog, `DailyBattleApplication`, `MainActivity`,
resources, a theme, unit tests, device tests, and the Hilt composition established in Sprint 1.5.
The approved values already match the checked-in configuration, so changing source/build files
would be unnecessary and outside this verification-focused sprint.

## Implementation

### Project contracts

- Recorded the explicit approval of min SDK 30, compile/target SDK 37, Kotlin 2.2.10, AGP 9.3.3,
  and Gradle 9.5.0.
- Accepted `DailyBattle/` as the approved Android project location and resolved
  `DEV-STRUCTURE-001` without a migration.

### Verified existing project shell

- `:app` is included by `settings.gradle.kts`; `app/build.gradle.kts` configures the application
  namespace, application ID, SDKs, Compose, Hilt/KSP, debug/release builds, and test runner.
- The manifest registers the Hilt application root, exported launcher activity, app label, icons,
  theme, backup rules, and RTL support.
- Resources include application strings, launcher assets, XML backup/data-extraction rules, and the
  current theme foundation.

No UI design, gameplay, navigation, data, API, persistence, authentication, analytics, permissions,
or dependencies were added.

## Files changed

### Added

- `decisions/TECH-002-004_SDK_TOOLCHAIN_AND_PROJECT_LOCATION.md` — records the approved SDK,
  toolchain, and project-location contracts.
- `audits/SPRINT_2.1_ANDROID_PROJECT.md` — sprint report.

### Modified / deleted

- None.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| Application module | PASS | `DailyBattle/settings.gradle.kts` includes `:app`; app module builds successfully. |
| Manifest | PASS | Manifest declares application root, launcher activity, icons, label, theme, and backup rules. |
| Gradle configuration | PASS | Wrapper/version catalog/module build scripts match all approved values. |
| Application entry point | PASS | `DailyBattleApplication` and exported `MainActivity` initialize and launch on device. |
| Resources and themes | PASS | String, launcher, XML, color, and theme resources resolve in debug build. |
| Approved SDK/toolchain versions | PASS | Recorded decision matches `minSdk=30`, compile/target SDK 37, Kotlin 2.2.10, AGP 9.3.3, and Gradle 9.5.0. |
| Approved project location | PASS | `DailyBattle/` is explicitly accepted; no relocation made. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + unit tests | PASS | `:app:testDebugUnitTest :app:assembleDebug` passed; 10 local tests, 0 failures, 0 errors. |
| Connected instrumentation | PASS | Retry of `:app:connectedDebugAndroidTest` passed 2/2 tests on the attached moto g85 5G (Android 16). An earlier run was externally cancelled by a Gradle daemon stop before tests executed. |
| Manual cold start | PASS | Debug APK installed and `MainActivity` cold-started successfully. |
| Static analysis | PASS | `:app:lintDebug` completed with zero errors; 12 pre-existing warnings are unchanged. |
| UI / accessibility / navigation / lifecycle feature behavior | NOT APPLICABLE | Sprint verifies the project shell and does not add a product screen or interaction. |

## Edge cases verified

- The Gradle wrapper and all approved toolchain values resolve together.
- The manifest application/launcher entry point installs and launches on physical hardware.
- Repeated app cold starts retain successful initialization.
- Generated build output and local SDK configuration remain excluded from version control.

## Dependencies

New dependencies: None.

## Deviations

None. `DEV-STRUCTURE-001` is resolved by the explicit accepted project-location contract.

## Pending / blocked items

TECH-PENDING-002, TECH-PENDING-003, TECH-PENDING-004, and DEV-STRUCTURE-001 are resolved by
explicit user approval. All other pending decisions remain unchanged.

## Self-audit

1. **Only requested sprint?** Yes.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No.
4. **Anything removed?** No.
5. **Unrelated functionality modified?** No.
6. **Unapproved dependency introduced?** No.
7. **Pending decision silently resolved?** No; only explicit user-approved decisions are recorded.
8. **All relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — the two changed files record only the approved contracts and this audit.

## Final status

Ready for checkpoint and push.
