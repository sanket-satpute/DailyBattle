# Sprint 1.4 — Error Architecture

## Sprint

- **Sprint ID:** 1.4
- **Sprint name:** Error Architecture
- **Phase:** 1 — Architecture Foundation

## Objective

Create a consistent, structured application error model and retain error categories through generic domain and presentation boundaries without inventing feature recovery or user-facing behavior.

## Documentation analyzed

- `docs/ROADMAP.md` — Sprint 1.4 required categories.
- `docs/07_TECHNICAL_ARCHITECTURE.md` — structured `AppError`, presentation ownership, error-message constraints, loading model, and duplicate-input principles.
- `docs/09_OFFLINE_AND_ERROR_HANDLING.md` — required failure categories, error severity concept, raw-error prohibition, unknown-error safety, retry/recovery constraints, and pending reliability decisions.
- `docs/12_TESTING_AND_QA.md` — future error-category testing requirements.
- `docs/15_ANTIGRAVITY_RULES.md` — architecture, scope, no-invention, and dependency rules.
- `decisions/SPRINT-1.4_ERROR_ARCHITECTURE_SCOPE.md` — approved scope and deferrals.

## Repository analysis

Sprint 1.3 supplied `ScreenState.Error` without an error payload and a marker-only `DomainResult<T>`, explicitly deferring structured errors to this sprint. The project has no networking, persistence, authentication, server client, logging provider, or feature-specific use case, so no concrete failure mapping exists to reuse or modify.

## Implementation

### Core error model

- Added sealed `AppError` categories: `Network`, `Offline`, `Timeout`, `Server`, `Authentication`, `Authorization`, `Validation`, `Persistence`, `Synchronization`, `Domain`, `BattleState`, and `Unknown`.
- Error categories have no raw exception, technical message, diagnostic payload, user data, retry policy, recovery action, or user-facing copy.

### Domain and presentation propagation

- Replaced marker-only `DomainResult<T>` with sealed `Success(value)` and `Failure(error: AppError)` variants, preventing generic domain failures from disappearing.
- Changed `ScreenState.Error` to retain `AppError`; it still contains no user-facing message or recovery instruction.

### Explicitly deferred

No network/persistence/authentication implementation, exception mapper, error-to-message mapper, logging, retry, backoff, severity mapping, recovery UX, navigation, feature behavior, UI, or dependency was added.

## Files changed

### Added

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/core/error/AppError.kt` — structured category taxonomy.
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/core/error/AppErrorTest.kt` — category-distinction tests.
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/domain/result/DomainResultTest.kt` — domain failure-propagation test.
- `decisions/SPRINT-1.4_ERROR_ARCHITECTURE_SCOPE.md` — scope and pending recovery decisions.
- `audits/SPRINT_1.4_ERROR_ARCHITECTURE.md` — sprint report.

### Modified

- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/domain/result/DomainResult.kt` — represents explicit success/failure results.
- `DailyBattle/app/src/main/java/com/sanket_satpute_20/dailybattle/presentation/state/ScreenState.kt` — error state retains a structured category.
- `DailyBattle/app/src/test/java/com/sanket_satpute_20/dailybattle/presentation/state/ScreenStateTest.kt` — verifies presentation error propagation.

### Deleted

- None.

## Requirement verification

| Requirement | Status | Evidence |
| --- | --- | --- |
| Consistent error model | PASS | Sealed `AppError` provides one application-wide category vocabulary. |
| Validation, domain/battle state, network, authentication, authorization, persistence, server, timeout, unknown | PASS | Corresponding `AppError` variants exist; battle state is the documented domain-state category. |
| Offline and synchronization distinction | PASS | Required by the offline/error contract and represented independently. |
| Errors do not silently disappear | PASS | `DomainResult.Failure` and `ScreenState.Error` retain `AppError`. |
| No raw exception exposed to UI | PASS | `AppError` contains no exception/message field; no UI mapping was added. |
| No unapproved behavior | PASS | No retry/recovery/message or concrete technical mapping is implemented. |

## Tests

| Verification | Status | Evidence |
| --- | --- | --- |
| Debug build + local unit suite | PASS | `gradlew.bat --offline :app:testDebugUnitTest :app:assembleDebug --stacktrace` completed successfully. |
| Static analysis | PASS | `gradlew.bat --offline :app:lintDebug --no-configuration-cache --console=plain` completed successfully with zero errors. |
| Error-category tests | PASS | `AppErrorTest`: 3 tests, 0 failures, 0 errors. |
| Failure propagation | PASS | `DomainResultTest`: 1 test, 0 failures, 0 errors; `ScreenStateTest`: 3 tests, 0 failures, 0 errors. |
| Integration / UI / manual / accessibility | NOT APPLICABLE | No runtime source, screen, UI mapping, network, persistence, or lifecycle behavior exists in this sprint. |

## Edge cases verified

- Offline is distinct from authorization; technical failure classes are not conflated.
- Timeout is distinct from unknown; an uncertain result is not represented as a success.
- A domain failure retains its category into the result boundary.
- A presentation error retains its category without raw exception text.

## Dependencies

New dependencies: None.

## Deviations

None. The pre-existing Sprint 0.1 project-location deviation remains recorded in the Sprint 0.3 audit and is untouched.

## Pending / blocked items

The following remain pending and intentionally unimplemented: concrete exception/category mapping; user-facing copy and error-message mapping; retry eligibility, limits and backoff; authentication recovery; critical persistence failure UX; offline mutation/reconciliation behavior; logging/diagnostic provider; and all feature-specific recovery flows. They do not block this category-model sprint.

## Self-audit

1. **Only requested sprint?** Yes.
2. **Authoritative documentation followed?** Yes.
3. **Anything not required added?** No.
4. **Anything removed?** No.
5. **Unrelated functionality modified?** No.
6. **Unapproved dependency introduced?** No.
7. **Pending decision silently resolved?** No.
8. **Relevant tests pass?** Yes.
9. **Edge cases verified?** Yes, as listed above.
10. **Final Git diff inspected?** Yes — the eight changed files are limited to the taxonomy, propagation boundaries, focused tests, the scoped decision record, and this audit.

## Final status

Ready for checkpoint and push.
