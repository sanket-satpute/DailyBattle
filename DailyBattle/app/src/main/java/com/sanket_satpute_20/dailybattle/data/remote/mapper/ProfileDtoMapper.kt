package com.sanket_satpute_20.dailybattle.data.remote.mapper

import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.BattleDnaDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.PersonalRecordsDto
import com.sanket_satpute_20.dailybattle.data.remote.dto.profile.ProfileResponseDto
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile

/**
 * Maps remote transport DTOs to domain models for Profile features.
 *
 * Adheres strictly to the contract:
 * - Does not invent new calculation formulas or weighting.
 * - Does not calculate authoritative profile metrics independently in the client.
 * - Preserves domain boundaries without coupling remote schemas to local persistence.
 */
fun ProfileResponseDto.toDomain(): Profile = Profile(
    userId = UserId(userId),
    battleName = battleName,
    momentum = momentum,
    bestScore = bestScore,
    averageScore = averageScore,
    battleDNA = battleDNA.toDomain(),
    records = records.toDomain(),
)

fun BattleDnaDto.toDomain(): BattleDNA = BattleDNA(
    speed = speed,
    memory = memory,
    people = people,
)

fun PersonalRecordsDto.toDomain(): PersonalRecords = PersonalRecords(
    bestScore = bestScore,
    bestMomentum = bestMomentum,
    battlesPlayed = battlesPlayed,
)
