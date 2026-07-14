package com.skystone1000.shrine.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A line in a user's cart: one row per (user, product, variant). [priceCents] snapshots the
 * product price so totals are stable and decimal-safe even if the catalog price later changes.
 */
@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["userId", "productId", "selectedVariant"], unique = true)],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val name: String,
    val imageUrl: String?,
    val selectedVariant: String? = null,
    val priceCents: Int,
    val quantity: Int = 1,
)
