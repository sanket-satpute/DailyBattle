# Sprint 2.2 — App startup scope

**Status:** APPROVED IMPLEMENTATION SCOPE
**Approved source:** `docs/ROADMAP.md` (Phase 2, Sprint 2.2) and `docs/07_TECHNICAL_ARCHITECTURE.md`
(sections 30, 66 — navigation and app shell belong to the later Phase 4 sprint).

Sprint 2.2 implements the generic startup sequence only: Hilt dependency construction reaching the
presentation layer for the first time, a navigation host with exactly one startup destination, and
an explicit initial `ScreenState` owned by a startup view model. The startup state resolves directly
to `Success(Unit)` because no asynchronous dependency (network, persistence, configuration) exists
yet to await; introducing an artificial delay or fabricated loading period would not reflect real
work.

The following remain pending and are not implemented: primary navigation destinations and bottom
navigation (`Home`/`Battle`/`Friends`/`Me`, Phase 4 app shell), environment/configuration separation
(Sprint 2.3), fixture-based/fake-repository test infrastructure (Sprint 2.4), and any error-message
copy or mapping (deferred since Sprint 1.4).
