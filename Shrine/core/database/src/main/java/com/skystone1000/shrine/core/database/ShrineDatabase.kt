package com.skystone1000.shrine.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skystone1000.shrine.core.model.AddressEntity
import com.skystone1000.shrine.core.model.CartItemEntity
import com.skystone1000.shrine.core.model.CategoryEntity
import com.skystone1000.shrine.core.model.OrderEntity
import com.skystone1000.shrine.core.model.OrderLineEntity
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.core.model.PromotionEntity
import com.skystone1000.shrine.core.model.RecentSearchEntity
import com.skystone1000.shrine.core.model.UserEntity
import com.skystone1000.shrine.core.model.WishlistItemEntity

/**
 * The modern Shrine database (db file `shrine.db`), backing the new Compose stack. It is separate
 * from the legacy `contactDB` used by the Fragment UI; the two coexist until Phase 5 deletes legacy.
 */
@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CategoryEntity::class,
        PromotionEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderLineEntity::class,
        WishlistItemEntity::class,
        AddressEntity::class,
        PaymentMethodEntity::class,
        RecentSearchEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ShrineDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun promotionDao(): PromotionDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun addressDao(): AddressDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        const val DB_NAME = "shrine.db"
    }
}
