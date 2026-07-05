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
- 2026-06-27 — plan_2_cart implemented: **B1** and **B7** fixed; **B2** now fully fixed (all three call sites navigate on the main thread); **B6** still partial (cart + cart-adapter done; add-to-cart adapter and register remain).
- 2026-06-27 — plan_3_catalog implemented: **B8** fixed (DAO renamed `ProductDAO`, queries the `products` table, now used); **B11** partially fixed (hardcoded catalog removed, images render a branded placeholder, `products.json` now seeds the DB; staggered adapters + `ProductEntry` remain for plan_5).
- 2026-06-27 — plan_4_security implemented: **B3** fixed (salted PBKDF2 hashing; no plaintext stored/compared); **B9** fixed (unique `user_email` index + pre-insert guard); **B6** advanced (register now lifecycle-scoped — only the add-to-cart adapter still uses `GlobalScope`). DB schema bumped to v2 (destructive migration).
- 2026-06-27 — plan_5_cleanup implemented: **B10**, **B12**, **B13** fixed; **B11** resolved with a scope change — instead of deleting the staggered grid it was **repurposed into a feature** (Settings screen with a grid-layout toggle, see plan_6_settings). `ProductEntry` is the sole remaining unused class, intentionally retained.
- 2026-06-27 — remaining items closed: **B6** fully fixed (`ProductCardRecyclerViewAdapter` add-to-cart now uses the host activity's `lifecycleScope` instead of `GlobalScope` — **no `GlobalScope` remains anywhere**); **B11** fully resolved (the unused `network/ProductEntry.kt` was removed). **All inventory items are now ✅.**

| # | Severity | Bug | Location | Fix in | Status |
|---|----------|-----|----------|--------|--------|
| B1 | High | Cart never shows real items | `fragments/CartFragment.kt:51`, `:26`, `:61/:78/:81` | plan_2_cart | ✅ Fixed (plan_2_cart) |
| B2 | High | Fragment navigation on background threads | `fragments/LoginFragment.kt:46→:84`; `fragments/CartFragment.kt:102→:104`, `:110→:112`; `adapters/lineargridlayout/CartRecyclerViewAdapter.kt:44→:46` | plan_1_login, plan_2_cart | ✅ Fixed (plan_1_login + plan_2_cart) |
| B3 | High | Passwords stored/compared in plaintext | `models/User.kt:14`; `fragments/RegisterFragment.kt:104`; `fragments/LoginFragment.kt:72` | plan_4_security | ✅ Fixed (plan_4_security) |
| B4 | Medium | Login silently fails (no error feedback) | `fragments/LoginFragment.kt:72` (else branch commented) | plan_1_login | ✅ Fixed (plan_1_login) |
| B5 | Medium | "Username" field actually requires the email | `fragments/LoginFragment.kt` + `res/layout/shr_login_fragment.xml`; query at `database/UserDAO.kt:13` | plan_1_login | ✅ Fixed (plan_1_login) |
| B6 | Medium | `GlobalScope` coroutines are not lifecycle-aware | (all sites migrated) | plan_1_login, plan_2_cart, plan_4_security | ✅ Fixed — no `GlobalScope` remains |
| B7 | Medium | `NumberFormatException` risk in cart totals | `fragments/CartFragment.kt:92` (`product_price.toInt()`, `product_quantity.toInt()`) | plan_2_cart | ✅ Fixed (plan_2_cart) |
| B8 | Low* | `PrductDAO` queries the wrong table | `database/PrductDAO.kt:17` (`SELECT * FROM cart`), `:20` (`DELETE FROM cart`) | plan_3_catalog | ✅ Fixed (plan_3_catalog) |
| B9 | Medium | No duplicate-email guard at registration | `database/UserDAO.kt` (no uniqueness); `fragments/RegisterFragment.kt:104` | plan_4_security | ✅ Fixed (plan_4_security) |
| B10 | Low | Deprecated `defaultDisplay.getMetrics` | `NavigationIconClickListener.kt:27` | plan_5_cleanup | ✅ Fixed (plan_5_cleanup) |
| B11 | Low | Dead code + stale image URLs | `adapters/staggeredgridlayout/*`, `network/ProductEntry.kt`, `res/raw/products.json`; hardcoded list in `fragments/ProductGridFragment.kt` | plan_3_catalog, plan_5_cleanup | ✅ Fixed — catalog/images fixed, staggered repurposed, `ProductEntry` removed |
| B12 | Low | Misplaced `@RequiresApi` on an `if` statement | `fragments/OrderPlacedFragment.kt:45` | plan_5_cleanup | ✅ Fixed (plan_5_cleanup) |
| B13 | Low | `targetSdk 33` lags `compileSdk 34` | `app/build.gradle:7,11` | plan_5_cleanup | ✅ Fixed (plan_5_cleanup) |

\* B8 is Low **today** because the DAO is dead code, but it is a latent High if ever wired up.

---

## Details

### B1 — Cart never displays real items (High) — ✅ Fixed (plan_2_cart)
`CartFragment` seeded `cartList` with a placeholder `CartItem(0,0,"Test","123","","10")` and bound the adapter to that reference, then reassigned `cartList` on a background coroutine with **no `notifyDataSetChanged()`** — so the cart always rendered the single "Test" row regardless of what was added, and the `onResume` totals could disagree with the list.

> **2026-06-27 (plan_2_cart):** removed the placeholder seed; the adapter now exposes `submit(list)` (replaces backing list + `notifyDataSetChanged()`); a single `viewLifecycleOwner.lifecycleScope.launch` loads from the DB in `withContext(Dispatchers.IO)`, regroups by `product_id`, then updates the adapter **and** totals together on the main thread. Verified on-device: adding iPhone 9 ×2 + iPhone X ×1 shows the real rows with quantities 2/1, count 2, total 524 $.

### B2 — Fragment transactions from background threads (High) — ✅ Fixed
`navigateTo(...)` ultimately calls `supportFragmentManager.beginTransaction()...commit()`, which must run on the main thread. It was invoked from inside `Dispatchers.IO` / `GlobalScope` coroutines in login, cart clear/checkout, and item removal. This is why login navigation was observed to be unreliable.

> **2026-06-27 (plan_1_login):** login fixed — DB lookup in `withContext(Dispatchers.IO)` inside `viewLifecycleOwner.lifecycleScope.launch`, navigation on the main thread.
> **2026-06-27 (plan_2_cart):** the remaining two sites are fixed — cart clear/checkout use `viewLifecycleOwner.lifecycleScope` (DB delete off-main, navigate on main); `CartRecyclerViewAdapter` removal uses `(context as AppCompatActivity).lifecycleScope` the same way. All `navigateTo` calls now run on the main thread. Verified on-device (remove, clear, checkout all navigate correctly with no crash).

### B3 — Plaintext passwords (High, security) — ✅ Fixed (plan_4_security)
`User.user_pass` held the raw password; registration inserted it as-is and login compared raw strings. Anyone with DB access (rooted device, backup) could read credentials.

> **2026-06-27 (plan_4_security):** `User` now stores `user_pass_hash` + `user_pass_salt` (no plaintext column). Registration hashes with salted PBKDF2 ([`auth/PasswordHasher`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/auth/PasswordHasher.kt), `PBKDF2WithHmacSHA1`, 100k iterations, random 16-byte salt); login recomputes the hash and compares. Verified on-device: the `users` row holds Base64 hash+salt only (e.g. `user_pass_hash=pqD/NU…`), no plaintext. This is on-device hardening for a local demo, not server-side auth.

### B4 — Silent login failure (Medium) — ✅ Fixed (plan_1_login)
On a wrong email/password, the old `userLogin()` did nothing — the `else` branch was commented out. The user got no toast, no error, no state change.

> **2026-06-27 (plan_1_login):** failed authentication (wrong password *or* unknown email) now sets `passwordTextInput.error = getString(R.string.shr_error_invalid_credentials)` ("Incorrect email or password."). Verified on-device for both paths.

### B5 — "Username" really means email (Medium) — ✅ Fixed (plan_1_login)
The login layout hint said "Username", but the auth path calls `UserDAO.getLogin(email)` which matches `WHERE user_email = :email` (`UserDAO.kt:13`). Anyone entering an actual username failed to log in.

> **2026-06-27 (plan_1_login):** the field hint is now `@string/label_email` ("Email") and its `inputType` is `textEmailAddress`, matching the query. (Username-based lookup was not added; login is by email.)

### B6 — `GlobalScope` everywhere (Medium) — 🟡 Partial
DB writes/reads for register, login, add-to-cart, remove-item, clear, and checkout ran on `GlobalScope` / ad-hoc `CoroutineScope(Dispatchers.IO)`, which ignore fragment/view lifecycle. Coroutines can outlive the view and touch destroyed UI, and uncaught exceptions crash the process. Fix: use `viewLifecycleOwner.lifecycleScope` (fragments) / a scoped approach for adapters.

> **2026-06-27 (plan_1_login):** login now uses `viewLifecycleOwner.lifecycleScope`.
> **2026-06-27 (plan_2_cart):** `CartFragment` (load/clear/checkout) and `CartRecyclerViewAdapter` (remove) now use lifecycle scopes instead of `GlobalScope`.
> **2026-06-27 (plan_4_security):** `RegisterFragment` register insert migrated to `viewLifecycleOwner.lifecycleScope`.
> **2026-06-27 (final):** `ProductCardRecyclerViewAdapter` add-to-cart now uses `(context as AppCompatActivity).lifecycleScope` + `withContext(Dispatchers.IO)`. **No `GlobalScope` remains anywhere in the codebase** (grep-verified). Verified on-device: one tap = exactly one cart insert, no crash.

### B7 — Cart total parsing can crash (Medium) — ✅ Fixed (plan_2_cart)
`CartFragment` did `cartItem.product_price.toInt() * cartItem.product_quantity.toInt()` on free-form `String`s; any price containing `$`, decimals, or commas would throw `NumberFormatException` and crash the cart.

> **2026-06-27 (plan_2_cart):** totals now parse with `removePrefix("$").trim().toIntOrNull() ?: 0` for price and `toIntOrNull() ?: 0` for quantity, so malformed data yields 0 instead of a crash.

### B8 — `PrductDAO` targets the `cart` table (Low today) — ✅ Fixed (plan_3_catalog)
`PrductDAO.getAll()` returned `List<Product>` from `SELECT * FROM cart`, and `clearCart()` did `DELETE FROM cart` — both wrong for a product DAO. Harmless only because nothing called them.

> **2026-06-27 (plan_3_catalog):** renamed `PrductDAO` → `ProductDAO` (file `database/ProductDAO.kt`); replaced the bogus queries with `getAll()`/`count()` on the `products` table and an `insertAll(...)`. It is now the catalog's data source (used by `ProductGridFragment`).

### B9 — Duplicate registrations (Medium) — ✅ Fixed (plan_4_security)
Nothing prevented two `User` rows with the same `user_email`; `getLogin` returns a single row, so behavior with duplicates was undefined.

> **2026-06-27 (plan_4_security):** added a unique index on `user_email` (`@Entity(indices=[Index(value=["user_email"], unique=true)])`) and a pre-insert check (`getLogin(email) != null`) that shows "An account with this email already exists." on the email field and aborts. Verified on-device: re-registering `bob@x.com` is rejected and the user count stays at 1.

### B10–B13 — Low — ✅ Fixed (plan_5_cleanup)
- **B10** ✅: replaced the deprecated `Activity.windowManager.defaultDisplay.getMetrics()` in `NavigationIconClickListener` with `context.resources.displayMetrics.heightPixels` (and dropped the now-unused `Activity`/`DisplayMetrics` imports). Verified on-device: the backdrop reveal still animates.
- **B11** ✅ Fixed (plan_3_catalog + plan_5_cleanup + final): hardcoded catalog gone (Room-seeded), images show a branded `shr_logo` placeholder (all remote product URLs return 404/000). The staggered grid was repurposed into a real feature (see [plan_6_settings](plan_6_settings.md)). The last unused class, `network/ProductEntry.kt`, has now been **removed** (`products.json` stays — it is used by `ProductSeed`). No dead code paths remain.
- **B12** ✅: removed the misplaced `@RequiresApi(LOLLIPOP)` in `OrderPlacedFragment` (the `is AnimatedVectorDrawable` runtime check is the real guard) and its unused `Build`/`RequiresApi` imports.
- **B13** ✅: `targetSdkVersion` raised to 34 to match `compileSdk`; full flow re-verified on-device.

See [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md) for new work and the `plan_*` files for fixes.
