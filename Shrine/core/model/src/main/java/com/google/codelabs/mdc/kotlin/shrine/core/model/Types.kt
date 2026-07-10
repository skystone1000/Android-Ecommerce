package com.google.codelabs.mdc.kotlin.shrine.core.model

import kotlinx.serialization.Serializable

/** A purchasable product variant, e.g. label "Sand" / type "Over-ear" (figma product detail & cart). */
@Serializable
data class Variant(
    val label: String,
    val type: String,
)

/** Lifecycle of an order (figma order history). */
enum class OrderStatus { PLACED, IN_TRANSIT, DELIVERED }

/** Delivery choices at checkout; [shippingCents] feeds the order total. */
enum class DeliveryOption(val label: String, val shippingCents: Int) {
    STANDARD("Standard", 0),
    EXPRESS("Express", 1200),
}

/** User-facing theme preference persisted in settings (maps to the UI theme in the app layer). */
enum class ThemePreference { SYSTEM, LIGHT, DARK }

/** Prices are stored as integer minor units (cents); this is the single place that renders them. */
object Money {
    fun format(cents: Int): String {
        val dollars = cents / 100.0
        return if (cents % 100 == 0) "$%,d".format(cents / 100) else "$%,.2f".format(dollars)
    }
}
