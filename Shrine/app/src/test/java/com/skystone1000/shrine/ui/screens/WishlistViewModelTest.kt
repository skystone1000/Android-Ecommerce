package com.skystone1000.shrine.ui.screens

import app.cash.turbine.test
import com.skystone1000.shrine.testing.FakeCartRepository
import com.skystone1000.shrine.testing.FakeCatalogRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeWishlistRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import com.skystone1000.shrine.testing.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun wishlistState_showsOnlySavedProducts() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(userId = 1) }
        val wishlist = FakeWishlistRepository()
        val catalog = FakeCatalogRepository(products = TestData.products)
        wishlist.setWishlisted(1, TestData.chair.id, true)
        val vm = WishlistViewModel(wishlist, FakeCartRepository(), session, catalog)

        vm.state.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()
            assertEquals(listOf(TestData.chair.id), state.products.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun remove_takesItemOutOfTheWishlist() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(userId = 1) }
        val wishlist = FakeWishlistRepository()
        wishlist.setWishlisted(1, TestData.chair.id, true)
        val vm = WishlistViewModel(wishlist, FakeCartRepository(), session, FakeCatalogRepository(TestData.products))

        vm.remove(TestData.chair.id)
        assertTrue(wishlist.wishlist(1).first().isEmpty())
    }
}
