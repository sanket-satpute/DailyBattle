package com.sanket_satpute_20.dailybattle.data.cache

/**
 * Encapsulates cached domain data with its freshness metadata.
 *
 * Conforms to 09_OFFLINE_AND_ERROR_HANDLING.md §43.
 *
 * @param data The domain model payload.
 * @param freshness The evaluated freshness status ([CacheFreshness]).
 * @param cachedAtMillis Timestamp when this entry was written to local storage.
 * @param isFromCache True if served from local cache, false if freshly fetched from remote.
 */
data class CachedResource<T>(
    val data: T,
    val freshness: CacheFreshness,
    val cachedAtMillis: Long,
    val isFromCache: Boolean,
) {
    val isFresh: Boolean get() = freshness == CacheFreshness.FRESH
    val isStale: Boolean get() = freshness == CacheFreshness.STALE
    val isExpired: Boolean get() = freshness == CacheFreshness.EXPIRED
    val isUsable: Boolean get() = isFresh || isStale
}
