package com.sanket_satpute_20.dailybattle.data.cache

import com.sanket_satpute_20.dailybattle.domain.authority.DomainType

/**
 * Cache policy specifying freshness (TTL) and maximum staleness duration for a domain.
 *
 * Conforms to 08_DATA_AND_API.md §62 and 09_OFFLINE_AND_ERROR_HANDLING.md §43.
 */
data class CachePolicy(
    val domain: DomainType,
    val ttlMillis: Long,
    val maxStaleMillis: Long,
) {
    init {
        require(ttlMillis >= 0) { "ttlMillis must be non-negative: $ttlMillis" }
        require(maxStaleMillis >= ttlMillis) { "maxStaleMillis ($maxStaleMillis) must be >= ttlMillis ($ttlMillis)" }
    }

    /**
     * Evaluates cache freshness against the provided [cachedAtMillis] and [currentTimeMillis].
     */
    fun evaluateFreshness(cachedAtMillis: Long, currentTimeMillis: Long): CacheFreshness {
        if (cachedAtMillis <= 0L) return CacheFreshness.MISSING
        val age = currentTimeMillis - cachedAtMillis
        return when {
            age < 0 -> CacheFreshness.FRESH // Clock skew tolerance
            age <= ttlMillis -> CacheFreshness.FRESH
            age <= maxStaleMillis -> CacheFreshness.STALE
            else -> CacheFreshness.EXPIRED
        }
    }

    companion object {
        const val FIVE_MINUTES_MS = 5 * 60 * 1000L
        const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
        const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L
        const val SEVEN_DAYS_MS = 7 * 24 * 60 * 60 * 1000L
        const val THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000L

        val PROFILE = CachePolicy(
            domain = DomainType.USER_PROFILE,
            ttlMillis = FIVE_MINUTES_MS,
            maxStaleMillis = SEVEN_DAYS_MS,
        )

        val FRIENDS = CachePolicy(
            domain = DomainType.FRIENDS,
            ttlMillis = FIVE_MINUTES_MS,
            maxStaleMillis = SEVEN_DAYS_MS,
        )

        val RIVAL = CachePolicy(
            domain = DomainType.RIVAL,
            ttlMillis = FIVE_MINUTES_MS,
            maxStaleMillis = TWENTY_FOUR_HOURS_MS,
        )

        val BATTLE_DEFINITION = CachePolicy(
            domain = DomainType.OFFICIAL_BATTLE_DEFINITION,
            ttlMillis = TWENTY_FOUR_HOURS_MS,
            maxStaleMillis = TWENTY_FOUR_HOURS_MS * 2,
        )

        val BATTLE_HISTORY = CachePolicy(
            domain = DomainType.BATTLE_HISTORY,
            ttlMillis = FIFTEEN_MINUTES_MS,
            maxStaleMillis = THIRTY_DAYS_MS,
        )

        val BATTLE_RESULT = CachePolicy(
            domain = DomainType.OFFICIAL_BATTLE_RESULT,
            ttlMillis = Long.MAX_VALUE,
            maxStaleMillis = Long.MAX_VALUE,
        )
    }
}
