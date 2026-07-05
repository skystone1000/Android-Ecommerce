package com.google.codelabs.mdc.kotlin.shrine.database

import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product

/**
 * Add [product] to [userId]'s cart. If the product is already in the cart, increment its quantity
 * instead of inserting a duplicate row — so the cart keeps one row per product per user (bounded
 * growth) and the stored quantity is authoritative. Call off the main thread.
 */
suspend fun CartItemDAO.addOrIncrement(userId: Long, product: Product) {
    val existing = findItem(userId, product.product_id)
    if (existing == null) {
        insertCartItem(
            CartItem(
                0, userId, product.product_id,
                product.product_name, product.product_price, product.product_url, "1"
            )
        )
    } else {
        val quantity = (existing.product_quantity.toIntOrNull() ?: 0) + 1
        updateCartItem(existing.copy(product_quantity = quantity.toString()))
    }
}
