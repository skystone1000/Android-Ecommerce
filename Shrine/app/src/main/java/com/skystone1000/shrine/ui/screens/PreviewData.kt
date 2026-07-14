package com.skystone1000.shrine.ui.screens

import com.skystone1000.shrine.core.model.AddressEntity
import com.skystone1000.shrine.core.model.CartItemEntity
import com.skystone1000.shrine.core.model.CategoryEntity
import com.skystone1000.shrine.core.model.DeliveryOption
import com.skystone1000.shrine.core.model.OrderEntity
import com.skystone1000.shrine.core.model.OrderLineEntity
import com.skystone1000.shrine.core.model.OrderStatus
import com.skystone1000.shrine.core.model.OrderWithLines
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.core.model.PromotionEntity
import com.skystone1000.shrine.core.model.Variant

/**
 * Static sample data for `@Preview` rendering only. Mirrors the shape of [com.skystone1000.shrine.core.database.CatalogSeed]
 * but lives in `:app` so previews don't need a database or Hilt.
 */
internal object PreviewData {

    val products: List<ProductEntity> = listOf(
        ProductEntity(
            id = 1, name = "Aether Wireless", categoryId = "audio",
            description = "Adaptive hybrid noise cancellation, 40-hour battery, and a machined aluminium frame.",
            priceCents = 129_900, originalPriceCents = 149_900, rating = 4.0f, reviewCount = 218,
            imageUrls = listOf("", "", "", ""),
            variants = listOf(Variant("Sand", "Over-ear"), Variant("Charcoal", "Over-ear")),
            isNew = true,
        ),
        ProductEntity(
            id = 2, name = "Pulse Buds Pro", categoryId = "audio",
            description = "In-ear buds with active noise cancellation and a compact charging case.",
            priceCents = 24_900, rating = 4.0f, reviewCount = 96, variants = listOf(Variant("Ivory", "In-ear")),
        ),
        ProductEntity(
            id = 3, name = "Mono Speaker", categoryId = "audio",
            description = "A single full-range driver in a walnut enclosure.", priceCents = 42_000, rating = 4.5f, reviewCount = 54,
        ),
        ProductEntity(
            id = 4, name = "Field Watch", categoryId = "fashion",
            description = "Titanium field watch with a sapphire crystal.", priceCents = 89_000, rating = 5.0f, reviewCount = 142,
        ),
    )

    val product: ProductEntity = products.first()

    val categories: List<CategoryEntity> = listOf(
        CategoryEntity("audio", "Audio", "headphones"),
        CategoryEntity("fashion", "Fashion", "checkroom"),
        CategoryEntity("beauty", "Beauty", "spa"),
        CategoryEntity("home", "Home", "chair"),
    )

    val promotion = PromotionEntity(id = 1, eyebrow = "SUMMER EDIT", title = "The quiet luxury edit", ctaLabel = "Shop now")

    val cartItems: List<CartItemEntity> = listOf(
        CartItemEntity(id = 1, userId = 1, productId = 1, name = "Aether Wireless", imageUrl = null, selectedVariant = "Sand", priceCents = 129_900, quantity = 1),
        CartItemEntity(id = 2, userId = 1, productId = 2, name = "Pulse Buds Pro", imageUrl = null, selectedVariant = null, priceCents = 24_900, quantity = 2),
    )

    val address = AddressEntity(id = 1, userId = 1, fullName = "Ava Morgan", line1 = "120 Market St", city = "San Francisco", state = "CA", zip = "94103", isDefault = true)

    val payment = PaymentMethodEntity(id = 1, userId = 1, brand = "Visa", last4 = "4242", expiry = "08/27", isDefault = true)

    val order = OrderWithLines(
        order = OrderEntity(
            id = 1, userId = 1, orderNumber = "SH-1042", status = OrderStatus.IN_TRANSIT, placedAtMillis = 0,
            subtotalCents = 154_800, shippingCents = 0, totalCents = 154_800, deliveryOption = DeliveryOption.STANDARD,
            estimatedArrival = "3–5 business days",
        ),
        lines = listOf(
            OrderLineEntity(id = 1, orderId = 1, productId = 1, name = "Aether Wireless", imageUrl = null, selectedVariant = "Sand", priceCents = 129_900, quantity = 1),
            OrderLineEntity(id = 2, orderId = 1, productId = 2, name = "Pulse Buds Pro", imageUrl = null, selectedVariant = null, priceCents = 24_900, quantity = 1),
        ),
    )
}
