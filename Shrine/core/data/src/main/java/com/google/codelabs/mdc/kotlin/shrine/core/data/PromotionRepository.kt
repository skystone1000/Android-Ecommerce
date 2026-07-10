package com.google.codelabs.mdc.kotlin.shrine.core.data

import com.google.codelabs.mdc.kotlin.shrine.core.database.PromotionDao
import com.google.codelabs.mdc.kotlin.shrine.core.model.PromotionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Read access to the home hero/promotion banners (seeded by [CatalogRepository]). */
interface PromotionRepository {
    fun promotions(): Flow<List<PromotionEntity>>
}

@Singleton
class DefaultPromotionRepository @Inject constructor(
    private val promotionDao: PromotionDao,
) : PromotionRepository {
    override fun promotions(): Flow<List<PromotionEntity>> = promotionDao.observeAll()
}
