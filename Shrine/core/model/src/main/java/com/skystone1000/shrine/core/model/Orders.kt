package com.skystone1000.shrine.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A placed order header (figma order history / order placed). */
@Entity(
    tableName = "orders",
    indices = [Index(value = ["userId"])],
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val placedAtMillis: Long,
    val subtotalCents: Int,
    val shippingCents: Int,
    val totalCents: Int,
    val deliveryOption: DeliveryOption,
    val estimatedArrival: String? = null,
)

/** A snapshot line item belonging to an [OrderEntity]. */
@Entity(
    tableName = "order_lines",
    indices = [Index(value = ["orderId"])],
)
data class OrderLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val name: String,
    val imageUrl: String?,
    val selectedVariant: String?,
    val priceCents: Int,
    val quantity: Int,
)

/** An order with its line items (Room @Relation result). */
data class OrderWithLines(
    @androidx.room.Embedded val order: OrderEntity,
    @androidx.room.Relation(parentColumn = "id", entityColumn = "orderId")
    val lines: List<OrderLineEntity>,
)
