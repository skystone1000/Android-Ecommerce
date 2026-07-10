package com.google.codelabs.mdc.kotlin.shrine.core.data

import com.google.codelabs.mdc.kotlin.shrine.core.database.CatalogSeed
import com.google.codelabs.mdc.kotlin.shrine.core.database.CategoryDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.ProductDao
import com.google.codelabs.mdc.kotlin.shrine.core.database.PromotionDao
import com.google.codelabs.mdc.kotlin.shrine.core.model.CategoryEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Read access to the product catalog, seeding the bundled [CatalogSeed] into Room on first run. */
interface CatalogRepository {
    suspend fun ensureSeeded()
    fun products(): Flow<List<ProductEntity>>
    fun categories(): Flow<List<CategoryEntity>>
    fun productsByCategory(categoryId: String): Flow<List<ProductEntity>>
    fun product(id: Long): Flow<ProductEntity?>
    suspend fun getProduct(id: Long): ProductEntity?
    suspend fun search(query: String): List<ProductEntity>
    suspend fun suggestions(query: String, limit: Int = 5): List<ProductEntity>
}

@Singleton
class DefaultCatalogRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val promotionDao: PromotionDao,
) : CatalogRepository {

    override suspend fun ensureSeeded() {
        if (productDao.count() == 0) productDao.insertAll(CatalogSeed.products)
        if (categoryDao.count() == 0) categoryDao.insertAll(CatalogSeed.categories)
        if (promotionDao.count() == 0) promotionDao.insertAll(CatalogSeed.promotions)
    }

    override fun products(): Flow<List<ProductEntity>> = productDao.observeAll()
    override fun categories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()
    override fun productsByCategory(categoryId: String): Flow<List<ProductEntity>> =
        productDao.observeByCategory(categoryId)
    override fun product(id: Long): Flow<ProductEntity?> = productDao.observeById(id)
    override suspend fun getProduct(id: Long): ProductEntity? = productDao.getById(id)
    override suspend fun search(query: String): List<ProductEntity> =
        if (query.isBlank()) emptyList() else productDao.search(query.trim())
    override suspend fun suggestions(query: String, limit: Int): List<ProductEntity> =
        search(query).take(limit)
}
