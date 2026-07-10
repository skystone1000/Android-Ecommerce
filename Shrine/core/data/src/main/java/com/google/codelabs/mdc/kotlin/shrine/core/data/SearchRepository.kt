package com.google.codelabs.mdc.kotlin.shrine.core.data

import com.google.codelabs.mdc.kotlin.shrine.core.database.RecentSearchDao
import com.google.codelabs.mdc.kotlin.shrine.core.model.ProductEntity
import com.google.codelabs.mdc.kotlin.shrine.core.model.RecentSearchEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Recent searches + catalog-derived suggestions (figma Search screen). */
interface SearchRepository {
    fun recentSearches(userId: Long): Flow<List<RecentSearchEntity>>
    suspend fun recordSearch(userId: Long, query: String)
    suspend fun clearRecent(userId: Long)
    suspend fun suggestions(query: String, limit: Int = 5): List<ProductEntity>
    suspend fun results(query: String): List<ProductEntity>
}

@Singleton
class DefaultSearchRepository @Inject constructor(
    private val recentSearchDao: RecentSearchDao,
    private val catalogRepository: CatalogRepository,
) : SearchRepository {

    override fun recentSearches(userId: Long): Flow<List<RecentSearchEntity>> =
        recentSearchDao.observeByUser(userId)

    override suspend fun recordSearch(userId: Long, query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        recentSearchDao.upsert(
            RecentSearchEntity(userId = userId, query = trimmed, timestampMillis = System.currentTimeMillis()),
        )
    }

    override suspend fun clearRecent(userId: Long) = recentSearchDao.clear(userId)

    override suspend fun suggestions(query: String, limit: Int): List<ProductEntity> =
        catalogRepository.suggestions(query, limit)

    override suspend fun results(query: String): List<ProductEntity> = catalogRepository.search(query)
}
