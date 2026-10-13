package com.sanket_satpute_20.dailybattle.data.di

import com.sanket_satpute_20.dailybattle.data.monetization.DefaultEntitlementRepository
import com.sanket_satpute_20.dailybattle.data.monetization.SandboxBillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.BillingProvider
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.DefaultPurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.billing.PurchaseVerifier
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonetizationModule {

    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(
        defaultEntitlementRepository: DefaultEntitlementRepository
    ): EntitlementRepository

    @Binds
    @Singleton
    abstract fun bindBillingProvider(
        sandboxBillingProvider: SandboxBillingProvider
    ): BillingProvider

    @Binds
    @Singleton
    abstract fun bindPurchaseVerifier(
        defaultPurchaseVerifier: DefaultPurchaseVerifier
    ): PurchaseVerifier
}
