package com.sanket_satpute_20.dailybattle.data.cache

/**
 * Freshness status of cached local data per Phase 17 — Synchronization + Offline (Sprint 17.2).
 *
 * Conforms to 09_OFFLINE_AND_ERROR_HANDLING.md §43:
 * - [FRESH]: Data is current within its time-to-live (TTL) window.
 * - [STALE]: Data has exceeded its TTL, but may be presented as "known but possibly stale"
 *   when offline or pending background revalidation.
 * - [EXPIRED]: Data has exceeded maximum stale retention or belongs to an expired daily cycle.
 * - [MISSING]: No cached record exists in local storage.
 */
enum class CacheFreshness {
    FRESH,
    STALE,
    EXPIRED,
    MISSING,
}
