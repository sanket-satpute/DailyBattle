package com.sanket_satpute_20.dailybattle.data.local.mapper

import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.user.AccountStatus
import com.sanket_satpute_20.dailybattle.domain.user.User

/**
 * Bidirectional mapper between [UserEntity] and [User].
 */
fun UserEntity.toDomain(): User {
    val status = runCatching { AccountStatus.valueOf(accountStatus) }
        .getOrDefault(AccountStatus.ACTIVE)
    return User(
        userId = UserId(userId),
        battleName = battleName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        accountStatus = status,
    )
}

fun User.toEntity(): UserEntity = UserEntity(
    userId = userId.value,
    battleName = battleName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    accountStatus = accountStatus.name,
)
