# Sprint 2.3 — Environment configuration scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 2, Sprint 2.3), `docs/07_TECHNICAL_ARCHITECTURE.md`
section 34 (Configuration Management) and section 60 (Environment Structure), and
`docs/15_ANTIGRAVITY_RULES.md` section 72 (Environment Rule).

Sprint 2.3 establishes only the generic environment-identity boundary: an `AppEnvironment` value
(`Development`/`Testing`/`Production`) and an `EnvironmentProvider` resolved from the compiled build
type (`BuildConfig.DEBUG`). No API base URL, secret, credential, feature flag, or backend endpoint is
introduced, because concrete per-environment configuration is pending backend/network decisions
(`TECH-PENDING-008`, `TECH-PENDING-009`) and the exact environment architecture is explicitly
"PENDING" per `07_TECHNICAL_ARCHITECTURE.md` section 60 and `12_TESTING_AND_QA.md` section 62.

`AppEnvironment.Testing` is not reachable from `BuildTypeEnvironmentProvider` because no dedicated
test build variant/flavor exists. Introducing one would resolve the still-pending build-variant
decision (`07_TECHNICAL_ARCHITECTURE.md` section 59) without approval. Test code may construct
`AppEnvironment.Testing` directly once fixture infrastructure exists (Sprint 2.4).
