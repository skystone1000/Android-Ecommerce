---
title: Cart correctness
status: active
last_updated: 2026-06-27
scope: Make the cart display real items, compute totals safely, and run all cart DB/navigation on the correct thread and lifecycle scope.
---

# plan_2_cart — Cart correctness

## Goal

Make the cart actually work: show the items the user added (not the seed placeholder), keep the list and totals consistent, parse prices/quantities without crashing, and run cart DB work + navigation on the right thread/scope.

This is second because the cart is visibly broken (B1) and is the next core flow after login, but it touches more surface than plan_1 (fragment + adapter + DAO read paths), so it carries slightly more risk.

## Scope

**In:**
- Fix the adapter/data desync so real cart rows render (B1).
- Remove the placeholder seed item (`CartFragment.kt:26`).
- Refresh the RecyclerView after load and after mutations (`notifyDataSetChanged` or list-diff).
- Move cart navigation (clear, checkout, remove) to the main thread (B2).
- Replace `GlobalScope` in cart paths with `viewLifecycleOwner.lifecycleScope` (fragment) / `requireActivity()` lifecycle scope from the adapter (B6).
- Defensive numeric parsing for totals (B7).

**Out:**
- Quantity stepper UI / a real `quantity` column (backlog item; this plan keeps the existing "one row per add, regroup by count" model but makes it display correctly).
- Catalog/product data source (→ plan_3_catalog).
- Order persistence (backlog: order history).

## Step-by-step changes

1. **Remove the seed placeholder:** initialize `cartList` to `mutableListOf()` (`CartFragment.kt:26`).
2. **Single load path, lifecycle-scoped:** collapse the nested `CoroutineScope(Dispatchers.IO).launch { initialization() }` (`:34`) + inner `GlobalScope.launch` (`:60`) into one `viewLifecycleOwner.lifecycleScope.launch` that:
   - reads rows with `withContext(Dispatchers.IO) { database.cartItemDao().getAll() }`,
   - performs the product_id regroup/count in memory,
   - assigns the result and **updates the adapter** on the main thread.
3. **Make the adapter refreshable:** give `CartRecyclerViewAdapter` a `submit(list)` method (replace its backing list + `notifyDataSetChanged()`), or expose the list as `var` and call `adapter.notifyDataSetChanged()` after load. Update `CartFragment` to call it once data is ready.
4. **Recompute totals after load, not in a racy `onResume`:** move the count/total computation so it runs after the list is populated (e.g., in the same continuation as step 2), writing `cart_items_total_value` / `cart_items_price_value`.
5. **Safe parsing (B7):** replace `product_price.toInt()` / `product_quantity.toInt()` (`:92`) with `toIntOrNull() ?: 0` (strip a leading `$` if present) so a non-numeric string can't crash the screen.
6. **Main-thread navigation (B2):** in clear (`:102`→`:104`), checkout (`:110`→`:112`), and `CartRecyclerViewAdapter` remove (`:44`→`:46`), do the DB delete with `withContext(Dispatchers.IO)` and then `navigateTo(...)` on the main thread within a lifecycle-aware scope.
7. **Adapter scope:** `CartRecyclerViewAdapter` is not a `LifecycleOwner`; use `(context as AppCompatActivity).lifecycleScope` for its coroutine instead of `GlobalScope`.
8. **Smoke test:** add 2–3 products from the grid, open cart → see those items + correct count/total; remove one → list and total update; clear → empty; checkout → order placed.

## Files / modules touched

- `app/src/main/java/.../fragments/CartFragment.kt` (load, refresh, totals, scope, navigation).
- `app/src/main/java/.../adapters/lineargridlayout/CartRecyclerViewAdapter.kt` (`submit`/refresh, scope, main-thread nav).
- (Read-only reference) `database/CartItemDAO.kt`, `models/CartItem.kt`.
- Docs: update `FEATURES.md` §6 (accurate load/refresh behavior) and `ARCHITECTURE.md` "Known issues" (remove cart background-thread + adapter-desync items).

## Dependencies on other plans

- **Soft dependency on plan_1_login**: reuses the `lifecycle-runtime-ktx` / `fragment-ktx` dependencies and the off-main-then-navigate pattern introduced there. If plan_2 runs first, add those dependencies here instead.

## Risks and rollback

- **Risk:** changing the adapter's list handling could break row rendering. Mitigation: keep `shr_cart_item.xml` and view-holder bindings unchanged; only change how the backing list is set + notified.
- **Risk:** empty cart now renders nothing (previously always showed a placeholder). Mitigation: acceptable; an explicit empty state is a backlog item.
- **Rollback:** revert `CartFragment.kt` and `CartRecyclerViewAdapter.kt`. No schema change, so existing `cart` rows are unaffected.

## Definition of done

- Adding products then opening the cart shows exactly those products with correct per-product quantity, count, and total.
- Removing an item and clearing the cart both update the list and totals immediately.
- Checkout navigates to Order Placed; all cart navigation runs on the main thread.
- No `GlobalScope` and no background-thread `navigateTo` remain in `CartFragment`/`CartRecyclerViewAdapter`.
- A non-numeric price/quantity cannot crash the cart.
- `./gradlew assembleDebug` passes; manual cart flow verified.
- `FEATURES.md` / `ARCHITECTURE.md` updated.
