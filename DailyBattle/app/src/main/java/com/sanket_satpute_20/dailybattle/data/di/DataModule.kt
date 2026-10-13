package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleResultRepository
import com.sanket_satpute_20.dailybattle.data.battle.RoomBattleSessionRepository
import com.sanket_satpute_20.dailybattle.data.friend.RoomFriendRepository
import com.sanket_satpute_20.dailybattle.data.profile.RoomProfileRepository
import com.sanket_satpute_20.dailybattle.data.rival.RoomRivalRepository
import com.sanket_satpute_20.dailybattle.data.settings.DataStoreSettingsRepository
import com.sanket_satpute_20.dailybattle.data.user.RoomUserRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.friend.FriendRepository
import com.sanket_satpute_20.dailybattle.domain.profile.ProfileRepository
import com.sanket_satpute_20.dailybattle.domain.rival.RivalRepository
import com.sanket_satpute_20.dailybattle.domain.settings.SettingsRepository
import com.sanket_satpute_20.dailybattle.domain.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        roomUserRepository: RoomUserRepository,
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindBattleSessionRepository(
        roomBattleSessionRepository: RoomBattleSessionRepository,
    ): BattleSessionRepository

    @Binds
    @Singleton
    abstract fun bindBattleResultRepository(
        roomBattleResultRepository: RoomBattleResultRepository,
    ): BattleResultRepository

    @Binds
    @Singleton
    abstract fun bindRivalRepository(
        roomRivalRepository: RoomRivalRepository,
    ): RivalRepository

    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        roomFriendRepository: RoomFriendRepository,
    ): FriendRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        roomProfileRepository: RoomProfileRepository,
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        dataStoreSettingsRepository: DataStoreSettingsRepository,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPracticeSessionRepository(
        inMemoryPracticeSessionRepository: com.sanket_satpute_20.dailybattle.data.practice.InMemoryPracticeSessionRepository,
    ): com.sanket_satpute_20.dailybattle.domain.practice.PracticeSessionRepository
}
