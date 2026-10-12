package com.sanket_satpute_20.dailybattle.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {

    private val policy = CachePolicy(
        domain = com.sanket_satpute_20.dailybattle.domain.authority.DomainType.USER_PROFILE,
        ttlMillis = 60_000L, // 1 minute
        maxStaleMillis = 300_000L, // 5 minutes
    )

    @Test
    fun evaluateFreshnessReturnsMissingWhenTimestampIsZeroOrNegative() {
        assertEquals(CacheFreshness.MISSING, policy.evaluateFreshness(0L, 100_000L))
        assertEquals(CacheFreshness.MISSING, policy.evaluateFreshness(-50L, 100_000L))
    }

    @Test
    fun evaluateFreshnessReturnsFreshWhenWithinTtl() {
        val cachedAt = 100_000L
        val now = 130_000L // 30s elapsed (< 60s)
        assertEquals(CacheFreshness.FRESH, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun evaluateFreshnessReturnsFreshAtExactTtlBoundary() {
        val cachedAt = 100_000L
        val now = 160_000L // 60s elapsed (== TTL)
        assertEquals(CacheFreshness.FRESH, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun evaluateFreshnessReturnsStalePastTtlButWithinMaxStale() {
        val cachedAt = 100_000L
        val now = 200_000L // 100s elapsed (> 60s, < 300s)
        assertEquals(CacheFreshness.STALE, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun evaluateFreshnessReturnsStaleAtExactMaxStaleBoundary() {
        val cachedAt = 100_000L
        val now = 400_000L // 300s elapsed (== maxStale)
        assertEquals(CacheFreshness.STALE, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun evaluateFreshnessReturnsExpiredWhenBeyondMaxStale() {
        val cachedAt = 100_000L
        val now = 400_001L // 300s + 1ms elapsed
        assertEquals(CacheFreshness.EXPIRED, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun evaluateFreshnessHandlesClockSkewGracefully() {
        val cachedAt = 200_000L
        val now = 100_000L // Clock skew: now < cachedAt
        assertEquals(CacheFreshness.FRESH, policy.evaluateFreshness(cachedAt, now))
    }

    @Test
    fun defaultDomainPoliciesHaveCorrectTtlAndMaxStaleValues() {
        assertEquals(CachePolicy.FIVE_MINUTES_MS, CachePolicy.PROFILE.ttlMillis)
        assertEquals(CachePolicy.SEVEN_DAYS_MS, CachePolicy.PROFILE.maxStaleMillis)

        assertEquals(CachePolicy.FIVE_MINUTES_MS, CachePolicy.FRIENDS.ttlMillis)
        assertEquals(CachePolicy.SEVEN_DAYS_MS, CachePolicy.FRIENDS.maxStaleMillis)

        assertEquals(CachePolicy.FIVE_MINUTES_MS, CachePolicy.RIVAL.ttlMillis)
        assertEquals(CachePolicy.TWENTY_FOUR_HOURS_MS, CachePolicy.RIVAL.maxStaleMillis)

        assertEquals(CachePolicy.TWENTY_FOUR_HOURS_MS, CachePolicy.BATTLE_DEFINITION.ttlMillis)
        assertEquals(CachePolicy.TWENTY_FOUR_HOURS_MS * 2, CachePolicy.BATTLE_DEFINITION.maxStaleMillis)

        assertEquals(CachePolicy.FIFTEEN_MINUTES_MS, CachePolicy.BATTLE_HISTORY.ttlMillis)
        assertEquals(CachePolicy.THIRTY_DAYS_MS, CachePolicy.BATTLE_HISTORY.maxStaleMillis)
    }

    @Test
    fun cachedResourceHelperPropertiesEvaluateCorrectly() {
        val freshResource = CachedResource("data", CacheFreshness.FRESH, 100L, isFromCache = true)
        assertTrue(freshResource.isFresh)
        assertTrue(freshResource.isUsable)

        val staleResource = CachedResource("data", CacheFreshness.STALE, 100L, isFromCache = true)
        assertFalse(staleResource.isFresh)
        assertTrue(staleResource.isUsable)

        val expiredResource = CachedResource("data", CacheFreshness.EXPIRED, 100L, isFromCache = true)
        assertFalse(expiredResource.isFresh)
        assertFalse(expiredResource.isUsable)

        val missingResource = CachedResource("data", CacheFreshness.MISSING, 0L, isFromCache = false)
        assertFalse(missingResource.isFresh)
        assertFalse(missingResource.isUsable)
    }
}
