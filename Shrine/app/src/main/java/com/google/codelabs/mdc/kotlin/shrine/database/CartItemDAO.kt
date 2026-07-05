package com.google.codelabs.mdc.kotlin.shrine.database

import androidx.room.*
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem


@Dao
interface CartItemDAO {
    @Insert
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    /** The existing row for this product in this user's cart, or null. Used to upsert quantity. */
    @Query("SELECT * FROM cart WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    suspend fun findItem(userId: Long, productId: Long): CartItem?

    @Query("SELECT * FROM cart WHERE user_id = :userId")
    suspend fun getAll(userId: Long): MutableList<CartItem>

    @Query("DELETE FROM cart WHERE user_id = :userId")
    suspend fun clearCart(userId: Long)

    @Query("DELETE FROM cart WHERE product_id = :id AND user_id = :userId")
    suspend fun deleteCartItem(id: Long, userId: Long)
}