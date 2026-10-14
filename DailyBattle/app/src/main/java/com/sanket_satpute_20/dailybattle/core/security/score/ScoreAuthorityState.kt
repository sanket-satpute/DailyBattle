package com.sanket_satpute_20.dailybattle.core.security.score

/**
 * Authority state for competitive battle scores.
 *
 * Implements Sprint 24.6 Score Authority verification per:
 * - `08_DATA_AND_API.md` §27 (Score Integrity) & §67 (Anti-Cheat Boundary)
 * - `ROADMAP.md` Sprint 24.6 ("Never trust the Android client as the sole authority for competitive score integrity.")
 */
enum class ScoreAuthorityState {
    /**
     * Client-generated candidate score.
     * Temporary representation during local gameplay before authoritative server verification.
     * Cannot be presented as official competitive rank until server confirmation.
     */
    LOCAL_CANDIDATE,

    /**
     * Authoritative server-confirmed score.
     * Sole authority for competitive integrity, percentiles, leaderboards, and user career history.
     * Strictly immutable on the client device after confirmation.
     */
    SERVER_CONFIRMED,
}
