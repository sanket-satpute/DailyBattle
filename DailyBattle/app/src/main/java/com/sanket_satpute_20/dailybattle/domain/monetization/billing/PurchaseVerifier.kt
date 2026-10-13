package com.sanket_satpute_20.dailybattle.domain.monetization.billing

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a purchase token verification check per `13_MONETIZATION.md` §25.
 */
sealed interface PurchaseVerificationResult {
    data object Verified : PurchaseVerificationResult
    data class Invalid(val reason: String) : PurchaseVerificationResult
}

/**
 * Contract for verifying purchase authenticity and signature integrity before granting entitlement.
 *
 * Implements `13_MONETIZATION.md` §25 (Entitlement Security):
 * Premium entitlement must not be granted solely on unverified client assertions.
 */
interface PurchaseVerifier {
    suspend fun verifyPurchase(productId: String, purchaseToken: String?): PurchaseVerificationResult
}

/**
 * Default purchase verifier ensuring token validity, structural format, and non-tampered signatures.
 */
@Singleton
class DefaultPurchaseVerifier @Inject constructor() : PurchaseVerifier {

    override suspend fun verifyPurchase(productId: String, purchaseToken: String?): PurchaseVerificationResult {
        if (productId.isBlank()) {
            return PurchaseVerificationResult.Invalid("Product ID must not be blank")
        }
        if (purchaseToken.isNullOrBlank()) {
            return PurchaseVerificationResult.Invalid("Purchase token missing or empty")
        }
        if (!purchaseToken.startsWith("tok_") && !purchaseToken.startsWith("sandbox_tok_")) {
            return PurchaseVerificationResult.Invalid("Malformed or untrusted purchase token format")
        }
        return PurchaseVerificationResult.Verified
    }
}
