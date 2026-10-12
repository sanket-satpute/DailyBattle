package com.sanket_satpute_20.dailybattle.domain.authority

/**
 * Authoritative registry defining the source of truth, caching, and synchronization
 * policies for all domains in Daily Battle.
 *
 * Implements Sprint 17.1 — Source of Truth.
 */
interface DomainAuthorityRegistry {

    /** Retrieves the complete [DomainAuthorityPolicy] for the specified [domain]. */
    fun getPolicy(domain: DomainType): DomainAuthorityPolicy

    /** Retrieves the primary [DataAuthority] for the specified [domain]. */
    fun getAuthority(domain: DomainType): DataAuthority

    /** Returns true if the client device is the authoritative owner of the [domain]. */
    fun isLocalAuthoritative(domain: DomainType): Boolean

    /** Returns true if the remote backend is the authoritative owner of the [domain]. */
    fun isRemoteAuthoritative(domain: DomainType): Boolean

    /** Returns true if the [domain] represents a locally cached replica of remote state. */
    fun isCached(domain: DomainType): Boolean

    /** Returns true if the [domain] is deterministically derived from other records. */
    fun isDerived(domain: DomainType): Boolean

    /** Returns true if mutations or completions in the [domain] require network synchronization. */
    fun requiresSynchronization(domain: DomainType): Boolean

    /** Returns true if the [domain] can be read offline without network connectivity. */
    fun allowsOfflineRead(domain: DomainType): Boolean

    /** Returns true if mutations can occur while offline. */
    fun allowsOfflineMutation(domain: DomainType): Boolean

    /** Returns all registered domain authority policies. */
    fun allPolicies(): List<DomainAuthorityPolicy>
}
