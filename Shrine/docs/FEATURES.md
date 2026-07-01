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
- Persistence: on success, builds a `User(0, name, email, phone, organisation, password)` and inserts it via `UserDAO.insertUser` inside `GlobalScope.launch`.
- Result: shows a toast "User Registered - Please Login" and navigates back to `LoginFragment`.

**Note:** the password is stored in plaintext (`User.user_pass`).

---

## 2. Login

**What it does:** Authenticates an existing user and starts a session.

**End to end:**
- UI: [`LoginFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/LoginFragment.kt) + `res/layout/shr_login_fragment.xml`.
- The login field is labeled **"Email"** and is matched against the user's email (`UserDAO.getLogin(email)` queries `WHERE user_email = :email`).
- Validation before the DB call: email non-empty; password ≥ 8 chars (`isPasswordValid`).
- Auth: `login()` runs on `viewLifecycleOwner.lifecycleScope`; the DB lookup is done in `withContext(Dispatchers.IO)`, then the result is handled on the main thread. It compares the stored `user_pass` to the entered password.
- On success: caches `user_id`, `user_name`, `user_email`, `user_phone` into per-Activity `SharedPreferences`, then navigates to `ProductGridFragment` (on the main thread).
- On failure (wrong password or unknown email): shows the error "Incorrect email or password." (`shr_error_invalid_credentials`) on the password field; stays on the login screen.
- Entry to Register: the "Register" button navigates to `RegisterFragment`.

> Implemented by [plan_1_login](plan_1_login.md). Note: the password is still compared in plaintext — see [BUG_INVENTORY.md](BUG_INVENTORY.md) B3 (plan_4_security).

---

## 3. Product catalog (product grid)

**What it does:** Shows a scrollable 2-column grid of products with image, name, and price.

**End to end:**
- UI: [`ProductGridFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/ProductGridFragment.kt) + `res/layout/shr_product_grid_fragment.xml` (which `<include>`s `shr_backdrop.xml`).
- Data: a **hardcoded** `List<Product>` of 17 items built inline in `onCreateView` (the Room `products` table and `products.json` are not used).
- List rendering: `RecyclerView` with `GridLayoutManager(span = 2)` and [`ProductCardRecyclerViewAdapter`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/adapters/lineargridlayout/ProductCardRecyclerViewAdapter.kt) inflating `res/layout/shr_product_card.xml`.
- Images: each card's image is loaded by URL through [`ImageRequester`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/network/ImageRequester.kt) (Volley + `LruCache`). Image URLs may be stale, so images can appear blank.
- Toolbar: `ProductGridFragment` sets the support action bar to the included toolbar and inflates `res/menu/shr_toolbar_menu.xml` (cart icon).

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
- `ProductCardRecyclerViewAdapter.onBindViewHolder`: the card's `onClickListener` inserts a `CartItem(0, product_id, name, price, url, "1")` via `CartItemDAO.insertCartItem` (`GlobalScope.launch`) and shows a toast "Item: <name> Added to cart".
- Each tap inserts a new cart row (quantities are aggregated later in the cart screen, not merged on insert).

---

## 6. Cart

**What it does:** Lists cart items, shows item count and total price, and supports removing items, clearing the cart, and checkout.

**End to end:**
- UI: [`CartFragment`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/fragments/CartFragment.kt) + `res/layout/shr_cart_fragment.xml`; rows via [`CartRecyclerViewAdapter`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/adapters/lineargridlayout/CartRecyclerViewAdapter.kt) + `res/layout/shr_cart_item.xml`.
- Load + regroup: `initialization()` reads all rows with `CartItemDAO.getAll()`, then **groups duplicate `product_id`s into a `HashMap` and counts occurrences** to derive a per-product quantity.
- Totals (`onResume`): `count = cartList.size`; `total = Σ(product_price × product_quantity)`, rendered into `cart_items_total_value` / `cart_items_price_value`.
- Remove one item: a row's delete icon calls `CartItemDAO.deleteCartItem(product_id)` then reloads `CartFragment` (toast "… Removed from the cart").
- Clear cart: `cart_clear_icon` calls `CartItemDAO.clearCart()` then reloads `CartFragment`.
- Checkout: `cart_checkout` calls `CartItemDAO.clearCart()` then navigates to `OrderPlacedFragment`.
- Navigation in: the toolbar cart icon on the product grid → `MainActivity.onOptionsItemSelected` (`R.id.cart_icon`) → `gotoCart()` → `CartFragment`.

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

## Not features (defined but unreachable)

These exist in the source tree but are **not** part of any user-facing flow. Do not document them as capabilities:

- **Staggered product grid** — `adapters/staggeredgridlayout/StaggeredProductCardRecyclerViewAdapter.kt` and `StaggeredProductCardViewHolder.kt` are never instantiated.
- **Bundled JSON catalog** — `res/raw/products.json` and `network/ProductEntry.kt` (`initProductEntryList`) are referenced only inside commented-out code; nothing loads them at runtime.
- **Products table/DAO** — `PrductDAO` and the Room `products` table are declared but never written to or read from in the active flow. (`PrductDAO`'s queries also target the `cart` table.)
- **Search / filter** — toolbar search/filter strings and drawables exist, but the menu items are commented out in `shr_toolbar_menu.xml`.

See [ARCHITECTURE.md](ARCHITECTURE.md) for design context and [CODEBASE.md](CODEBASE.md) for the file map.
