package com.sanket_satpute_20.dailybattle.data.di

import android.content.Context
import androidx.room.Room
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
import com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase
import com.sanket_satpute_20.dailybattle.data.local.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDailyBattleDatabase(
        @ApplicationContext context: Context,
    ): DailyBattleDatabase = Room.databaseBuilder(
        context,
        DailyBattleDatabase::class.java,
        DailyBattleDatabase.DATABASE_NAME,
    )
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .build()

    @Provides
    fun provideUserDao(database: DailyBattleDatabase): UserDao = database.userDao()

    @Provides
    fun provideBattleDao(database: DailyBattleDatabase): BattleDao = database.battleDao()

    @Provides
    fun provideChallengeDao(database: DailyBattleDatabase): ChallengeDao = database.challengeDao()

    @Provides
    fun provideBattleSessionDao(database: DailyBattleDatabase): BattleSessionDao = database.battleSessionDao()

    @Provides
    fun provideChallengeSessionDao(database: DailyBattleDatabase): ChallengeSessionDao = database.challengeSessionDao()

    @Provides
    fun provideChallengeResultDao(database: DailyBattleDatabase): ChallengeResultDao = database.challengeResultDao()

    @Provides
    fun provideBattleResultDao(database: DailyBattleDatabase): BattleResultDao = database.battleResultDao()

    @Provides
    fun provideFriendDao(database: DailyBattleDatabase): FriendDao = database.friendDao()

    @Provides
    fun provideRivalDao(database: DailyBattleDatabase): RivalDao = database.rivalDao()

    @Provides
    fun provideRivalMatchHistoryDao(database: DailyBattleDatabase): RivalMatchHistoryDao = database.rivalMatchHistoryDao()

    @Provides
    fun provideProfileDao(database: DailyBattleDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideBattleHistoryDao(database: DailyBattleDatabase): BattleHistoryDao = database.battleHistoryDao()
}
