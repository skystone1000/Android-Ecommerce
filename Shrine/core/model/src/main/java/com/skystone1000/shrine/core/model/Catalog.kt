package com.skystone1000.shrine.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A catalog product. Price is in integer minor units (cents); [originalPriceCents] is the
 * struck-through pre-sale price when present. [imageUrls] and [variants] are stored via
 * TypeConverters (JSON) registered on the database.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: String,
    val description: String,
    val priceCents: Int,
    val originalPriceCents: Int? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val isNew: Boolean = false,
)

/** A top-level shopping category (figma Home category tiles). [iconKey] maps to a UI icon. */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
)

/** A hero/promotion banner shown on Home. */
@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eyebrow: String,
    val title: String,
    val ctaLabel: String,
)
