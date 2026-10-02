# TECH-PENDING-002 through TECH-PENDING-004 — SDK, toolchain, and project location

**Status:** IMPLEMENTED

**Approved by:** Product Owner, 2026-10-02

**Scope:** Sprint 2.1 — Android Project

## Approved project contracts

| ID | Decision | Approved value |
| --- | --- | --- |
| TECH-PENDING-002 | Minimum Android SDK | 30 |
| TECH-PENDING-003 | Compile Android SDK | 37 |
| TECH-PENDING-003 | Target Android SDK | 37 |
| TECH-PENDING-004 | Kotlin | 2.2.10 |
| TECH-PENDING-004 | Android Gradle Plugin | 9.3.3 |
| TECH-PENDING-004 | Gradle wrapper | 9.5.0 |

These values are locked for this project and must not change without a subsequent documented
compatibility review and explicit product-owner decision.

## Project structure acceptance

`DailyBattle/` is the approved location of the Android Gradle project. It contains the root Gradle
configuration and the single `:app` application module. No relocation or rename to `android/` is
authorized or required.

This resolves `DEV-STRUCTURE-001`; the earlier roadmap's `android/` directory example does not
apply to this approved implementation structure.

## Constraints

- This record resolves only TECH-PENDING-002, TECH-PENDING-003, TECH-PENDING-004, and
  DEV-STRUCTURE-001.
- It does not resolve other pending technical, data, authentication, backend, persistence, or
  product decisions.
- Existing application identity and Compose/Hilt decisions remain governed by their respective
  decision records.
