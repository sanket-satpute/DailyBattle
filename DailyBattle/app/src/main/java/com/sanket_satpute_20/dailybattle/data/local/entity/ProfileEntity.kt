package com.sanket_satpute_20.dailybattle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local persistence entity representing a cached user profile.
 *
 * Implements the local persistence representation for the Profile domain
 * per `08_DATA_AND_API.md` §38, §39, §60, §62.
 *
 * Fields:
 * - [userId]               — primary key / user ID
 * - [battleName]           — user's in-product identity name
 * - [momentum]             — consecutive calendar days momentum
 * - [bestScore]            — highest official total score achieved
 * - [averageScore]         — average official score achieved
 * - [speedDna]             — historical average Snap score (nullable)
 * - [memoryDna]            — historical average Shift score (nullable)
 * - [peopleDna]            — historical average Crowd Call score (nullable)
 * - [bestMomentumRecord]   — personal best momentum streak
 * - [battlesPlayedRecord]  — total official battles played count
 * - [updatedAt]            — cache update timestamp (epoch ms)
 */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val userId: String,
    val battleName: String,
    val momentum: Int,
    val bestScore: Int,
    val averageScore: Int,
    val speedDna: Int?,
    val memoryDna: Int?,
    val peopleDna: Int?,
    val bestMomentumRecord: Int,
    val battlesPlayedRecord: Int,
    val updatedAt: Long,
)
