package com.sanket_satpute_20.dailybattle.data.conflict

/**
 * Categories of synchronization and data conflicts encountered across local storage,
 * remote endpoints, and pending mutation queues.
 *
 * Conforms to:
 * - 08_DATA_AND_API.md §74 (Concurrency & Authoritative Conflict Resolution)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §47, §48, §50 (Queue and mutation conflict safety)
 * - 15_ANTIGRAVITY_RULES.md Rule 42 (No duplicate requests, sessions, submissions, friend actions)
 */
enum class ConflictType {
    /**
     * Stale local record or cached resource compared to newer remote state.
     */
    STALE_RECORD,

    /**
     * Incoming remote data has an older timestamp or version than local authoritative state
     * (e.g. out-of-order replication or stale server replica).
     */
    SERVER_STALE,

    /**
     * An identical mutation is already pending or was already acknowledged.
     */
    DUPLICATE_MUTATION,

    /**
     * Local and remote state conflict (e.g. local session in-progress, remote session already completed).
     */
    CONFLICTING_UPDATE,

    /**
     * Remote authoritative state has advanced beyond local state.
     */
    REMOTE_AHEAD,
}
