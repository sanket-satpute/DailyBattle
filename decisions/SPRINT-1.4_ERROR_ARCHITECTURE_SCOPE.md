# Sprint 1.4 — Error architecture scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md`, `07_TECHNICAL_ARCHITECTURE.md`, and `09_OFFLINE_AND_ERROR_HANDLING.md`

Sprint 1.4 establishes a data-free, structured `AppError` category taxonomy and propagation
through generic domain and presentation results.

The following remain pending and are not implemented: user-facing message copy, error-to-message
mapping, error severity, retry eligibility, automatic retry count/backoff, authentication recovery,
critical persistence recovery UX, logging provider/diagnostic payloads, network implementation, and
all feature-specific recovery behavior.
