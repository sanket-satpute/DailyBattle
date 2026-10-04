package com.sanket_satpute_20.dailybattle.domain.friend

/**
 * Represents the current conceptual state of a friendship.
 *
 * Defined in `08_DATA_AND_API.md` section 34:
 * - PENDING
 * - ACCEPTED
 * - BLOCKED
 */
enum class FriendStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}
