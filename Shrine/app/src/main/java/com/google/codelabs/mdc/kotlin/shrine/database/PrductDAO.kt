package com.google.codelabs.mdc.kotlin.shrine.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.google.codelabs.mdc.kotlin.shrine.models.Product

@Dao
interface PrductDAO {
    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM cart")
    suspend fun getAll(): List<Product>

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}