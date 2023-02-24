package com.google.codelabs.mdc.kotlin.shrine.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val cart_item_id : Long,
    val product_id : Long,
    val product_name : String,
    val product_price : String,
    val product_url : String,
    var product_quantity : String,
)
