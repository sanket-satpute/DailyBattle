package com.sanket_satpute_20.dailybattle.domain.rival

import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

enum class RivalStatus {
    ACTIVE,
    REMOVED
}

data class Rival(
    val userId: UserId,
    val rivalUserId: UserId,
    val rivalName: String,
    val selectedAt: Long,
    val status: RivalStatus
)
