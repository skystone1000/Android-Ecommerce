package com.skystone1000.shrine.testing

import com.skystone1000.shrine.core.data.AddressRepository
import com.skystone1000.shrine.core.data.AuthRepository
import com.skystone1000.shrine.core.data.AuthResult
import com.skystone1000.shrine.core.data.CartRepository
import com.skystone1000.shrine.core.data.CatalogRepository
import com.skystone1000.shrine.core.data.OrderRepository
import com.skystone1000.shrine.core.data.PaymentRepository
import com.skystone1000.shrine.core.data.SearchRepository
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.SessionState
import com.skystone1000.shrine.core.data.SettingsRepository
import com.skystone1000.shrine.core.data.SettingsState
import com.skystone1000.shrine.core.data.WishlistRepository
import com.skystone1000.shrine.core.data.subtotalCents
import com.skystone1000.shrine.core.model.AddressEntity
import com.skystone1000.shrine.core.model.CartItemEntity
import com.skystone1000.shrine.core.model.CategoryEntity
import com.skystone1000.shrine.core.model.OrderWithLines
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import com.skystone1000.shrine.core.model.ProductEntity
import com.skystone1000.shrine.core.model.RecentSearchEntity
import com.skystone1000.shrine.core.model.ThemePreference
import com.skystone1000.shrine.core.model.UserEntity
import com.skystone1000.shrine.core.model.WishlistItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [SessionRepository]; `signedIn`/`guest` helpers seed a starting state in tests. */
class FakeSessionRepository(initial: SessionState? = null) : SessionRepository {
    private val _session = MutableStateFlow(initial)
    override val session: Flow<SessionState?> = _session.asStateFlow()
    override suspend fun currentUserId(): Long? = _session.value?.userId?.takeIf { it != -1L }
    override suspend fun signIn(userId: Long, name: String, email: String, phone: String?) {
        _session.value = SessionState(userId, name, email, phone, isGuest = false)
    }
    override suspend fun continueAsGuest() {
        _session.value = SessionState(-1L, "", "", null, isGuest = true)
    }
    override suspend fun signOut() { _session.value = null }

    fun signedIn(userId: Long = 1L, name: String = "Ava", email: String = "ava@shrine.com") {
        _session.value = SessionState(userId, name, email, null, isGuest = false)
    }
    fun guest() { _session.value = SessionState(-1L, "", "", null, isGuest = true) }
}

/** In-memory [AuthRepository] with a real duplicate-email guard and case-insensitive email. */
class FakeAuthRepository : AuthRepository {
    private data class Account(var user: UserEntity, val password: String)
    private val accounts = mutableListOf<Account>()
    private var nextId = 1L

    override suspend fun register(name: String, email: String, password: String, phone: String?): AuthResult {
        val normalised = email.trim().lowercase()
        if (accounts.any { it.user.email == normalised }) return AuthResult.EmailTaken
        val user = UserEntity(id = nextId++, name = name.trim(), email = normalised, phone = phone, passwordHash = "", passwordSalt = "")
        accounts += Account(user, password)
        return AuthResult.Success(user.id)
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val account = accounts.firstOrNull { it.user.email == email.trim().lowercase() }
            ?: return AuthResult.InvalidCredentials
        return if (account.password == password) AuthResult.Success(account.user.id) else AuthResult.InvalidCredentials
    }

    override suspend fun getUser(userId: Long): UserEntity? = accounts.firstOrNull { it.user.id == userId }?.user

    override suspend fun updateProfile(
        userId: Long,
        name: String,
        email: String,
        phone: String?,
        dateOfBirthMillis: Long?,
        avatarUri: String?,
    ) {
        val account = accounts.firstOrNull { it.user.id == userId } ?: return
        account.user = account.user.copy(
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone,
            dateOfBirthMillis = dateOfBirthMillis,
            avatarUri = avatarUri,
        )
    }
}

/** In-memory cart shared across users; mirrors the real upsert-by-(user,product,variant) behaviour. */
class FakeCartRepository : CartRepository {
    private val items = MutableStateFlow<List<CartItemEntity>>(emptyList())
    private var nextId = 1L

