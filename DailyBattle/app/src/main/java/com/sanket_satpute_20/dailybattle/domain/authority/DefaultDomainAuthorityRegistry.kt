package com.sanket_satpute_20.dailybattle.domain.authority

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [DomainAuthorityRegistry] providing the authoritative
 * Source of Truth definitions across all Daily Battle domains.
 *
 * Implements Sprint 17.1 specification per 08_DATA_AND_API.md §60 and 07_TECHNICAL_ARCHITECTURE.md §12.
 */
@Singleton
class DefaultDomainAuthorityRegistry @Inject constructor() : DomainAuthorityRegistry {

    private val policies: Map<DomainType, DomainAuthorityPolicy> = mapOf(
        DomainType.USER_SETTINGS to DomainAuthorityPolicy(
            domain = DomainType.USER_SETTINGS,
            primaryAuthority = DataAuthority.Local,
            storageTier = StorageTier.DATASTORE,
            isLocallyWritable = true,
            isRemoteAuthoritative = false,
            isCacheable = false,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = true,
            conflictResolutionRule = "Local client is the sole authority; changes are immediately applied locally.",
            description = "User settings and device preferences (audio, haptics, theme).",
        ),
        DomainType.ACTIVE_BATTLE_SESSION to DomainAuthorityPolicy(
            domain = DomainType.ACTIVE_BATTLE_SESSION,
            primaryAuthority = DataAuthority.Local,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = true,
            isRemoteAuthoritative = false,
            isCacheable = false,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = true,
            conflictResolutionRule = "Active in-flight session is owned locally during play; cannot be altered remotely.",
            description = "In-progress battle session, challenge execution, and real-time state machine.",
        ),
        DomainType.OFFICIAL_BATTLE_DEFINITION to DomainAuthorityPolicy(
            domain = DomainType.OFFICIAL_BATTLE_DEFINITION,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = false,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is authoritative for battle puzzle definition and availability window; client cannot fabricate official battles.",
            description = "Official daily challenge composition, puzzle parameters, and availability window.",
        ),
        DomainType.OFFICIAL_BATTLE_RESULT to DomainAuthorityPolicy(
            domain = DomainType.OFFICIAL_BATTLE_RESULT,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = true,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is the sole authority for competitive score integrity; local record is cached/pending until server confirmation.",
            description = "Official completed battle result and challenge score breakdown.",
        ),
        DomainType.PRACTICE_SESSION to DomainAuthorityPolicy(
            domain = DomainType.PRACTICE_SESSION,
            primaryAuthority = DataAuthority.Local,
            storageTier = StorageTier.IN_MEMORY,
            isLocallyWritable = true,
            isRemoteAuthoritative = false,
            isCacheable = false,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = true,
            conflictResolutionRule = "Local-only; practice plays are never submitted to remote leaderboards or competitive records.",
            description = "Casual non-competitive practice gameplay and temporary practice scores.",
        ),
        DomainType.USER_PROFILE to DomainAuthorityPolicy(
            domain = DomainType.USER_PROFILE,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = false,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is authoritative for cross-device user profile and career records; cached locally for instant display.",
            description = "User career statistics, all-time best records, and player metadata.",
        ),
        DomainType.BATTLE_DNA to DomainAuthorityPolicy(
            domain = DomainType.BATTLE_DNA,
            primaryAuthority = DataAuthority.Derived,
            storageTier = StorageTier.IN_MEMORY,
            isLocallyWritable = false,
            isRemoteAuthoritative = false,
            isCacheable = true,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Deterministically derived from historical challenge performance; cannot be independently edited.",
            description = "Player cognitive fingerprint across speed, memory, people, and endurance dimensions.",
        ),
        DomainType.MOMENTUM_STREAK to DomainAuthorityPolicy(
            domain = DomainType.MOMENTUM_STREAK,
            primaryAuthority = DataAuthority.Derived,
            storageTier = StorageTier.IN_MEMORY,
            isLocallyWritable = false,
            isRemoteAuthoritative = false,
            isCacheable = true,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Deterministically derived from consecutive verified daily battle completions; cannot be directly manipulated.",
            description = "Consecutive daily battle streak and momentum multiplier.",
        ),
        DomainType.FRIENDS to DomainAuthorityPolicy(
            domain = DomainType.FRIENDS,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = false,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is authoritative for friend list, relationship state, and friend daily scores; cached locally for offline viewing.",
            description = "Friend list, friendship connections, and friend daily battle scores.",
        ),
        DomainType.RIVAL to DomainAuthorityPolicy(
            domain = DomainType.RIVAL,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = false,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is authoritative for daily rival assignment; score gap/direction is deterministically derived from verified scores.",
            description = "Daily server-assigned rival and comparative match performance.",
        ),
        DomainType.BATTLE_HISTORY to DomainAuthorityPolicy(
            domain = DomainType.BATTLE_HISTORY,
            primaryAuthority = DataAuthority.Remote,
            storageTier = StorageTier.ROOM,
            isLocallyWritable = false,
            isRemoteAuthoritative = true,
            isCacheable = true,
            requiresSynchronization = true,
            allowsOfflineRead = true,
            allowsOfflineMutation = false,
            conflictResolutionRule = "Server is authoritative repository for historical battles; cached locally in Room for offline archive browsing.",
            description = "Historical log of past completed battles and scores.",
        ),
        DomainType.TRANSIENT_UI to DomainAuthorityPolicy(
            domain = DomainType.TRANSIENT_UI,
            primaryAuthority = DataAuthority.Local,
            storageTier = StorageTier.IN_MEMORY,
            isLocallyWritable = true,
            isRemoteAuthoritative = false,
            isCacheable = false,
            requiresSynchronization = false,
            allowsOfflineRead = true,
            allowsOfflineMutation = true,
            conflictResolutionRule = "Presentation-layer ephemeral state; discarded on process death or screen exit without persistence.",
            description = "Transient UI screen state, animations, dialog buffers, and form inputs.",
        ),
    )

    init {
        // Enforce full exhaustive domain coverage
        DomainType.values().forEach { domain ->
            require(policies.containsKey(domain)) {
                "Missing domain authority policy for domain: $domain"
            }
        }
    }

    override fun getPolicy(domain: DomainType): DomainAuthorityPolicy =
        policies[domain] ?: error("No authority policy configured for domain: $domain")

    override fun getAuthority(domain: DomainType): DataAuthority =
        getPolicy(domain).primaryAuthority

    override fun isLocalAuthoritative(domain: DomainType): Boolean =
        getAuthority(domain) == DataAuthority.Local

    override fun isRemoteAuthoritative(domain: DomainType): Boolean =
        getPolicy(domain).isRemoteAuthoritative

    override fun isCached(domain: DomainType): Boolean =
        getPolicy(domain).isCacheable

    override fun isDerived(domain: DomainType): Boolean =
        getAuthority(domain) == DataAuthority.Derived

    override fun requiresSynchronization(domain: DomainType): Boolean =
        getPolicy(domain).requiresSynchronization

    override fun allowsOfflineRead(domain: DomainType): Boolean =
        getPolicy(domain).allowsOfflineRead

    override fun allowsOfflineMutation(domain: DomainType): Boolean =
        getPolicy(domain).allowsOfflineMutation

    override fun allPolicies(): List<DomainAuthorityPolicy> =
        policies.values.toList()
}
