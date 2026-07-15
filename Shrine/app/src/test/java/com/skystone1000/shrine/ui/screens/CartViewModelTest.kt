package com.skystone1000.shrine.ui.screens

import app.cash.turbine.test
import com.skystone1000.shrine.testing.FakeCartRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import com.skystone1000.shrine.testing.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun cartState_reflectsItemsAndSubtotalForTheSignedInUser() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(userId = 1) }
        val cart = FakeCartRepository()
        cart.add(userId = 1, product = TestData.headphones, variant = "Sand", quantity = 2)
        val vm = CartViewModel(cart, session)

        vm.state.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()
            assertEquals(1, state.items.size)
            assertEquals(2, state.items.first().quantity)
            assertEquals(TestData.headphones.priceCents * 2, state.subtotalCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setQuantityToZero_removesTheLine() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(userId = 1) }
        val cart = FakeCartRepository()
        cart.add(userId = 1, product = TestData.headphones)
        val vm = CartViewModel(cart, session)

        vm.state.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()
            val itemId = state.items.first().id

            vm.setQuantity(itemId, 0)
            assertEquals(0, awaitItem().items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
