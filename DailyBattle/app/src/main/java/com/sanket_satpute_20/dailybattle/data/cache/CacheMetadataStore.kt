package com.sanket_satpute_20.dailybattle.data.cache

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks domain cache update timestamps.
 */
interface CacheMetadataStore {
    suspend fun getLastUpdated(key: String): Long?
    suspend fun setLastUpdated(key: String, timestamp: Long)
    suspend fun clear(key: String)
    suspend fun clearAll()
}

/**
 * Thread-safe in-memory implementation of [CacheMetadataStore].
 */
@Singleton
class InMemoryCacheMetadataStore @Inject constructor() : CacheMetadataStore {
    private val timestamps = ConcurrentHashMap<String, Long>()

    override suspend fun getLastUpdated(key: String): Long? = timestamps[key]

    override suspend fun setLastUpdated(key: String, timestamp: Long) {
        timestamps[key] = timestamp
    }

    override suspend fun clear(key: String) {
        timestamps.remove(key)
    }

    override suspend fun clearAll() {
        timestamps.clear()
    }
}
