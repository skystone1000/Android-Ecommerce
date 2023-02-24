package com.google.codelabs.mdc.kotlin.shrine.database

import androidx.room.*
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem


@Dao
interface CartItemDAO {
    @Insert
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Query("SELECT * FROM cart")
    suspend fun getAll(): MutableList<CartItem>

    @Query("DELETE FROM cart")
    suspend fun clearCart()

    @Query("DELETE FROM cart Where product_id=:id")
    suspend fun deleteCartItem(id: Long)
}