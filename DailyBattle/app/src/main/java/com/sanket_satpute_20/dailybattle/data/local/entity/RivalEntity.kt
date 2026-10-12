package com.sanket_satpute_20.dailybattle.data.local.entity

/**
 * Local persistence entity representing a selected rival competition relationship.
 *
 * Implements the local persistence representation for the Rival domain
 * per `08_DATA_AND_API.md` §36.
 *
 * Fields:
 * - [userId]      — primary user ID
 * - [rivalUserId] — selected rival's user ID
 * - [rivalName]   — display name of the rival
 * - [selectedAt]  — selection timestamp (epoch ms)
 * - [status]      — serialized name of [com.sanket_satpute_20.dailybattle.domain.rival.RivalStatus]
 */
data class RivalEntity(
    val userId: String,
    val rivalUserId: String,
    val rivalName: String,
    val selectedAt: Long,
    val status: String,
)
