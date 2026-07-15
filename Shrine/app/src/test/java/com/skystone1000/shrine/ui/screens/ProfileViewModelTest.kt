package com.skystone1000.shrine.ui.screens

import app.cash.turbine.test
import com.skystone1000.shrine.core.model.DeliveryOption
import com.skystone1000.shrine.core.model.OrderEntity
import com.skystone1000.shrine.core.model.OrderStatus
import com.skystone1000.shrine.core.model.OrderWithLines
import com.skystone1000.shrine.testing.FakeOrderRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeWishlistRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import com.skystone1000.shrine.testing.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun order(userId: Long, id: Long) = OrderWithLines(
        order = OrderEntity(
            id = id, userId = userId, orderNumber = "SH-100$id", status = OrderStatus.PLACED,
            placedAtMillis = 0, subtotalCents = 1000, shippingCents = 0, totalCents = 1000,
            deliveryOption = DeliveryOption.STANDARD,
        ),
        lines = emptyList(),
    )

    @Test
    fun guestSession_isReportedAsGuest() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { guest() }
        val vm = ProfileViewModel(session, FakeOrderRepository(), FakeWishlistRepository())
        vm.state.test {
            var state = awaitItem()
            while (state.name.isEmpty()) state = awaitItem()
            assertTrue(state.isGuest)
            assertEquals("Guest", state.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signedInSession_reportsNameAndCounts() = runTest(mainRule.dispatcher) {
        val session = FakeSessionRepository().apply { signedIn(userId = 1, name = "Ava") }
        val orders = FakeOrderRepository(listOf(order(1, 1), order(1, 2)))
        val wishlist = FakeWishlistRepository().apply { setWishlisted(1, TestData.chair.id, true) }
        val vm = ProfileViewModel(session, orders, wishlist)

        vm.state.test {
            var state = awaitItem()
            while (state.name != "Ava") state = awaitItem()
            assertFalse(state.isGuest)
            assertEquals(2, state.orderCount)
            assertEquals(1, state.savedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
