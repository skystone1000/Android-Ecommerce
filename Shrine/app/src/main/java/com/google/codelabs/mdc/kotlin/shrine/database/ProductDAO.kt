package com.google.codelabs.mdc.kotlin.shrine.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.codelabs.mdc.kotlin.shrine.models.Product

@Dao
interface ProductDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("SELECT * FROM products")
    suspend fun getAll(): List<Product>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}
