package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.database.CartDao
import com.skystone1000.shrine.core.database.OrderDao
import com.skystone1000.shrine.core.model.DeliveryOption
import com.skystone1000.shrine.core.model.OrderEntity
import com.skystone1000.shrine.core.model.OrderLineEntity
import com.skystone1000.shrine.core.model.OrderStatus
import com.skystone1000.shrine.core.model.OrderWithLines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/** Order history + checkout: turns the current cart into a persisted order. */
interface OrderRepository {
    fun orders(userId: Long): Flow<List<OrderWithLines>>
    suspend fun getOrder(orderId: Long): OrderWithLines?
    /** Places an order from the user's cart, clears the cart, and returns the new order id (or null if empty). */
    suspend fun placeOrder(userId: Long, delivery: DeliveryOption): Long?
}

@Singleton
class DefaultOrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
) : OrderRepository {

    override fun orders(userId: Long): Flow<List<OrderWithLines>> = orderDao.observeOrders(userId)

    override suspend fun getOrder(orderId: Long): OrderWithLines? = orderDao.getOrder(orderId)

    override suspend fun placeOrder(userId: Long, delivery: DeliveryOption): Long? {
        val items = cartDao.observeByUser(userId).first()
        if (items.isEmpty()) return null

        val subtotal = items.subtotalCents()
        val shipping = delivery.shippingCents
        val order = OrderEntity(
            userId = userId,
            orderNumber = "SH-${Random.nextInt(1000, 9999)}",
            status = OrderStatus.PLACED,
            placedAtMillis = System.currentTimeMillis(),
            subtotalCents = subtotal,
            shippingCents = shipping,
            totalCents = subtotal + shipping,
            deliveryOption = delivery,
            estimatedArrival = if (delivery == DeliveryOption.EXPRESS) "1–2 business days" else "3–5 business days",
        )
        val orderId = orderDao.insertOrder(order)
        orderDao.insertLines(
            items.map { item ->
                OrderLineEntity(
                    orderId = orderId,
                    productId = item.productId,
                    name = item.name,
                    imageUrl = item.imageUrl,
                    selectedVariant = item.selectedVariant,
                    priceCents = item.priceCents,
                    quantity = item.quantity,
                )
            },
        )
        cartDao.clear(userId)
        return orderId
    }
}
