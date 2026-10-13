package com.sanket_satpute_20.dailybattle.data.monetization

import com.sanket_satpute_20.dailybattle.domain.monetization.model.Entitlement
import com.sanket_satpute_20.dailybattle.domain.monetization.model.EntitlementState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.PurchaseState
import com.sanket_satpute_20.dailybattle.domain.monetization.model.RestoreState
import com.sanket_satpute_20.dailybattle.domain.monetization.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default thread-safe implementation of [EntitlementRepository].
 *
 * Implements Sprint 21.1 per `13_MONETIZATION.md` §24, §36, §37, §38, §39:
 * - Thread-safe access via [Mutex].
 * - Automatically computes [EntitlementState] based on active vs expired entitlements.
 * - Handles expiration dynamically using injected [Clock].
 * - Preserves free core experience by defaulting to [EntitlementState.Free].
 */
@Singleton
class DefaultEntitlementRepository @Inject constructor(
    private val clock: Clock = Clock.systemDefaultZone()
) : EntitlementRepository {

    private val mutex = Mutex()
    private val entitlementsMap = mutableMapOf<String, Entitlement>()

    private val _entitlementState = MutableStateFlow<EntitlementState>(EntitlementState.Free)
    override val entitlementState: StateFlow<EntitlementState> = _entitlementState.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    override val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    override suspend fun getActiveEntitlements(): List<Entitlement> = mutex.withLock {
        val now = clock.millis()
        entitlementsMap.values.filter { it.isActive(now) }
    }

    override suspend fun getAllEntitlements(): List<Entitlement> = mutex.withLock {
        entitlementsMap.values.toList()
    }

    override suspend fun hasAccessToFeature(featureId: String): Boolean = mutex.withLock {
        // §26 & §36: Unknown state fails-closed for optional premium features
        if (_entitlementState.value is EntitlementState.Unknown) {
            return false
        }
        val now = clock.millis()
        entitlementsMap.values.any { it.featureId == featureId && it.isActive(now) }
    }

    override suspend fun grantEntitlement(entitlement: Entitlement) = mutex.withLock {
        entitlementsMap[entitlement.id] = entitlement
        recalculateEntitlementStateLocked()
    }

    override suspend fun revokeEntitlement(entitlementId: String) = mutex.withLock {
        entitlementsMap.remove(entitlementId)
        recalculateEntitlementStateLocked()
    }

    override suspend fun clearAllEntitlements() = mutex.withLock {
        entitlementsMap.clear()
        _entitlementState.value = EntitlementState.Free
        _purchaseState.value = PurchaseState.Idle
        _restoreState.value = RestoreState.Idle
    }

    override suspend fun setUnknownState(reason: String?) = mutex.withLock {
        _entitlementState.value = EntitlementState.Unknown(reason)
    }

    override suspend fun updatePurchaseState(newState: PurchaseState) {
        _purchaseState.value = newState
    }

    override suspend fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    override suspend fun updateRestoreState(newState: RestoreState) {
        _restoreState.value = newState
    }

    override suspend fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    /**
     * Re-evaluates active vs expired status (e.g. periodically or on lifecycle resume).
     */
    suspend fun refreshExpirations() = mutex.withLock {
        recalculateEntitlementStateLocked()
    }

    private fun recalculateEntitlementStateLocked() {
        val now = clock.millis()
        val all = entitlementsMap.values.toList()
        val active = all.filter { it.isActive(now) }
        val expired = all.filter { it.isExpired(now) }

        _entitlementState.value = when {
            active.isNotEmpty() -> EntitlementState.Premium(active)
            expired.isNotEmpty() -> EntitlementState.Expired(expired)
            else -> EntitlementState.Free
        }
    }
}
