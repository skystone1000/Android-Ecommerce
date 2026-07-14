package com.skystone1000.shrine.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.skystone1000.shrine.core.model.AddressEntity
import com.skystone1000.shrine.core.model.CartItemEntity
import com.skystone1000.shrine.core.model.CategoryEntity
import com.skystone1000.shrine.core.model.OrderEntity
import com.skystone1000.shrine.core.model.OrderLineEntity
import com.skystone1000.shrine.core.model.OrderWithLines
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.core.model.PromotionEntity
import com.skystone1000.shrine.core.model.RecentSearchEntity
import com.skystone1000.shrine.core.model.UserEntity
import com.skystone1000.shrine.core.model.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY id")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY id")
    fun observeByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY id")
    suspend fun search(query: String): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface PromotionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(promotions: List<PromotionEntity>)

    @Query("SELECT * FROM promotions")
    fun observeAll(): Flow<List<PromotionEntity>>

    @Query("SELECT COUNT(*) FROM promotions")
    suspend fun count(): Int
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY id")
    fun observeByUser(userId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId AND selectedVariant IS :variant LIMIT 1")
    suspend fun find(userId: Long, productId: Long, variant: String?): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity): Long

    @Update
    suspend fun update(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clear(userId: Long)
}

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertLines(lines: List<OrderLineEntity>)

    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY placedAtMillis DESC")
    fun observeOrders(userId: Long): Flow<List<OrderWithLines>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrder(orderId: Long): OrderWithLines?
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY id")
    fun observeByUser(userId: Long): Flow<List<WishlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE userId = :userId AND productId = :productId)")
    fun observeIsWishlisted(userId: Long, productId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun delete(userId: Long, productId: Long)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id")
    fun observeByUser(userId: Long): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id LIMIT 1")
    suspend fun getDefault(userId: Long): AddressEntity?

    @Insert
    suspend fun insert(address: AddressEntity): Long

    @Update
    suspend fun update(address: AddressEntity)

    @Delete
    suspend fun delete(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefault(userId: Long)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun markDefault(id: Long)
}

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY isDefault DESC, id")
    fun observeByUser(userId: Long): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY isDefault DESC, id LIMIT 1")
    suspend fun getDefault(userId: Long): PaymentMethodEntity?

    @Insert
    suspend fun insert(method: PaymentMethodEntity): Long

    @Update
    suspend fun update(method: PaymentMethodEntity)

    @Delete
    suspend fun delete(method: PaymentMethodEntity)

    @Query("UPDATE payment_methods SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefault(userId: Long)

    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :id")
    suspend fun markDefault(id: Long)
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches WHERE userId = :userId ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeByUser(userId: Long, limit: Int = 10): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE userId = :userId")
    suspend fun clear(userId: Long)
}
