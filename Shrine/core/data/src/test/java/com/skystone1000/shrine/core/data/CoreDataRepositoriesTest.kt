package com.skystone1000.shrine.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.skystone1000.shrine.core.database.CatalogSeed
import com.skystone1000.shrine.core.database.ShrineDatabase
import com.skystone1000.shrine.core.model.DeliveryOption
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CoreDataRepositoriesTest {

    private lateinit var db: ShrineDatabase
    private lateinit var catalog: CatalogRepository
    private lateinit var cart: CartRepository
    private lateinit var auth: AuthRepository
    private lateinit var order: OrderRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShrineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        catalog = DefaultCatalogRepository(db.productDao(), db.categoryDao(), db.promotionDao())
        cart = DefaultCartRepository(db.cartDao())
        auth = DefaultAuthRepository(db.userDao())
        order = DefaultOrderRepository(db.orderDao(), db.cartDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun seedingPopulatesCatalogOnce() = runTest {
        catalog.ensureSeeded()
        catalog.ensureSeeded() // idempotent
        assertEquals(CatalogSeed.products.size, catalog.products().first().size)
        assertEquals(CatalogSeed.categories.size, catalog.categories().first().size)
    }

    @Test
    fun searchMatchesByName() = runTest {
        catalog.ensureSeeded()
        val results = catalog.search("aether")
        assertEquals(1, results.size)
        assertEquals("Aether Wireless", results.first().name)
    }

    @Test
    fun registerThenLoginSucceeds_andDuplicateEmailRejected() = runTest {
        val registered = auth.register("Ava", "Ava@Shrine.com", "secret123")
        assertTrue(registered is AuthResult.Success)

        // Email is normalised, password is case-sensitive and not trimmed.
        assertTrue(auth.login("ava@shrine.com", "secret123") is AuthResult.Success)
        assertTrue(auth.login("ava@shrine.com", "wrong") is AuthResult.InvalidCredentials)
        assertTrue(auth.register("Other", "ava@shrine.com", "another") is AuthResult.EmailTaken)
    }

    @Test
    fun cartAddsIncrementsAndComputesSubtotal() = runTest {
        catalog.ensureSeeded()
        val product = catalog.products().first().first { it.name == "Aether Wireless" }

        cart.add(userId = 1, product = product, variant = "Sand")
        cart.add(userId = 1, product = product, variant = "Sand") // increments same line

        val items = cart.cart(1).first()
        assertEquals(1, items.size)
        assertEquals(2, items.first().quantity)
        assertEquals(product.priceCents * 2, cart.subtotalCents(1).first())

        cart.setQuantity(items.first().id, 0) // removes
        assertTrue(cart.cart(1).first().isEmpty())
    }

    @Test
    fun placeOrderPersistsOrderAndClearsCart() = runTest {
        catalog.ensureSeeded()
        val product = catalog.products().first().first()
        cart.add(userId = 7, product = product)

        val orderId = order.placeOrder(userId = 7, delivery = DeliveryOption.EXPRESS)
        assertNotNull(orderId)

        assertTrue(cart.cart(7).first().isEmpty())
        val orders = order.orders(7).first()
        assertEquals(1, orders.size)
        val placed = orders.first()
        assertEquals(1, placed.lines.size)
        assertEquals(product.priceCents + DeliveryOption.EXPRESS.shippingCents, placed.order.totalCents)

        // Placing with an empty cart returns null.
        assertNull(order.placeOrder(userId = 7, delivery = DeliveryOption.STANDARD))
    }
}
