package com.sanket_satpute_20.dailybattle.domain.profile

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

data class Profile(
    val userId: UserId,
    val battleName: String,
    val momentum: Int,
    val bestScore: Int,
    val averageScore: Int,
    val battleDNA: BattleDNA,
    val records: PersonalRecords
)

data class BattleDNA(
    val speed: Int,
    val memory: Int,
    val people: Int
)

data class PersonalRecords(
    val bestScore: Int,
    val bestMomentum: Int,
    val battlesPlayed: Int
)
