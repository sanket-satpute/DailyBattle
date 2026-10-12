package com.sanket_satpute_20.dailybattle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local persistence entity representing an official daily Battle definition.
 *
 * Implements the local persistence representation for the Battle domain
 * per `08_DATA_AND_API.md` §7, §8.
 *
 * Fields:
 * - [battleId]   — primary key / opaque unique battle identifier
 * - [battleDate] — official battle date (epoch ms or epoch days UTC)
 * - [status]     — serialized name of [com.sanket_satpute_20.dailybattle.domain.battle.BattleStatus]
 * - [version]    — configuration version
 */
@Entity(tableName = "battles")
data class BattleEntity(
    @PrimaryKey
    val battleId: String,
    val battleDate: Long,
    val status: String,
    val version: Int,
)
