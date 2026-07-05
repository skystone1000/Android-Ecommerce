---
title: Features
last_updated: 2026-06-27
scope: Every user-facing feature, what it does, and the end-to-end modules that implement it.
---

# Features

This documents the **actually wired-up** user-facing behavior. Each feature lists the modules involved end to end. Dead/unreachable code is called out at the bottom so it is not mistaken for a feature.

All paths are relative to `Shrine/`.

---

## 1. Registration (sign up)

**What it does:** Creates a local user account from name, email, phone, organisation, password, and confirm-password.

**End to end:**
- UI: [`RegisterFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/RegisterFragment.kt) + layout `res/layout/shr_register_fragment.xml`.
- Validation (`check()` + `isPasswordValid()`):
  - All six `TextInputLayout` fields must be non-empty (else field error "Field must not be empty.").
  - Password and Confirm Password must match (else "Passwords Should Match" + toast).
  - Password must be ≥ 8 characters (string `shr_error_password`).
- Duplicate guard: before inserting, `userRegister()` checks `UserDAO.getLogin(email)`; if a user already exists it shows "An account with this email already exists." on the email field and aborts (the `users` table also has a unique index on `user_email`).
- Persistence: on success (`viewLifecycleOwner.lifecycleScope`), a random salt is generated and the password is hashed with salted PBKDF2 ([`PasswordHasher`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/auth/PasswordHasher.kt)); a `User(0, name, email, phone, organisation, hash, salt)` is inserted via `UserDAO.insertUser` off the main thread.
- Result: shows a toast "User Registered - Please Login" and navigates back to `LoginFragment`.

**Note:** passwords are stored as salted PBKDF2 hashes (`user_pass_hash` + `user_pass_salt`), never plaintext. See [plan_4_security](plan_4_security.md).

---

## 2. Login

**What it does:** Authenticates an existing user and starts a session.

**End to end:**
- UI: [`LoginFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/LoginFragment.kt) + `res/layout/shr_login_fragment.xml`.
- The login field is labeled **"Email"** and is matched against the user's email (`UserDAO.getLogin(email)` queries `WHERE user_email = :email`).
- Validation before the DB call: email non-empty; password ≥ 8 chars (`isPasswordValid`).
- Auth: `login()` runs on `viewLifecycleOwner.lifecycleScope`; the DB lookup **and** password verification are done in `withContext(Dispatchers.IO)`, then the result is handled on the main thread. It recomputes the salted PBKDF2 hash of the entered password and compares it to the stored `user_pass_hash`.
- On success: caches `user_id`, `user_name`, `user_email`, `user_phone` into per-Activity `SharedPreferences`, then navigates to `ProductGridFragment` (on the main thread).
- On failure (wrong password or unknown email): shows the error "Incorrect email or password." (`shr_error_invalid_credentials`) on the password field; stays on the login screen.
- Entry to Register: the "Register" button navigates to `RegisterFragment`.

> Implemented by [plan_1_login](plan_1_login.md); hash-based verification added by [plan_4_security](plan_4_security.md).

---

## 3. Product catalog (product grid)

**What it does:** Shows a scrollable 2-column grid of products with image, name, and price.

**End to end:**
- UI: [`ProductGridFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/ProductGridFragment.kt) + `res/layout/shr_product_grid_fragment.xml` (which `<include>`s `shr_backdrop.xml`).
- Data: loaded from the Room **`products` table** via `ProductDAO.getAll()`. On first run the table is empty, so `loadCatalog()` seeds it from the bundled `res/raw/products.json` ([`ProductSeed.read`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/database/ProductSeed.kt), Gson) using `ProductDAO.insertAll(...)`. The load runs on `viewLifecycleOwner.lifecycleScope` (`withContext(Dispatchers.IO)`), then publishes to the adapter via `adapter.submit(...)` on the main thread.
- List rendering: `RecyclerView` with `GridLayoutManager(span = 2)` and [`ProductCardRecyclerViewAdapter`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/adapters/lineargridlayout/ProductCardRecyclerViewAdapter.kt) inflating `res/layout/shr_product_card.xml`.
- Images: each card attempts to load its `product_url` via [`ImageRequester`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/network/ImageRequester.kt) (Volley + `LruCache`), with the `shr_logo` drawable set as the `NetworkImageView` default/error image so cards always show a branded placeholder (the seed data ships empty URLs, since no working product image host is available).
- Toolbar: `ProductGridFragment` sets the support action bar to the included toolbar and inflates `res/menu/shr_toolbar_menu.xml` (cart icon).

> Made data-driven by [plan_3_catalog](plan_3_catalog.md): the catalog was previously a hardcoded inline list with dead image URLs (B11) and `PrductDAO` queried the wrong table (B8).

---

## 4. Backdrop reveal (toolbar navigation icon)

**What it does:** Tapping the toolbar navigation icon slides the product grid sheet down to reveal a backdrop menu, and swaps the icon between "menu" and "close". Tapping again slides it back.

**End to end:**
- [`NavigationIconClickListener`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/NavigationIconClickListener.kt) animates the sheet's `translationY` (duration 500ms) using `R.dimen.shr_product_grid_reveal_height`.
- Wired in `ProductGridFragment.onCreateView` via `appBar.setNavigationOnClickListener(...)`, with open/close icons `shr_branded_menu` / `shr_close_menu`.

---

## 5. Add to cart

**What it does:** Tapping a product card adds that product to the cart.

**End to end:**
- `ProductCardRecyclerViewAdapter.onBindViewHolder` (and the staggered adapter): the card's `onClickListener` inserts a `CartItem(0, product_id, name, price, url, "1")` via `CartItemDAO.insertCartItem` off the main thread on the host activity's `lifecycleScope`, and shows a toast "Item: <name> Added to cart".
- Each tap inserts a new cart row (quantities are aggregated later in the cart screen, not merged on insert).

---

## 6. Cart

**What it does:** Lists cart items, shows item count and total price, and supports removing items, clearing the cart, and checkout.

**End to end:**
- UI: [`CartFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/CartFragment.kt) + `res/layout/shr_cart_fragment.xml`; rows via [`CartRecyclerViewAdapter`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/adapters/lineargridlayout/CartRecyclerViewAdapter.kt) + `res/layout/shr_cart_item.xml`.
- Load + regroup: `loadCart()` runs on `viewLifecycleOwner.lifecycleScope`; it reads all rows with `CartItemDAO.getAll()` in `withContext(Dispatchers.IO)`, then `regroupByProduct()` collapses duplicate `product_id`s into one entry whose quantity is the row count. The result is pushed to the adapter via `adapter.submit(list)` (which calls `notifyDataSetChanged()`) on the main thread.
- Totals: computed in `updateTotals()` right after the list loads (not in `onResume`): `count = items.size` (distinct product lines); `total = Σ(price × quantity)` with defensive parsing (`removePrefix("$").trim().toIntOrNull() ?: 0`). Rendered into `cart_items_total_value` / `cart_items_price_value`.
- Remove one item: a row's delete icon calls `CartItemDAO.deleteCartItem(product_id)` off-main, then re-opens `CartFragment` on the main thread (toast "… Removed from the cart"). Note: this removes *all* rows for that product.
- Clear cart: `cart_clear_icon` calls `CartItemDAO.clearCart()` off-main, then reloads `CartFragment` on the main thread.
- Checkout: `cart_checkout` calls `CartItemDAO.clearCart()` off-main, then navigates to `OrderPlacedFragment` on the main thread.
- Navigation in: the toolbar cart icon on the product grid → `MainActivity.onOptionsItemSelected` (`R.id.cart_icon`) → `gotoCart()` → `CartFragment`.

