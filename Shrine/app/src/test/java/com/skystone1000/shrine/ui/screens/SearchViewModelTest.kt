package com.skystone1000.shrine.ui.screens

import app.cash.turbine.test
import com.skystone1000.shrine.testing.FakeSearchRepository
import com.skystone1000.shrine.testing.FakeSessionRepository
import com.skystone1000.shrine.testing.FakeWishlistRepository
import com.skystone1000.shrine.testing.MainDispatcherRule
import com.skystone1000.shrine.testing.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the search debounce (plan_9 Phase C / item 13): a burst of keystrokes typed within the
 * debounce window collapses to a single expensive `results(...)` query for the final term, while
 * the *visible* query in the UI state still updates immediately so the text field never lags.
 *
 * Note: `runTest` auto-advances virtual time whenever the test coroutine suspends (e.g. in Turbine's
 * `awaitItem`), so the debounce fires on its own — the invariant under test is the *collapse*
 * (one query, not five), not the exact firing instant.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SearchViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun viewModel(search: FakeSearchRepository) = SearchViewModel(
        searchRepository = search,
        sessionRepository = FakeSessionRepository().apply { signedIn(userId = 1) },
        wishlistRepository = FakeWishlistRepository(),
    )

    @Test
    fun rapidKeystrokes_collapseToASingleResultsQuery() = runTest(mainRule.dispatcher) {
        val search = FakeSearchRepository(products = TestData.products)
        val vm = viewModel(search)

        vm.state.test {
            awaitItem() // initial empty state

            // Five keystrokes typed "instantly" (no virtual time advanced between them).
            vm.onQuery("a")
            vm.onQuery("ae")
            vm.onQuery("aet")
            vm.onQuery("aeth")
            vm.onQuery("aethe")

            // Drain until the results for the final term land. The visible query tracks the latest
            // keystroke immediately; results arrive after the debounce window.
            var state = awaitItem()
            while (state.query != "aethe" || state.results.isEmpty()) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // The burst collapsed to a single query — and only for the last term.
        assertEquals(1, search.resultsCallCount)
        assertEquals(listOf("aethe"), search.queries)
    }

    @Test
    fun blankQuery_neverHitsTheResultsPath() = runTest(mainRule.dispatcher) {
        val search = FakeSearchRepository(products = TestData.products)
        val vm = viewModel(search)

        vm.state.test {
            awaitItem()

            vm.onQuery("aether")
            var state = awaitItem()
            while (state.results.isEmpty()) state = awaitItem()

            vm.onQuery("") // cleared
            while (state.query != "") state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Only the non-blank term queried; clearing the field does not call results("").
        assertEquals(1, search.resultsCallCount)
        assertTrue(search.queries.none { it.isBlank() })
    }
}
