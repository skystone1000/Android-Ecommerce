package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.database.CartDao
import com.skystone1000.shrine.core.model.CartItemEntity
import com.skystone1000.shrine.core.model.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Subtotal of a set of cart lines, in cents. */
fun List<CartItemEntity>.subtotalCents(): Int = sumOf { it.priceCents * it.quantity }

/** Per-user shopping cart. One row per (product, variant); quantity is a real integer. */
interface CartRepository {
    fun cart(userId: Long): Flow<List<CartItemEntity>>
    fun subtotalCents(userId: Long): Flow<Int>
    fun itemCount(userId: Long): Flow<Int>
    suspend fun add(userId: Long, product: ProductEntity, variant: String? = null, quantity: Int = 1)
    suspend fun setQuantity(itemId: Long, quantity: Int)
    suspend fun remove(itemId: Long)
    suspend fun clear(userId: Long)
}

@Singleton
class DefaultCartRepository @Inject constructor(
    private val cartDao: CartDao,
) : CartRepository {

    override fun cart(userId: Long): Flow<List<CartItemEntity>> = cartDao.observeByUser(userId)

    override fun subtotalCents(userId: Long): Flow<Int> =
        cartDao.observeByUser(userId).map { it.subtotalCents() }

    override fun itemCount(userId: Long): Flow<Int> =
        cartDao.observeByUser(userId).map { items -> items.sumOf { it.quantity } }

    override suspend fun add(userId: Long, product: ProductEntity, variant: String?, quantity: Int) {
        val existing = cartDao.find(userId, product.id, variant)
        if (existing != null) {
            cartDao.updateQuantity(existing.id, existing.quantity + quantity)
        } else {
            cartDao.insert(
                CartItemEntity(
                    userId = userId,
                    productId = product.id,
                    name = product.name,
                    imageUrl = product.imageUrls.firstOrNull(),
                    selectedVariant = variant,
                    priceCents = product.priceCents,
                    quantity = quantity,
                ),
            )
        }
    }

    override suspend fun setQuantity(itemId: Long, quantity: Int) {
        if (quantity <= 0) cartDao.deleteById(itemId) else cartDao.updateQuantity(itemId, quantity)
    }

    override suspend fun remove(itemId: Long) = cartDao.deleteById(itemId)

    override suspend fun clear(userId: Long) = cartDao.clear(userId)
}
