package com.google.codelabs.mdc.kotlin.shrine.core.database.di

import android.content.Context
import androidx.room.Room
import com.google.codelabs.mdc.kotlin.shrine.core.database.AddressDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.CartDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.CategoryDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.OrderDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.PaymentMethodDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.ProductDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.PromotionDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.RecentSearchDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.core.database.UserDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.WishlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShrineDatabase =
        Room.databaseBuilder(context, ShrineDatabase::class.java, ShrineDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun userDao(db: ShrineDatabase): UserDao = db.userDao()
    @Provides fun productDao(db: ShrineDatabase): ProductDao = db.productDao()
    @Provides fun categoryDao(db: ShrineDatabase): CategoryDao = db.categoryDao()
    @Provides fun promotionDao(db: ShrineDatabase): PromotionDao = db.promotionDao()
    @Provides fun cartDao(db: ShrineDatabase): CartDao = db.cartDao()
    @Provides fun orderDao(db: ShrineDatabase): OrderDao = db.orderDao()
    @Provides fun wishlistDao(db: ShrineDatabase): WishlistDao = db.wishlistDao()
    @Provides fun addressDao(db: ShrineDatabase): AddressDao = db.addressDao()
    @Provides fun paymentMethodDao(db: ShrineDatabase): PaymentMethodDao = db.paymentMethodDao()
    @Provides fun recentSearchDao(db: ShrineDatabase): RecentSearchDao = db.recentSearchDao()
}
