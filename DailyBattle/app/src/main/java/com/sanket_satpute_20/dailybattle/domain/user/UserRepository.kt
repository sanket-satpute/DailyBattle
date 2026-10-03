package com.sanket_satpute_20.dailybattle.domain.user

/**
 * Contract for managing user profile persistence.
 *
 * Auth and complete User model synchronization is pending (Sprint 6.2).
 * Currently supports local persistence of the user-chosen battle name.
 */
interface UserRepository {
    /**
     * Persists the user's selected battle name locally.
     */
    suspend fun saveBattleName(name: String)

    /**
     * Retrieves the saved battle name, if any.
     */
    suspend fun getBattleName(): String?
}
