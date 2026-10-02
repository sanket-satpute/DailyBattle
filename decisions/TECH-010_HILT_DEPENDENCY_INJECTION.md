# TECH-PENDING-010 — Hilt dependency injection

**Status:** IMPLEMENTED

**Approved by:** Product Owner, 2026-10-02

## Decision

Daily Battle uses Hilt as its dependency injection solution.

## Sprint 1.5 scope

Sprint 1.5 establishes the Hilt application component through `DailyBattleApplication` and makes
the existing `MainActivity` an Android entry point. Hilt's generated application and activity
components are used; no hand-built component, service locator, global object graph, repository
binding, service binding, configuration binding, or fake implementation is added.

## Constraints

- Dependency direction remains UI → presentation → domain → repository contracts → data.
- No Android UI dependency is introduced into domain code.
- No UI code constructs a network client, database, service, or repository.
- Repository, service, environment, backend, persistence, and authentication decisions remain
  pending until their respective contracts exist and are approved.
- Future production bindings must use Hilt modules only when a concrete approved contract requires
  a binding; their test fakes or mocks must replace those bindings in test source sets.

## Dependency record

| Dependency | Version | Purpose | Approval |
| --- | --- | --- | --- |
| `com.google.dagger.hilt.android` Gradle plugin | 2.60.1 | Generate Hilt Android integration. | Explicit user approval for TECH-PENDING-010 |
| `com.google.dagger:hilt-android` | 2.60.1 | Hilt runtime annotations and generated component support. | Explicit user approval for TECH-PENDING-010 |
| Kotlin Symbol Processing Gradle plugin | 2.3.12 | Required compiler integration because AGP built-in Kotlin rejects KAPT. | Technically necessary for the approved Hilt setup |
| `com.google.dagger:hilt-compiler` | 2.60.1 | Generate Hilt dependency code through KSP. | Explicit user approval for TECH-PENDING-010 |

Hilt and KSP add no runtime permissions, network behavior, user-data collection, secret, or
service-account credential. Both are Apache-2.0 licensed; their build impact is source generation
during compilation. The existing platform cannot provide generated Android lifecycle-aware dependency
injection, so Hilt is used instead of a manual composition mechanism as explicitly approved.
