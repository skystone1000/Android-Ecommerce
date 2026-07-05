---
title: App-wide Audit & Fixes (B14–B24)
status: completed
last_updated: 2026-06-28
scope: Whole-app audit after plan_1–plan_6 plus the sequenced fixes. Catalogues defects B14–B24 (grid-empty, per-user cart, DB singleton, password trim, backup, totals, cart efficiency, validation/polish), answers the four audit questions, and records the implemented fixes and on-emulator verification.
---

# Plan 7 — App-wide Audit & Fixes

This document is both the **audit** (findings + the four audit questions) and the **fix plan** for the second review pass. It re-reviews the code after `plan_1`–`plan_6` closed `B1`–`B13`, and captures defects that were introduced, missed, or newly understood. New IDs continue the [BUG_INVENTORY.md](BUG_INVENTORY.md) space at **B14**.

**Status: all code fixes B14–B23 are implemented, `./gradlew assembleDebug` is green, and every observable fix was verified on the emulator (see below). B24 is left as noted.** The only outstanding follow-up is the automated test suite (no scaffolding existed; tests listed in Q4).

> **Deviation from the original plan:** Step 7 fixes B15 by **scoping** the cart with a `cart.user_id` column (every query filtered by user) rather than clearing the cart on logout — this prevents cross-account visibility *and* lets each user keep their own cart across sessions.

Severity scale matches [BUG_INVENTORY.md](BUG_INVENTORY.md): **Critical** · **High** · **Medium** · **Low**. Paths are relative to `Shrine/app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/` unless noted.

---

## Findings (all resolved)

| # | Sev | Bug | Location | Status |
|---|-----|-----|----------|--------|
| B14 | High | Product grid comes back **empty** after returning from Cart/Settings (stale `staggeredMode` cache survives view destruction, so `renderGrid()` early-returns and never sets the adapter) | `fragments/ProductGridFragment.kt` | ✅ Fixed (`onDestroyView` resets the cache) — verified |
| B15 | High | **Cart was global, not per-user.** The `cart` table had no `user_id`; login/sign-out never scoped it, so user B saw user A's cart on a shared device | `models/CartItem.kt`, `database/CartItemDAO.kt`, adapters, `CartFragment.kt` | ✅ Fixed (scoped by `user_id`) — verified |
| B16 | Medium | **Broken double-checked locking** in the DB singleton: no re-check of `INSTANCE` inside `synchronized`, so two racing threads could each build a `ShrineDatabase` and overwrite the instance | `database/ShrineDatabase.kt` | ✅ Fixed (idiomatic DCL) |
| B17 | Medium | **Password trim mismatch:** register hashed `password.trim()`, login hashed the raw string. A password with leading/trailing spaces registered but could never log in | `RegisterFragment.kt` vs `LoginFragment.kt` | ✅ Fixed (register no longer trims) — verified |
| B18 | Medium | **`allowBackup="true"`** let `adb backup`/cloud backup exfiltrate the Room DB (hashes + salts, profile) and session prefs | `app/src/main/AndroidManifest.xml` | ✅ Fixed (`allowBackup="false"`) — verified |
| B19 | Medium | **Decimal prices silently became 0** in the cart total: `"12.99".toIntOrNull()` is `null` → `0`. Hidden only because seed prices are integer strings | `fragments/CartFragment.kt` | ✅ Fixed (parse as `Double`) — verified |
| B20 | Low | **Inefficient cart writes:** each tap inserted a new row (unbounded growth); "remove" deleted *all* rows for a product then rebuilt the whole `CartFragment`. No real quantity column | `adapters/.../ProductCardRecyclerViewAdapter.kt`, `adapters/.../CartRecyclerViewAdapter.kt` | ✅ Fixed (upsert quantity, single-row delete, in-place reload + `DiffUtil`) — verified |
| B21 | Low | **No email-format validation** (login/register only checked non-empty) | `LoginFragment.kt`, `RegisterFragment.kt` | ✅ Fixed (`Patterns.EMAIL_ADDRESS`) — verified |
| B22 | Low | **Hardcoded user-facing strings** + ad-hoc currency formatting bypassed `strings.xml` | `LoginFragment.kt`, `RegisterFragment.kt`, adapters | ✅ Fixed (moved to `strings.xml`) — verified |
| B23 | Low | **No empty-cart guard:** checkout succeeded on an empty cart | `fragments/CartFragment.kt` | ✅ Fixed (checkout/clear disabled when empty) — verified |
| B24 | Low | **Weaker-than-ideal KDF.** PBKDF2-HMAC-**SHA1** (minSdk 16 constraint). Acceptable for a local demo; SHA256 preferred if minSdk rises | `auth/PasswordHasher.kt` | ⬜ Noted (no change; revisit on minSdk bump) |

---

## On-emulator verification (2026-06-28)

Driven end-to-end on `Medium_Phone_API_36.1`, fresh v3 DB:

| Fix | How verified | Result |
|-----|-------------|--------|
| B14 | Added items → opened cart → pressed back | Grid fully repopulated |
| B15 | Alice had iPhone9×2 + iPhoneX; signed out; Bob logged in | Bob's cart empty (0 items / 0 $); Alice's was 2 |
| B17 | Registered + logged in with the same password | Login succeeds (no trim mismatch) |
| B18 | `dumpsys package … flags` | No `ALLOW_BACKUP` flag present |
| B19 | Cart with 2×234 + 56 | Total Cost **524 $** (correct) |
| B20 | Tapped iPhone 9 twice | One row, **Quantity: 2** (not two rows) |
| B21 | Logged in with `notanemail` | "Enter a valid email address." |
| B22 | Grid/cart prices, add-to-cart toast | "234 $" + resource-backed toasts |
| B23 | uiautomator on empty cart | `cart_checkout` + `cart_clear_icon` `enabled="false"` |
| B16 | Full multi-screen flow w/ concurrent DB access | No crash (correctness hardening; not UI-visible) |

