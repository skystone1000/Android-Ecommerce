package com.google.codelabs.mdc.kotlin.shrine.core.data.di

import com.google.codelabs.mdc.kotlin.shrine.core.data.AddressRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.AuthRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.CartRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.CatalogRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DataStoreSessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DataStoreSettingsRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultAddressRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultAuthRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultCartRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultCatalogRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultOrderRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultPaymentRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultPromotionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultSearchRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.DefaultWishlistRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.OrderRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.PaymentRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.PromotionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SearchRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SessionRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.SettingsRepository
import com.google.codelabs.mdc.kotlin.shrine.core.data.WishlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindCatalogRepository(impl: DefaultCatalogRepository): CatalogRepository

    @Binds @Singleton
    abstract fun bindCartRepository(impl: DefaultCartRepository): CartRepository

    @Binds @Singleton
    abstract fun bindOrderRepository(impl: DefaultOrderRepository): OrderRepository

    @Binds @Singleton
    abstract fun bindWishlistRepository(impl: DefaultWishlistRepository): WishlistRepository

    @Binds @Singleton
    abstract fun bindPromotionRepository(impl: DefaultPromotionRepository): PromotionRepository

    @Binds @Singleton
    abstract fun bindSearchRepository(impl: DefaultSearchRepository): SearchRepository

    @Binds @Singleton
    abstract fun bindAddressRepository(impl: DefaultAddressRepository): AddressRepository

    @Binds @Singleton
    abstract fun bindPaymentRepository(impl: DefaultPaymentRepository): PaymentRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: DataStoreSessionRepository): SessionRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
