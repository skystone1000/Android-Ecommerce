package com.skystone1000.shrine.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.skystone1000.shrine.core.database.ShrineDatabase
import com.skystone1000.shrine.core.model.AddressEntity
import com.skystone1000.shrine.core.model.PaymentMethodEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Wishlist / Address / Payment repositories over an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MoreRepositoriesTest {

    private lateinit var db: ShrineDatabase
    private lateinit var wishlist: WishlistRepository
    private lateinit var address: AddressRepository
    private lateinit var payment: PaymentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShrineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wishlist = DefaultWishlistRepository(db.wishlistDao())
        address = DefaultAddressRepository(db.addressDao())
        payment = DefaultPaymentRepository(db.paymentMethodDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun wishlistTogglesOnAndOff() = runTest {
        assertFalse(wishlist.isWishlisted(1, 42).first())

        wishlist.setWishlisted(1, 42, true)
        assertTrue(wishlist.isWishlisted(1, 42).first())
        assertEquals(listOf(42L), wishlist.wishlist(1).first().map { it.productId })

        // Idempotent + per-user scoped: a different user is unaffected.
        wishlist.setWishlisted(1, 42, true)
        assertEquals(1, wishlist.wishlist(1).first().size)
        assertTrue(wishlist.wishlist(2).first().isEmpty())

        wishlist.setWishlisted(1, 42, false)
        assertFalse(wishlist.isWishlisted(1, 42).first())
    }

    @Test
    fun addingADefaultAddressClearsThepreviousDefault() = runTest {
        val first = address.add(AddressEntity(userId = 1, fullName = "Ava", line1 = "1 St", city = "NYC", state = "NY", zip = "10001", isDefault = true))
        address.add(AddressEntity(userId = 1, fullName = "Ava", line1 = "2 Ave", city = "NYC", state = "NY", zip = "10002", isDefault = true))

        val all = address.addresses(1).first()
        assertEquals(2, all.size)
        assertEquals(1, all.count { it.isDefault })
        assertFalse(all.first { it.id == first }.isDefault)
        assertEquals(address.getDefault(1)!!.line1, "2 Ave")
    }

    @Test
    fun paymentMethodsArePerUserWithASingleDefault() = runTest {
        payment.add(PaymentMethodEntity(userId = 5, brand = "Visa", last4 = "4291", expiry = "04/27", isDefault = true))
        val amex = payment.add(PaymentMethodEntity(userId = 5, brand = "Amex", last4 = "1001", expiry = "08/26"))
        payment.setDefault(5, amex)

        val methods = payment.paymentMethods(5).first()
        assertEquals(2, methods.size)
        assertEquals(1, methods.count { it.isDefault })
        assertEquals("Amex", payment.getDefault(5)!!.brand)
        assertTrue(payment.paymentMethods(99).first().isEmpty())
    }
}
