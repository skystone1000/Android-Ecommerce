---
title: Bug Inventory
last_updated: 2026-06-27
scope: Prioritized, verified defects in the Shrine app with file/line references and rationale. Source for the plan_* fix sequence.
---

# Bug Inventory

Severity scale: **Critical** (data loss / cannot use core flow) · **High** (core feature broken or security hole) · **Medium** (wrong/confusing behavior, latent crash) · **Low** (cosmetic, dead code, deprecation).

Every entry was verified in source on 2026-06-27. Line numbers are relative to `Shrine/app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/` unless noted. Fixes are sequenced in the `plan_*` files (see the "Fix in" column). The **Status** column tracks resolution; line numbers reflect the code *at audit time* and may have shifted in files that were since edited.

**Changelog**
- 2026-06-27 — plan_1_login implemented: **B4** and **B5** fixed; **B2** and **B6** partially fixed (login paths only; cart/adapter/register paths remain).

| # | Severity | Bug | Location | Fix in | Status |
|---|----------|-----|----------|--------|--------|
| B1 | High | Cart never shows real items | `fragments/CartFragment.kt:51`, `:26`, `:61/:78/:81` | plan_2_cart | ⬜ Open |
| B2 | High | Fragment navigation on background threads | `fragments/LoginFragment.kt:46→:84`; `fragments/CartFragment.kt:102→:104`, `:110→:112`; `adapters/lineargridlayout/CartRecyclerViewAdapter.kt:44→:46` | plan_1_login, plan_2_cart | 🟡 Partial — login fixed; cart/adapter open |
| B3 | High | Passwords stored/compared in plaintext | `models/User.kt:14`; `fragments/RegisterFragment.kt:104`; `fragments/LoginFragment.kt:72` | plan_4_security | ⬜ Open |
| B4 | Medium | Login silently fails (no error feedback) | `fragments/LoginFragment.kt:72` (else branch commented) | plan_1_login | ✅ Fixed (plan_1_login) |
| B5 | Medium | "Username" field actually requires the email | `fragments/LoginFragment.kt` + `res/layout/shr_login_fragment.xml`; query at `database/UserDAO.kt:13` | plan_1_login | ✅ Fixed (plan_1_login) |
| B6 | Medium | `GlobalScope` coroutines are not lifecycle-aware | `fragments/RegisterFragment.kt:104`; `fragments/CartFragment.kt:60/102/110`; both adapters `:44/:46` | plan_1_login, plan_2_cart | 🟡 Partial — login fixed; register/cart/adapters open |
| B7 | Medium | `NumberFormatException` risk in cart totals | `fragments/CartFragment.kt:92` (`product_price.toInt()`, `product_quantity.toInt()`) | plan_2_cart | ⬜ Open |
| B8 | Low* | `PrductDAO` queries the wrong table | `database/PrductDAO.kt:17` (`SELECT * FROM cart`), `:20` (`DELETE FROM cart`) | plan_3_catalog / plan_5_cleanup | ⬜ Open |
| B9 | Medium | No duplicate-email guard at registration | `database/UserDAO.kt` (no uniqueness); `fragments/RegisterFragment.kt:104` | plan_4_security | ⬜ Open |
| B10 | Low | Deprecated `defaultDisplay.getMetrics` | `NavigationIconClickListener.kt:27` | plan_5_cleanup | ⬜ Open |
| B11 | Low | Dead code + stale image URLs | `adapters/staggeredgridlayout/*`, `network/ProductEntry.kt`, `res/raw/products.json`; hardcoded list in `fragments/ProductGridFragment.kt` | plan_3_catalog, plan_5_cleanup | ⬜ Open |
| B12 | Low | Misplaced `@RequiresApi` on an `if` statement | `fragments/OrderPlacedFragment.kt:45` | plan_5_cleanup | ⬜ Open |
| B13 | Low | `targetSdk 33` lags `compileSdk 34` | `app/build.gradle:7,11` | plan_5_cleanup | ⬜ Open |

\* B8 is Low **today** because the DAO is dead code, but it is a latent High if ever wired up.

---

## Details

### B1 — Cart never displays real items (High)
`CartFragment` seeds `cartList` with a placeholder `CartItem(0,0,"Test","123","","10")` (`:26`). `onCreateView` binds the adapter to that list reference (`:51`). The real load happens later in `initialization()`, which **reassigns** `cartList` to a brand-new list (`:61`, `:78`, `:81`) on a background coroutine. The adapter still points at the original placeholder list, and **no `notifyDataSetChanged()`/`notifyItem*` is ever called** (verified: zero occurrences in the module). Net effect: the cart screen always renders the single "Test" placeholder row regardless of what was added. The totals in `onResume` read the `cartList` *field*, so they may or may not reflect the reload depending on timing — an inconsistency between the list and the totals.

