package com.sanket_satpute_20.dailybattle.core.security.di

import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import com.sanket_satpute_20.dailybattle.core.security.audit.SecurityAuditor
import com.sanket_satpute_20.dailybattle.core.security.auth.SecurityAuthValidator
import com.sanket_satpute_20.dailybattle.core.security.debug.DebugEndpointGuard
import com.sanket_satpute_20.dailybattle.core.security.logging.SecurityLoggingInterceptor
import com.sanket_satpute_20.dailybattle.core.security.ownership.BattleOwnershipValidator
import com.sanket_satpute_20.dailybattle.core.security.score.ScoreAuthorityEnforcer
import com.sanket_satpute_20.dailybattle.core.security.secrets.SecretsAudit
import com.sanket_satpute_20.dailybattle.core.security.storage.SecureStorageValidator
import com.sanket_satpute_20.dailybattle.core.security.validation.ApiSecurityValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurityAuthValidator(): SecurityAuthValidator = SecurityAuthValidator()

    @Provides
    @Singleton
    fun provideSecureStorageValidator(): SecureStorageValidator = SecureStorageValidator()

    @Provides
    @Singleton
    fun provideApiSecurityValidator(): ApiSecurityValidator = ApiSecurityValidator()

    @Provides
    @Singleton
    fun provideScoreAuthorityEnforcer(): ScoreAuthorityEnforcer = ScoreAuthorityEnforcer()

    @Provides
    @Singleton
    fun provideBattleOwnershipValidator(): BattleOwnershipValidator = BattleOwnershipValidator()

    @Provides
    @Singleton
    fun provideSecretsAudit(): SecretsAudit = SecretsAudit()

    @Provides
    @Singleton
    fun provideDebugEndpointGuard(): DebugEndpointGuard = DebugEndpointGuard()

    @Provides
    @Singleton
    fun provideSecurityLoggingInterceptor(
        environmentProvider: EnvironmentProvider
    ): SecurityLoggingInterceptor = SecurityLoggingInterceptor(
        environment = environmentProvider.current
    )

    @Provides
    @Singleton
    fun provideSecurityAuditor(
        authValidator: SecurityAuthValidator,
        storageValidator: SecureStorageValidator,
        apiValidator: ApiSecurityValidator,
        scoreEnforcer: ScoreAuthorityEnforcer,
        ownershipValidator: BattleOwnershipValidator,
        secretsAudit: SecretsAudit,
        debugGuard: DebugEndpointGuard,
        environmentProvider: EnvironmentProvider,
    ): SecurityAuditor = SecurityAuditor(
        authValidator = authValidator,
        storageValidator = storageValidator,
        apiValidator = apiValidator,
        scoreEnforcer = scoreEnforcer,
        ownershipValidator = ownershipValidator,
        secretsAudit = secretsAudit,
        debugGuard = debugGuard,
        environmentProvider = environmentProvider,
    )
}
