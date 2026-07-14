package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.database.WishlistDao
import com.skystone1000.shrine.core.model.WishlistItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Per-user wishlist of saved products. */
interface WishlistRepository {
    fun wishlist(userId: Long): Flow<List<WishlistItemEntity>>
    fun isWishlisted(userId: Long, productId: Long): Flow<Boolean>
    suspend fun setWishlisted(userId: Long, productId: Long, wishlisted: Boolean)
}

@Singleton
class DefaultWishlistRepository @Inject constructor(
    private val wishlistDao: WishlistDao,
) : WishlistRepository {

    override fun wishlist(userId: Long): Flow<List<WishlistItemEntity>> = wishlistDao.observeByUser(userId)

    override fun isWishlisted(userId: Long, productId: Long): Flow<Boolean> =
        wishlistDao.observeIsWishlisted(userId, productId)

    override suspend fun setWishlisted(userId: Long, productId: Long, wishlisted: Boolean) {
        if (wishlisted) {
            wishlistDao.insert(WishlistItemEntity(userId = userId, productId = productId))
        } else {
            wishlistDao.delete(userId, productId)
        }
    }
}
