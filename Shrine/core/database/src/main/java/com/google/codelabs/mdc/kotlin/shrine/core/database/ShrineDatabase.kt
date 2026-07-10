package com.google.codelabs.mdc.kotlin.shrine.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.codelabs.mdc.kotlin.shrine.core.model.AddressEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.CartItemEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.CategoryEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.OrderEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.OrderLineEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.PaymentMethodEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.PromotionEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.RecentSearchEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.UserEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.WishlistItemEntity

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
