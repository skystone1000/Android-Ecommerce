package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.database.PromotionDao
import com.skystone1000.shrine.core.model.PromotionEntity
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