> Corrected by [plan_2_cart](plan_2_cart.md): the cart previously showed only a hardcoded placeholder row (B1). It now displays the real cart contents, and all cart DB work/navigation is lifecycle-scoped and main-thread-safe.

---

## 7. Order placed (confirmation)

**What it does:** Shows an animated confirmation after checkout and offers to continue shopping.

**End to end:**
- [`OrderPlacedFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/OrderPlacedFragment.kt) + `res/layout/shr_order_placed_fragment.xml`.
- Plays the animated vector drawable in the `done` `ImageView` (`res/drawable/avd_done.xml`) in `onResume`.
- "Continue shopping" navigates back to `ProductGridFragment`.

---

## 8. Profile & sign out

**What it does:** Shows the logged-in user's name, email, and phone, and lets them sign out.

**End to end:**
- [`ProfileFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/ProfileFragment.kt) + `res/layout/shr_profile_fragment.xml`.
- Reads `user_name` / `user_email` / `user_phone` from the per-Activity `SharedPreferences` populated at login.
- Reached from `ProductGridFragment` via the `grid_profile_button` (in the backdrop) → `navigateTo(ProfileFragment())`.
- Sign out (`button_signOut`): clears `SharedPreferences`, then restarts `MainActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` (wipes the back stack and returns to `LoginFragment`).

---

## 9. Settings — product grid layout

**What it does:** Lets the user switch the product grid between the regular vertical grid and a **horizontally-scrolling, two-row staggered carousel**.

**End to end:**
- UI: [`SettingsFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/SettingsFragment.kt) + `res/layout/shr_settings_fragment.xml` (a `SwitchMaterial`).
- Entry point: the gear icon in the product-grid toolbar (`R.id.settings_icon` in `shr_toolbar_menu.xml`) → `MainActivity.onOptionsItemSelected` → `navigateTo(SettingsFragment(), true)`.
- Persistence: the toggle is stored in a named `SharedPreferences` file `shrine_settings` (key `staggered_grid`), so it survives sign-out (which only clears the per-Activity session prefs).
- Effect: [`ProductGridFragment.renderGrid()`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/ProductGridFragment.kt) (called from `onResume`) reads the preference and configures either a vertical `GridLayoutManager(2)` + `ProductCardRecyclerViewAdapter`, or a **horizontal** `StaggeredGridLayoutManager(2, HORIZONTAL)` + [`StaggeredProductCardRecyclerViewAdapter`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/adapters/staggeredgridlayout/StaggeredProductCardRecyclerViewAdapter.kt). For the staggered mode the RecyclerView height is bounded (`shr_staggered_recycler_height`) so it scrolls horizontally as a 2-row carousel; the `shr_staggered_product_card_{first,second,third}` layouts give cards varied widths for the staggered effect. Add-to-cart works identically in both layouts.

> Added by [plan_6_settings](plan_6_settings.md), which repurposed the previously-dead staggered-grid adapter.

---

## Not features (defined but unreachable)

These exist in the source tree but are **not** part of any user-facing flow. Do not document them as capabilities:

- **Search / filter** — `shr_search`/`shr_filter` drawables and strings exist, but there is no toolbar menu item or handler for them.

> Note: all former dead code is now either active or removed — the Room `products` table/`ProductDAO`/`products.json` (plan_3_catalog) and the staggered product grid (plan_6_settings) are active, and the unused `ProductEntry` class was deleted.

See [ARCHITECTURE.md](ARCHITECTURE.md) for design context and [CODEBASE.md](CODEBASE.md) for the file map.
