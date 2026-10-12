package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing the application user.
 *
 * Implements the local persistence representation for the User domain
 * per `08_DATA_AND_API.md` §6.
 *
 * Fields:
 * - [userId]        — primary key / opaque identifier
 * - [battleName]    — user-chosen in-product identity (1–20 characters)
 * - [createdAt]     — creation timestamp (epoch ms)
 * - [updatedAt]     — last update timestamp (epoch ms)
 * - [accountStatus] — serialized name of [com.sanket_satpute_20.dailybattle.domain.user.AccountStatus]
 */
data class UserEntity(
    val userId: String,
    val battleName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val accountStatus: String,
)
