package com.sanket_satpute_20.dailybattle.domain.authority

/**
 * Architectural policy defining the authoritative ownership, persistence,
 * offline access, and synchronization behavior for a specific domain.
 *
 * Source: 08_DATA_AND_API.md §60, 07_TECHNICAL_ARCHITECTURE.md §12.
 */
data class DomainAuthorityPolicy(
    val domain: DomainType,
    val primaryAuthority: DataAuthority,
    val storageTier: StorageTier,
    val isLocallyWritable: Boolean,
    val isRemoteAuthoritative: Boolean,
    val isCacheable: Boolean,
    val requiresSynchronization: Boolean,
    val allowsOfflineRead: Boolean,
    val allowsOfflineMutation: Boolean,
    val conflictResolutionRule: String,
    val description: String,
)
