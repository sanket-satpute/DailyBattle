package com.sanket_satpute_20.dailybattle.domain.monetization.model

/**
 * Represents a purchasable catalog product offered through the billing provider.
 *
 * Implements Sprint 21.2 per `13_MONETIZATION.md` §22, §23:
 * - Transparent product identification, title, description, and localized pricing.
 * - Maps product to its corresponding unlocked [featureId].
 */
data class BillingProduct(
    val id: String,
    val type: ProductType,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val currencyCode: String,
    val priceMicros: Long,
    val featureId: String
)

/**
 * The billing model category of a product.
 */
enum class ProductType {
    IN_APP,
    SUBSCRIPTION
}
