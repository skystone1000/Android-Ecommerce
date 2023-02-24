package com.google.codelabs.mdc.kotlin.shrine.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val product_id : Long,
    val product_name : String,
    val product_price : String,
    val product_url : String,
    val product_quantity : String,
)