package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.profile.BattleDNA
import com.sanket_satpute_20.dailybattle.domain.profile.PersonalRecords
import com.sanket_satpute_20.dailybattle.domain.profile.Profile

/**
 * Bidirectional mapper between [ProfileEntity] and [Profile].
 */
fun ProfileEntity.toDomain(): Profile = Profile(
    userId = UserId(userId),
    battleName = battleName,
    momentum = maxOf(0, momentum),
    bestScore = maxOf(0, bestScore),
    averageScore = maxOf(0, averageScore),
    battleDNA = BattleDNA(
        speed = speedDna?.coerceAtLeast(0),
        memory = memoryDna?.coerceAtLeast(0),
        people = peopleDna?.coerceAtLeast(0),
    ),
    records = PersonalRecords(
        bestScore = maxOf(0, bestScore),
        bestMomentum = maxOf(0, bestMomentumRecord),
        battlesPlayed = maxOf(0, battlesPlayedRecord),
    ),
)

fun Profile.toEntity(updatedAt: Long = 0L): ProfileEntity = ProfileEntity(
    userId = userId.value,
    battleName = battleName,
    momentum = momentum,
    bestScore = bestScore,
    averageScore = averageScore,
    speedDna = battleDNA.speed,
    memoryDna = battleDNA.memory,
    peopleDna = battleDNA.people,
    bestMomentumRecord = records.bestMomentum,
    battlesPlayedRecord = records.battlesPlayed,
    updatedAt = updatedAt,
)
