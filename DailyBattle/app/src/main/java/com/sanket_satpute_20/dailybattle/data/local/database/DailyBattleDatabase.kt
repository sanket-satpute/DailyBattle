package com.sanket_satpute_20.dailybattle.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.BattleSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeResultDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ChallengeSessionDao
import com.sanket_satpute_20.dailybattle.data.local.dao.FriendDao
import com.sanket_satpute_20.dailybattle.data.local.dao.ProfileDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalDao
import com.sanket_satpute_20.dailybattle.data.local.dao.RivalMatchHistoryDao
import com.sanket_satpute_20.dailybattle.data.local.dao.UserDao
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleHistoryItemEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.BattleSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeResultEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ChallengeSessionEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.FriendEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.ProfileEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.RivalMatchHistoryEntity
import com.sanket_satpute_20.dailybattle.data.local.entity.UserEntity

/**
 * Authoritative local Room database for Daily Battle.
 *
 * Implements approved technical decision TECH-PENDING-006.
 */
@Database(
    entities = [
        UserEntity::class,
        BattleEntity::class,
        ChallengeEntity::class,
        BattleSessionEntity::class,
        ChallengeSessionEntity::class,
        ChallengeResultEntity::class,
        BattleResultEntity::class,
        FriendEntity::class,
        RivalEntity::class,
        RivalMatchHistoryEntity::class,
        ProfileEntity::class,
        BattleHistoryItemEntity::class,
    ],
    version = DailyBattleDatabase.DATABASE_VERSION,
    exportSchema = true,
)
abstract class DailyBattleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun battleDao(): BattleDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun battleSessionDao(): BattleSessionDao
    abstract fun challengeSessionDao(): ChallengeSessionDao
    abstract fun challengeResultDao(): ChallengeResultDao
    abstract fun battleResultDao(): BattleResultDao
    abstract fun friendDao(): FriendDao
    abstract fun rivalDao(): RivalDao
    abstract fun rivalMatchHistoryDao(): RivalMatchHistoryDao
    abstract fun profileDao(): ProfileDao
    abstract fun battleHistoryDao(): BattleHistoryDao

    companion object {
        const val DATABASE_NAME = "daily_battle.db"
        const val DATABASE_VERSION = 1
    }
}