    override fun cart(userId: Long): Flow<List<CartItemEntity>> = items.map { all -> all.filter { it.userId == userId } }
    override fun subtotalCents(userId: Long): Flow<Int> = cart(userId).map { it.subtotalCents() }
    override fun itemCount(userId: Long): Flow<Int> = cart(userId).map { line -> line.sumOf { it.quantity } }

    override suspend fun add(userId: Long, product: ProductEntity, variant: String?, quantity: Int) {
        val existing = items.value.firstOrNull { it.userId == userId && it.productId == product.id && it.selectedVariant == variant }
        items.value = if (existing != null) {
            items.value.map { if (it.id == existing.id) it.copy(quantity = it.quantity + quantity) else it }
        } else {
            items.value + CartItemEntity(
                id = nextId++, userId = userId, productId = product.id, name = product.name,
                imageUrl = product.imageUrls.firstOrNull(), selectedVariant = variant,
                priceCents = product.priceCents, quantity = quantity,
            )
        }
    }

    override suspend fun setQuantity(itemId: Long, quantity: Int) {
        items.value = if (quantity <= 0) items.value.filterNot { it.id == itemId }
        else items.value.map { if (it.id == itemId) it.copy(quantity = quantity) else it }
    }

    override suspend fun remove(itemId: Long) { items.value = items.value.filterNot { it.id == itemId } }
    override suspend fun clear(userId: Long) { items.value = items.value.filterNot { it.userId == userId } }
}

/** In-memory per-user wishlist. */
class FakeWishlistRepository : WishlistRepository {
    private val items = MutableStateFlow<List<WishlistItemEntity>>(emptyList())
    private var nextId = 1L

    override fun wishlist(userId: Long): Flow<List<WishlistItemEntity>> = items.map { all -> all.filter { it.userId == userId } }
    override fun isWishlisted(userId: Long, productId: Long): Flow<Boolean> =
        items.map { all -> all.any { it.userId == userId && it.productId == productId } }

    override suspend fun setWishlisted(userId: Long, productId: Long, wishlisted: Boolean) {
        val present = items.value.any { it.userId == userId && it.productId == productId }
        items.value = when {
            wishlisted && !present -> items.value + WishlistItemEntity(id = nextId++, userId = userId, productId = productId)
            !wishlisted -> items.value.filterNot { it.userId == userId && it.productId == productId }
            else -> items.value
        }
    }
}

/** Catalog seeded from constructor lists; search is a case-insensitive name contains. */
class FakeCatalogRepository(
    products: List<ProductEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
) : CatalogRepository {
    private val _products = MutableStateFlow(products)
    private val _categories = MutableStateFlow(categories)

    override suspend fun ensureSeeded() {}
    override fun products(): Flow<List<ProductEntity>> = _products.asStateFlow()
    override fun categories(): Flow<List<CategoryEntity>> = _categories.asStateFlow()
    override fun productsByCategory(categoryId: String): Flow<List<ProductEntity>> =
        _products.map { list -> list.filter { it.categoryId == categoryId } }
    override fun product(id: Long): Flow<ProductEntity?> = _products.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun getProduct(id: Long): ProductEntity? = _products.value.firstOrNull { it.id == id }
    override suspend fun search(query: String): List<ProductEntity> =
        if (query.isBlank()) emptyList() else _products.value.filter { it.name.contains(query.trim(), ignoreCase = true) }
    override suspend fun suggestions(query: String, limit: Int): List<ProductEntity> = search(query).take(limit)
}

/** In-memory settings. */
class FakeSettingsRepository(initial: SettingsState = SettingsState()) : SettingsRepository {
    private val _settings = MutableStateFlow(initial)
    override val settings: Flow<SettingsState> = _settings.asStateFlow()
    override suspend fun setTheme(theme: ThemePreference) { _settings.value = _settings.value.copy(theme = theme) }
    override suspend fun setLargeImagery(enabled: Boolean) { _settings.value = _settings.value.copy(largeImagery = enabled) }
    override suspend fun setOrderUpdates(enabled: Boolean) { _settings.value = _settings.value.copy(orderUpdates = enabled) }
    override suspend fun setPromotions(enabled: Boolean) { _settings.value = _settings.value.copy(promotions = enabled) }
}