### B2 — Fragment transactions from background threads (High) — 🟡 Partial
`navigateTo(...)` ultimately calls `supportFragmentManager.beginTransaction()...commit()`, which must run on the main thread. It is invoked from inside `Dispatchers.IO` / `GlobalScope` coroutines in login, cart clear/checkout (`CartFragment.kt:104`, `:112`), and item removal (`CartRecyclerViewAdapter.kt:46`). This is why login navigation was observed to be unreliable. Fix: do DB work off-main, then switch to the main thread for navigation.

> **2026-06-27 (plan_1_login):** the login path is fixed — `LoginFragment` now does the DB lookup in `withContext(Dispatchers.IO)` inside `viewLifecycleOwner.lifecycleScope.launch`, then navigates on the main thread. The cart and `CartRecyclerViewAdapter` paths remain open (plan_2_cart).

### B3 — Plaintext passwords (High, security)
`User.user_pass` holds the raw password; registration inserts it as-is (`RegisterFragment.kt:104`) and login compares raw strings (`LoginFragment.kt:72`). Anyone with DB access (rooted device, backup) reads credentials. Fix: hash with a salted KDF before storage; compare hashes.

### B4 — Silent login failure (Medium) — ✅ Fixed (plan_1_login)
On a wrong email/password, the old `userLogin()` did nothing — the `else` branch was commented out. The user got no toast, no error, no state change.

> **2026-06-27 (plan_1_login):** failed authentication (wrong password *or* unknown email) now sets `passwordTextInput.error = getString(R.string.shr_error_invalid_credentials)` ("Incorrect email or password."). Verified on-device for both paths.

### B5 — "Username" really means email (Medium) — ✅ Fixed (plan_1_login)
The login layout hint said "Username", but the auth path calls `UserDAO.getLogin(email)` which matches `WHERE user_email = :email` (`UserDAO.kt:13`). Anyone entering an actual username failed to log in.

> **2026-06-27 (plan_1_login):** the field hint is now `@string/label_email` ("Email") and its `inputType` is `textEmailAddress`, matching the query. (Username-based lookup was not added; login is by email.)

### B6 — `GlobalScope` everywhere (Medium) — 🟡 Partial
DB writes/reads for register, login, add-to-cart, remove-item, clear, and checkout ran on `GlobalScope` / ad-hoc `CoroutineScope(Dispatchers.IO)`, which ignore fragment/view lifecycle. Coroutines can outlive the view and touch destroyed UI, and uncaught exceptions crash the process. Fix: use `viewLifecycleOwner.lifecycleScope` (fragments) / a scoped approach for adapters.

> **2026-06-27 (plan_1_login):** login now uses `viewLifecycleOwner.lifecycleScope`. Register (`RegisterFragment.kt:104`), cart (`CartFragment.kt`), and both adapters still use `GlobalScope` — open under plan_2_cart / plan_4_security.

### B7 — Cart total parsing can crash (Medium)
`CartFragment.kt:92` does `cartItem.product_price.toInt() * cartItem.product_quantity.toInt()`. `product_price`/`product_quantity` are free-form `String`s. It works only because the hardcoded catalog uses plain integer strings; any price containing `$`, decimals, or commas throws `NumberFormatException` and crashes the cart. Fix: store numeric types or parse defensively.

### B8 — `PrductDAO` targets the `cart` table (Low today)
`PrductDAO.getAll()` returns `List<Product>` from `SELECT * FROM cart`, and `clearCart()` does `DELETE FROM cart` (`:17`,`:20`). Both are wrong for a product DAO. Harmless only because nothing calls them. Fix: correct the queries when wiring a real catalog (plan_3) or delete the DAO (plan_5).

### B9 — Duplicate registrations (Medium)
Nothing prevents two `User` rows with the same `user_email`. `getLogin` returns a single row, so behavior with duplicates is undefined. Fix: unique index on `user_email` + pre-insert existence check with a clear error.

### B10–B13 — Low
- **B10**: `Activity.windowManager.defaultDisplay.getMetrics()` is deprecated (API 30+); use `WindowMetrics`/`Resources.displayMetrics`.
- **B11**: `StaggeredProductCardRecyclerViewAdapter`/`...ViewHolder`, `ProductEntry`/`products.json` are unreachable; the active catalog is hardcoded with image URLs that may no longer resolve (blank images).
- **B12**: `@RequiresApi(LOLLIPOP)` annotates an `if` statement in `OrderPlacedFragment` rather than a method; `minSdk 16` makes the guard meaningful but the placement is wrong.
- **B13**: `targetSdk 33` while `compileSdk 34`; align `targetSdk` to 34 after a quick behavior check.

See [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md) for new work and the `plan_*` files for fixes.
