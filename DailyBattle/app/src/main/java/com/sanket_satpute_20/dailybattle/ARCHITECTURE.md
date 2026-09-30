# Application logical boundaries

Sprint 1.1 establishes these single-module package boundaries beneath
`com.sanket_satpute_20.dailybattle`:

- `core` — platform-neutral common utilities and cross-cutting primitives.
- `design` — Compose theme and reusable design-system components.
- `navigation` — routes and navigation coordination only.
- `presentation` — screen state, user events, and transient UI effects.
- `domain` — use cases and business rules; depends on repository contracts, never Android UI or storage APIs.
- `data` — repository implementations, mappers, and local/remote data sources.
- `testing` — reusable test fixtures and helpers.

The layers are logical, not separate Gradle modules. UI code communicates with
presentation; presentation invokes domain; domain depends on repository
contracts; data implements those contracts. No domain, repository, or data
implementation is introduced by Sprint 1.1.