/** In-memory order history (read-only for ViewModel tests). */
class FakeOrderRepository(orders: List<OrderWithLines> = emptyList()) : OrderRepository {
    private val _orders = MutableStateFlow(orders)
    override fun orders(userId: Long): Flow<List<OrderWithLines>> = _orders.map { all -> all.filter { it.order.userId == userId } }
    override suspend fun getOrder(orderId: Long): OrderWithLines? = _orders.value.firstOrNull { it.order.id == orderId }
    override suspend fun placeOrder(userId: Long, delivery: com.skystone1000.shrine.core.model.DeliveryOption): Long? = null
}

/** Empty address/payment fakes for completeness where a ViewModel needs them. */
class FakeAddressRepository : AddressRepository {
    private val items = MutableStateFlow<List<AddressEntity>>(emptyList())
    private var nextId = 1L
    override fun addresses(userId: Long): Flow<List<AddressEntity>> = items.map { all -> all.filter { it.userId == userId } }
    override suspend fun getDefault(userId: Long): AddressEntity? = items.value.firstOrNull { it.userId == userId && it.isDefault }
    override suspend fun add(address: AddressEntity): Long {
        val withId = address.copy(id = nextId++)
        items.value = items.value + withId
        return withId.id
    }
    override suspend fun update(address: AddressEntity) { items.value = items.value.map { if (it.id == address.id) address else it } }
    override suspend fun delete(address: AddressEntity) { items.value = items.value.filterNot { it.id == address.id } }
    override suspend fun setDefault(userId: Long, addressId: Long) {
        items.value = items.value.map { it.copy(isDefault = it.userId == userId && it.id == addressId) }
    }
}

class FakePaymentRepository : PaymentRepository {
    private val items = MutableStateFlow<List<PaymentMethodEntity>>(emptyList())
    private var nextId = 1L
    override fun paymentMethods(userId: Long): Flow<List<PaymentMethodEntity>> = items.map { all -> all.filter { it.userId == userId } }
    override suspend fun getDefault(userId: Long): PaymentMethodEntity? = items.value.firstOrNull { it.userId == userId && it.isDefault }
    override suspend fun add(method: PaymentMethodEntity): Long {
        val withId = method.copy(id = nextId++)
        items.value = items.value + withId
        return withId.id
    }
    override suspend fun delete(method: PaymentMethodEntity) { items.value = items.value.filterNot { it.id == method.id } }
    override suspend fun setDefault(userId: Long, methodId: Long) {
        items.value = items.value.map { it.copy(isDefault = it.userId == userId && it.id == methodId) }
    }
}

/**
 * In-memory [SearchRepository]. [resultsCallCount] / [queries] record how often (and with what)
 * the expensive `results(...)` path runs — used to assert the search debounce collapses keystrokes.
 */
class FakeSearchRepository(
    private val products: List<ProductEntity> = emptyList(),
) : SearchRepository {
    private val recent = MutableStateFlow<List<RecentSearchEntity>>(emptyList())

    var resultsCallCount = 0
        private set
    val queries = mutableListOf<String>()

    override fun recentSearches(userId: Long): Flow<List<RecentSearchEntity>> = recent.asStateFlow()
    override suspend fun recordSearch(userId: Long, query: String) {}
    override suspend fun clearRecent(userId: Long) { recent.value = emptyList() }
    override suspend fun suggestions(query: String, limit: Int): List<ProductEntity> =
        products.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(limit)

    override suspend fun results(query: String): List<ProductEntity> {
        resultsCallCount++
        queries += query
        return products.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
}

/** A couple of catalog products used across ViewModel tests. */
object TestData {
    val headphones = ProductEntity(id = 1, name = "Aether Wireless", categoryId = "audio", description = "", priceCents = 29900, rating = 4.6f)
    val speaker = ProductEntity(id = 2, name = "Pulse Speaker", categoryId = "audio", description = "", priceCents = 14900, rating = 4.2f)
    val chair = ProductEntity(id = 3, name = "Loom Chair", categoryId = "home", description = "", priceCents = 49900, rating = 4.9f)
    val products = listOf(headphones, speaker, chair)
    val categories = listOf(
        CategoryEntity(id = "audio", name = "Audio", iconKey = "headphones"),
        CategoryEntity(id = "home", name = "Home", iconKey = "chair"),
    )
}
