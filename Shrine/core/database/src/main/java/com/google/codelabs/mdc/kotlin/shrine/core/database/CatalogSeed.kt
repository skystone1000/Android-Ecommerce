package com.google.codelabs.mdc.kotlin.shrine.core.database

import com.google.codelabs.mdc.kotlin.shrine.core.model.CategoryEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.PromotionEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.Variant

/**
 * Bundled catalog seed for the modern data layer, loaded on first run by the catalog repository.
 *
 * This supersedes the legacy `res/raw/products.json`, which lacked the categories, ratings,
 * variants and cents-based prices the new schema (and figma design) require.
 */
object CatalogSeed {

    val categories: List<CategoryEntity> = listOf(
        CategoryEntity("audio", "Audio", "headphones"),
        CategoryEntity("fashion", "Fashion", "checkroom"),
        CategoryEntity("beauty", "Beauty", "spa"),
        CategoryEntity("home", "Home", "chair"),
    )

    val promotions: List<PromotionEntity> = listOf(
        PromotionEntity(eyebrow = "SUMMER EDIT", title = "The quiet luxury edit", ctaLabel = "Shop now"),
    )

    val products: List<ProductEntity> = listOf(
        ProductEntity(
            name = "Aether Wireless",
            categoryId = "audio",
            description = "Adaptive hybrid noise cancellation, 40-hour battery, and a machined aluminium frame. Tuned for warm, natural sound.",
            priceCents = 129_900,
            originalPriceCents = 149_900,
            rating = 4.0f,
            reviewCount = 218,
            imageUrls = listOf("", "", "", ""),
            variants = listOf(Variant("Sand", "Over-ear"), Variant("Charcoal", "Over-ear")),
            isNew = true,
        ),
        ProductEntity(
            name = "Pulse Buds Pro",
            categoryId = "audio",
            description = "In-ear buds with active noise cancellation and a compact charging case.",
            priceCents = 24_900,
            rating = 4.0f,
            reviewCount = 96,
            variants = listOf(Variant("Ivory", "In-ear")),
        ),
        ProductEntity(
            name = "Mono Speaker",
            categoryId = "audio",
            description = "A single full-range driver in a walnut enclosure for honest, room-filling sound.",
            priceCents = 42_000,
            rating = 4.5f,
            reviewCount = 54,
            variants = listOf(Variant("Walnut", "Speaker")),
        ),
        ProductEntity(
            name = "Field Watch",
            categoryId = "fashion",
            description = "Titanium field watch with a sapphire crystal and 200m water resistance.",
            priceCents = 89_000,
            rating = 5.0f,
            reviewCount = 142,
            variants = listOf(Variant("Titanium", "Watch")),
        ),
        ProductEntity(
            name = "Cardigan No.4",
            categoryId = "fashion",
            description = "A relaxed merino cardigan, garment-dyed for a soft, lived-in finish.",
            priceCents = 21_000,
            rating = 4.0f,
            reviewCount = 31,
            variants = listOf(Variant("Oat", "Knitwear")),
        ),
        ProductEntity(
            name = "Lumen Diffuser",
            categoryId = "beauty",
            description = "Ceramic ultrasonic diffuser with a warm ambient glow and 8-hour runtime.",
            priceCents = 14_800,
            rating = 4.8f,
            reviewCount = 173,
            variants = listOf(Variant("Ivory", "Ceramic")),
            isNew = true,
        ),
        ProductEntity(
            name = "Atelier No.1 Eau de Parfum",
            categoryId = "beauty",
            description = "A woody-amber fragrance with bergamot, cedar and a whisper of incense.",
            priceCents = 16_500,
            rating = 4.6f,
            reviewCount = 88,
        ),
        ProductEntity(
            name = "Linen Throw",
            categoryId = "home",
            description = "Stonewashed pure-linen throw with a hand-knotted fringe.",
            priceCents = 12_000,
            rating = 4.3f,
            reviewCount = 27,
            variants = listOf(Variant("Clay", "Linen")),
        ),
    )
}
