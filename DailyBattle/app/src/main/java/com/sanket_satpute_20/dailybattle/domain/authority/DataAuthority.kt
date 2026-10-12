package com.sanket_satpute_20.dailybattle.domain.authority

/**
 * Defines the authoritative source of truth for application state as required by
 * Phase 17 — Synchronization + Offline (Sprint 17.1).
 *
 * Distinguishes between:
 * - [Local]: State owned by the local device client.
 * - [Remote]: State owned and validated by the backend server.
 * - [Cached]: Local persistent replica of remote-authoritative state for offline resilience.
 * - [Derived]: State deterministically calculated from underlying authoritative records.
 *
 * Source: 08_DATA_AND_API.md §60, 07_TECHNICAL_ARCHITECTURE.md §12.
 */
sealed interface DataAuthority {
    val name: String
    val description: String

    /**
     * Authoritative source is the local client device.
     * State updates originate locally and do not require remote server authorization.
     */
    data object Local : DataAuthority {
        override val name: String = "LOCAL"
        override val description: String = "Authoritative on the client device"
    }

    /**
     * Authoritative source is the remote server.
     * The backend is the sole arbiter of competitive score validity, puzzle definitions, and social relationships.
     */
    data object Remote : DataAuthority {
        override val name: String = "REMOTE"
        override val description: String = "Authoritative on the remote server"
    }

    /**
     * Local persistent replica of remote-authoritative data.
     * Stored locally in Room or cache storage to enable offline viewing and instant startup,
     * but subject to staleness, expiration, and server refresh.
     */
    data object Cached : DataAuthority {
        override val name: String = "CACHED"
        override val description: String = "Locally cached replica of remote-authoritative data"
    }

    /**
     * Deterministically computed from other authoritative or cached data.
     * Must never be treated as an independent source of truth that conflicts with its underlying inputs.
     */
    data object Derived : DataAuthority {
        override val name: String = "DERIVED"
        override val description: String = "Deterministically computed from underlying authoritative data"
    }
}
