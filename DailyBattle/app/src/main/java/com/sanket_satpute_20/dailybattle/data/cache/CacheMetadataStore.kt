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
    suspend fun pruneExpired(maxAgeMs: Long, currentTimeMs: Long)
    val entryCount: Int get() = 0
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

    override suspend fun pruneExpired(maxAgeMs: Long, currentTimeMs: Long) {
        val iterator = timestamps.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (currentTimeMs - entry.value > maxAgeMs) {
                iterator.remove()
            }
        }
    }

    override val entryCount: Int get() = timestamps.size
}
