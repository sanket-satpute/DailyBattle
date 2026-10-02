package com.sanket_satpute_20.dailybattle.domain.user

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Domain representation of the application user.
 *
 * Per `08_DATA_AND_API.md` §6:
 * - userId:        opaque identifier, authority = remote/auth system
 * - battleName:    user-chosen in-product identity (1–20 chars, validated by [InputValidator])
 * - createdAt:     timestamp, authority = backend
 * - updatedAt:     timestamp, authority = backend
 * - accountStatus: lifecycle state, authority = backend (enum values pending)
 *
 * The complete authentication model is PENDING (`01_PRODUCT.md` §56.1).
 * This model represents the *domain identity*, not the auth session.
 */
data class User(
    val userId: UserId,
    val battleName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val accountStatus: AccountStatus,
)