---

## The four audit questions

### 1. Did we build this in the most secure way?

**Mostly, for a local demo — the real gaps are now closed.** Strengths: salted PBKDF2 hashes (plan_4), duplicate-email block, off-main-thread DB. Resolved this pass: **B18** backup exfiltration (`allowBackup="false"`), **B15** cross-account cart leak (per-user scoping), **B17** auth correctness (trim). Remaining/known: **B24** PBKDF2-SHA1 is the weakest acceptable KDF (fine for a demo, flagged for a minSdk bump); this is on-device auth only (no backend/transport security); release builds run `minifyEnabled false` (no R8 shrink/obfuscation).

### 2. Did we build this in the most efficient way?

**Now yes for the cart path.** **B20** was the main inefficiency: tap-to-insert grew the table without bound and "remove" recreated the whole fragment — replaced with an upsert on a real `quantity` column, single-row delete, in-place reload, and `DiffUtil`. **B16** (DB-singleton race that could build the DB twice) is fixed. The grid still reloads the catalog from Room on every `onResume` — correct and cheap (17 rows), not cached in memory (acceptable).

### 3. What regressions could the fixes introduce? (and how they were contained)

- **B15 / B20 (cart schema):** adding `cart.user_id` + a real quantity column changed the schema → handled by the existing **destructive fallback** (DB bumped v2 → v3); every add/clear/checkout/remove query now passes the current user id. Verified on a fresh install that items persist per-user and don't leak across accounts.
- **B16:** drop-in idiomatic DCL — low risk.
- **B17:** register now takes the password verbatim; pre-existing whitespace-padded passwords may need re-registration (DB is destructive anyway).
- **B18:** users lose local backup/restore — intended for a credential store; no in-app change.
- **B19:** total display format changed; verified integer seed totals (524 $) render unchanged.

### 4. What tests do we need before shipping? (outstanding)

Only template tests exist (`ExampleUnitTest`, `ExampleInstrumentedTest`). The fixes were verified manually on the emulator; the automated guards still to be written:

**Unit (JVM):**
- `PasswordHasher`: same password+salt → same hash; different salts → different hashes.
- Password policy (B17): the string hashed at register equals the string hashed at login for the same input (incl. surrounding spaces).
- Cart totals (B19): integer prices sum correctly; `"12.99"`/`"$5"`/`"abc"` don't crash and produce documented values.
- `addOrIncrement` (B20): adding the same product twice → one row, quantity 2.

**Instrumented (Room + UI):**
- Destructive-fallback to v3 (new `cart.user_id`) loads cleanly.
- `ShrineDatabase.getDatabase` returns the **same** instance across concurrent threads (B16).
- Flow: register → login → add 2 products → cart shows them → **cart → back → grid still populated** (B14) → checkout empties cart.
- Per-user cart (B15): A adds, signs out, B logs in → B empty.

---

## Implemented fixes (what changed)

### Step 0 — B14: grid empty on return ✅
`ProductGridFragment.onDestroyView()` resets `staggeredMode = null`, so a recreated view forces `renderGrid()` to reconfigure the RecyclerView and reload the catalog.

### Step 1 — B16: DB singleton race ✅
`ShrineDatabase.getDatabase` rewritten as idiomatic double-checked locking (`INSTANCE ?: synchronized(this) { INSTANCE ?: build().also { INSTANCE = it } }`).

### Step 2 — B18: disable backup ✅
`AndroidManifest.xml` → `android:allowBackup="false"`.

### Step 3 — B17: consistent password handling ✅
`RegisterFragment.userRegister()` no longer calls `.trim()` on the password (other fields still trimmed); login already takes it verbatim.

### Step 4 — B19: money math ✅
`CartFragment.updateTotals()` parses prices with `removePrefix("$").trim().toDoubleOrNull() ?: 0.0`, formats via the `shr_price_format` string and a `formatAmount` helper (whole numbers without trailing `.0`).

### Step 5 — B21 + B22: validation & strings ✅
Email validated with `Patterns.EMAIL_ADDRESS` in `LoginFragment`/`RegisterFragment`; hardcoded strings + currency formatting moved into `res/values/strings.xml` (`shr_error_email_empty`, `shr_error_invalid_email`, `shr_error_passwords_match`, `shr_added_to_cart`, `shr_removed_from_cart`, `shr_cart_empty`, `shr_price_format`).

### Step 6 — B23: empty-cart guard ✅
`CartFragment.updateTotals()` disables `cart_checkout` and `cart_clear_icon` when the cart is empty.

### Step 7 — B15: per-user cart ✅
`CartItem` gains `user_id`; `CartItemDAO` queries (`findItem`/`getAll`/`clearCart`/`deleteCartItem`) are all scoped by `user_id`; `auth/Session.currentUserId(activity)` reads the logged-in id from session prefs and is threaded through the add-to-cart adapters and `CartFragment`. `ShrineDatabase` bumped to **version 3** (destructive fallback). Carts persist per user; no clear-on-logout needed.

### Step 8 — B20: real quantity column + DiffUtil ✅
New `database/CartOps.addOrIncrement(userId, product)` upserts one row per product (increments `product_quantity`); the cart adapter removes a single scoped row and reloads in place via a callback (no fragment recreation) and diffs with `DiffUtil`. `CartFragment` drops the old in-memory `regroupByProduct`.

---

See [BUG_INVENTORY.md](BUG_INVENTORY.md) for the consolidated defect list and [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md) for deferred work (full loading/error states, cart quantity stepper, MVVM refactor, automated test suite).
