package com.skystone1000.shrine.ui.screens

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.skystone1000.shrine.testing.FakeCatalogRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeWishlistRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import com.skystone1000.shrine.testing.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CategoryViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun viewModel(): CategoryViewModel {
        val session = FakeSessionRepository().apply { signedIn(userId = 1) }
        return CategoryViewModel(
            catalogRepository = FakeCatalogRepository(products = TestData.products, categories = TestData.categories),
            sessionRepository = session,
            wishlistRepository = FakeWishlistRepository(),
            savedStateHandle = SavedStateHandle(mapOf("id" to "audio")),
        )
    }

    @Test
    fun loadsProductsForTheRouteCategory() = runTest(mainRule.dispatcher) {
        viewModel().state.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()
            assertEquals("Audio", state.categoryName)
            assertEquals(setOf(TestData.headphones.id, TestData.speaker.id), state.products.map { it.id }.toSet())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sortByPriceLowToHigh_ordersByAscendingPrice() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        vm.state.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()

            vm.setSort(SortOption.PriceLowHigh)
            while (state.sort != SortOption.PriceLowHigh) state = awaitItem()
            assertEquals(TestData.speaker.id, state.products.first().id) // 14900 < 29900
            cancelAndIgnoreRemainingEvents()
        }
    }
}
