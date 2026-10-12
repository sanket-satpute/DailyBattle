package com.sanket_satpute_20.dailybattle.domain.authority

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite for Sprint 17.1 — Source of Truth.
 *
 * Validates:
 * - Exhaustive domain authority policies across all [DomainType] entries.
 * - Distinct classifications for Local, Remote, Cached, and Derived authorities.
 * - Per-domain storage tiers, offline accessibility, and synchronization constraints.
 * - Core architectural invariants (derived immutability, cacheability, conflict resolution).
 */
class DomainAuthorityRegistryTest {

    private lateinit var registry: DomainAuthorityRegistry

    @Before
    fun setup() {
        registry = DefaultDomainAuthorityRegistry()
    }

    @Test
    fun registryContainsAllDomainsExhaustively() {
        val allPolicies = registry.allPolicies()
        assertEquals(DomainType.values().size, allPolicies.size)

        DomainType.values().forEach { domain ->
            val policy = registry.getPolicy(domain)
            assertEquals(domain, policy.domain)
            assertTrue(policy.conflictResolutionRule.isNotBlank())
            assertTrue(policy.description.isNotBlank())
        }
    }

    @Test
    fun userSettingsAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.USER_SETTINGS)
        assertEquals(DataAuthority.Local, policy.primaryAuthority)
        assertEquals(StorageTier.DATASTORE, policy.storageTier)
        assertTrue(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertFalse(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertTrue(policy.allowsOfflineMutation)

        assertTrue(registry.isLocalAuthoritative(DomainType.USER_SETTINGS))
        assertFalse(registry.isRemoteAuthoritative(DomainType.USER_SETTINGS))
        assertFalse(registry.isDerived(DomainType.USER_SETTINGS))
        assertFalse(registry.isCached(DomainType.USER_SETTINGS))
    }

    @Test
    fun activeBattleSessionAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.ACTIVE_BATTLE_SESSION)
        assertEquals(DataAuthority.Local, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertTrue(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertFalse(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertTrue(policy.allowsOfflineMutation)

        assertTrue(registry.isLocalAuthoritative(DomainType.ACTIVE_BATTLE_SESSION))
        assertTrue(registry.requiresSynchronization(DomainType.ACTIVE_BATTLE_SESSION))
    }

    @Test
    fun officialBattleDefinitionAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.OFFICIAL_BATTLE_DEFINITION)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertTrue(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isRemoteAuthoritative(DomainType.OFFICIAL_BATTLE_DEFINITION))
        assertTrue(registry.isCached(DomainType.OFFICIAL_BATTLE_DEFINITION))
        assertFalse(registry.isLocalAuthoritative(DomainType.OFFICIAL_BATTLE_DEFINITION))
    }

    @Test
    fun officialBattleResultAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.OFFICIAL_BATTLE_RESULT)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertTrue(policy.isLocallyWritable) // initially produced on client
        assertTrue(policy.isRemoteAuthoritative) // verified and finalized by server
        assertTrue(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation) // immutable once finished

        assertTrue(registry.isRemoteAuthoritative(DomainType.OFFICIAL_BATTLE_RESULT))
        assertTrue(registry.requiresSynchronization(DomainType.OFFICIAL_BATTLE_RESULT))
        assertTrue(registry.isCached(DomainType.OFFICIAL_BATTLE_RESULT))
    }

    @Test
    fun practiceSessionAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.PRACTICE_SESSION)
        assertEquals(DataAuthority.Local, policy.primaryAuthority)
        assertEquals(StorageTier.IN_MEMORY, policy.storageTier)
        assertTrue(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertFalse(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertTrue(policy.allowsOfflineMutation)

        assertTrue(registry.isLocalAuthoritative(DomainType.PRACTICE_SESSION))
        assertFalse(registry.requiresSynchronization(DomainType.PRACTICE_SESSION))
    }

    @Test
    fun userProfileAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.USER_PROFILE)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertTrue(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isRemoteAuthoritative(DomainType.USER_PROFILE))
        assertTrue(registry.isCached(DomainType.USER_PROFILE))
    }

    @Test
    fun battleDnaAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.BATTLE_DNA)
        assertEquals(DataAuthority.Derived, policy.primaryAuthority)
        assertEquals(StorageTier.IN_MEMORY, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isDerived(DomainType.BATTLE_DNA))
        assertFalse(registry.isLocalAuthoritative(DomainType.BATTLE_DNA))
        assertFalse(registry.isRemoteAuthoritative(DomainType.BATTLE_DNA))
    }

    @Test
    fun momentumStreakAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.MOMENTUM_STREAK)
        assertEquals(DataAuthority.Derived, policy.primaryAuthority)
        assertEquals(StorageTier.IN_MEMORY, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isDerived(DomainType.MOMENTUM_STREAK))
    }

    @Test
    fun friendsAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.FRIENDS)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertTrue(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isRemoteAuthoritative(DomainType.FRIENDS))
        assertTrue(registry.isCached(DomainType.FRIENDS))
    }

    @Test
    fun rivalAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.RIVAL)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertTrue(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isRemoteAuthoritative(DomainType.RIVAL))
        assertTrue(registry.isCached(DomainType.RIVAL))
    }

    @Test
    fun battleHistoryAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.BATTLE_HISTORY)
        assertEquals(DataAuthority.Remote, policy.primaryAuthority)
        assertEquals(StorageTier.ROOM, policy.storageTier)
        assertFalse(policy.isLocallyWritable)
        assertTrue(policy.isRemoteAuthoritative)
        assertTrue(policy.isCacheable)
        assertTrue(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertFalse(policy.allowsOfflineMutation)

        assertTrue(registry.isRemoteAuthoritative(DomainType.BATTLE_HISTORY))
        assertTrue(registry.isCached(DomainType.BATTLE_HISTORY))
    }

    @Test
    fun transientUiAuthorityPolicyConformsToSpec() {
        val policy = registry.getPolicy(DomainType.TRANSIENT_UI)
        assertEquals(DataAuthority.Local, policy.primaryAuthority)
        assertEquals(StorageTier.IN_MEMORY, policy.storageTier)
        assertTrue(policy.isLocallyWritable)
        assertFalse(policy.isRemoteAuthoritative)
        assertFalse(policy.isCacheable)
        assertFalse(policy.requiresSynchronization)
        assertTrue(policy.allowsOfflineRead)
        assertTrue(policy.allowsOfflineMutation)

        assertTrue(registry.isLocalAuthoritative(DomainType.TRANSIENT_UI))
        assertFalse(registry.requiresSynchronization(DomainType.TRANSIENT_UI))
    }

    @Test
    fun architecturalInvariantsHoldAcrossAllPolicies() {
        registry.allPolicies().forEach { policy ->
            // Invariant 1: Derived data is never directly writable by the client
            if (policy.primaryAuthority == DataAuthority.Derived) {
                assertFalse("Derived domain ${policy.domain} must not be locally writable", policy.isLocallyWritable)
                assertFalse("Derived domain ${policy.domain} must not be directly synchronized", policy.requiresSynchronization)
            }

            // Invariant 2: Remote authoritative entities accessible offline must be cacheable
            if (policy.isRemoteAuthoritative && policy.allowsOfflineRead) {
                assertTrue("Remote domain ${policy.domain} readable offline must be cacheable", policy.isCacheable)
            }

            // Invariant 3: Local-only domains must not require remote synchronization
            if (policy.primaryAuthority == DataAuthority.Local && !policy.isRemoteAuthoritative && policy.domain != DomainType.ACTIVE_BATTLE_SESSION) {
                assertFalse("Local-only domain ${policy.domain} must not require synchronization", policy.requiresSynchronization)
            }
        }
    }
}
