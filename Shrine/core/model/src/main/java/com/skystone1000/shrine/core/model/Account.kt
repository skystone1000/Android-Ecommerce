package com.skystone1000.shrine.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A per-user saved (wishlisted) product. */
@Entity(
    tableName = "wishlist_items",
    indices = [Index(value = ["userId", "productId"], unique = true)],
)
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
)

/** A per-user shipping address (figma checkout). */
@Entity(
    tableName = "addresses",
    indices = [Index(value = ["userId"])],
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val fullName: String,
    val line1: String,
    val line2: String? = null,
    val city: String,
    val state: String,
    val zip: String,
    val isDefault: Boolean = false,
)

/** A per-user masked payment method (figma checkout). No real card data is stored. */
@Entity(
    tableName = "payment_methods",
    indices = [Index(value = ["userId"])],
)
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val brand: String,
    val last4: String,
    val expiry: String,
    val isDefault: Boolean = false,
)

/** A per-user recent search query (figma search "RECENT"). */
@Entity(
    tableName = "recent_searches",
    indices = [Index(value = ["userId", "query"], unique = true)],
)
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val query: String,
    val timestampMillis: Long,
)
