---
title: Data-driven product catalog
status: active
last_updated: 2026-06-27
scope: Replace the hardcoded product list with a real data source (Room-seeded from products.json), fix product images, and correct the products DAO.
---

# plan_3_catalog — Data-driven product catalog

## Goal

Make the product grid data-driven instead of a hardcoded inline list, with images that actually load, and a `PrductDAO`/`products` table that is correct and used. This unblocks future catalog features (search, product detail, quantity stepper) and removes the blank-image problem.

Third in sequence: it is a meaningful feature improvement and de-risks later backlog work, but it is larger than the login/cart fixes and depends on a correct cart for end-to-end verification.

## Scope

**In:**
- Seed the Room `products` table from a bundled JSON catalog on first run.
- Load the catalog in `ProductGridFragment` from the DB (via a corrected DAO) instead of the hardcoded `listOf(Product(...))`.
- Fix `PrductDAO` queries to target the `products` table (B8).
- Replace the stale/blank image URLs with working assets or URLs (B11, image portion).
- Rename the misspelled `PrductDAO` → `ProductDAO` (and its `productDao()` accessor stays).

**Out:**
- Remote/networked catalog API (backlog).
- Product detail / search / filter screens (backlog).
- Removing the now-unused `StaggeredProductCardRecyclerViewAdapter` and `ProductEntry`/`products.json`-for-`ProductEntry` (→ plan_5_cleanup, after this plan decides what the catalog source is).

## Step-by-step changes

1. **Decide the catalog source.** Use a bundled JSON (reuse/replace `res/raw/products.json`, adjusting its schema to match `models/Product` fields) parsed with Gson (already a dependency).
2. **Correct the DAO** (`database/PrductDAO.kt`): fix `getAll()` to `SELECT * FROM products`, remove/repurpose the bogus `clearCart()` (`DELETE FROM cart`), and add an insert/`insertAll`. Rename file/class to `ProductDAO` and update `ShrineDatabase.productDao()` return type.
3. **Seed on first launch.** In `ShrineApplication.onCreate` (or a `RoomDatabase.Callback` on `ShrineDatabase`), if the `products` table is empty, parse the JSON and `insertAll`. Run on a background dispatcher.
4. **Load from DB in the grid:** in `ProductGridFragment.onCreateView`, replace the hardcoded `var products = listOf(Product(...))` with a lifecycle-scoped load: `withContext(Dispatchers.IO) { database.productDao().getAll() }`, then set/refresh the `ProductCardRecyclerViewAdapter`. Give the adapter a `submit(list)`/refresh entry point (mirroring plan_2's adapter change).
5. **Fix images (B11):** point product URLs at assets that resolve. Options, in preference order: (a) bundle local drawables and load by resource, (b) use known-good remote URLs. Update `ImageRequester` usage only if switching away from `NetworkImageView`.
6. **Verify add-to-cart still works:** `ProductCardRecyclerViewAdapter` inserts a `CartItem` from the `Product`; ensure field mapping is intact after the data-source change.
7. **Smoke test:** fresh install → grid populated from DB with visible images → add to cart → cart (plan_2) shows them.

## Files / modules touched

- `app/src/main/res/raw/products.json` (schema aligned to `Product`).
- `app/src/main/java/.../database/PrductDAO.kt` → `ProductDAO.kt` (queries + insert).
- `app/src/main/java/.../database/ShrineDatabase.kt` (accessor type; optional seed callback).
- `app/src/main/java/.../application/ShrineApplication.kt` (first-run seed, if not on the DB callback).
- `app/src/main/java/.../fragments/ProductGridFragment.kt` (DB-backed load + adapter refresh).
- `app/src/main/java/.../adapters/lineargridlayout/ProductCardRecyclerViewAdapter.kt` (refreshable list; image source if changed).
- Possibly `network/ImageRequester.kt` (only if image strategy changes).
- Docs: update `FEATURES.md` §3 and "Not features" (products table/DAO now active), `ARCHITECTURE.md` data-flow §2 + "Known issues" (hardcoded catalog, wrong-table DAO, stale images), `CODEBASE.md` (DAO rename; raw/json now used).

## Dependencies on other plans

- **Soft dependency on plan_2_cart** for end-to-end verification (add-to-cart → cart display). The catalog changes themselves are independent of plan_1.
- **Feeds plan_5_cleanup**: once the catalog source is decided here, plan_5 removes whichever dead path (staggered adapter, `ProductEntry`) is confirmed unused.

## Risks and rollback

- **Risk:** Room schema/seed change. Mitigation: `products` table already exists in the `@Database` (version 1); seeding only inserts rows. If `Product` field names change, bump DB `version` and add a migration or `fallbackToDestructiveMigration()` (acceptable for this demo's local data).
- **Risk:** image strategy change introduces new failures. Mitigation: prefer bundled local drawables to remove network dependence entirely.
- **Rollback:** revert `ProductGridFragment` to the hardcoded list and restore the original DAO; drop the seed callback. Bundled JSON/asset additions are inert if unreferenced.

## Definition of done

- The product grid is populated from the Room `products` table, seeded on first run; no hardcoded `listOf(Product(...))` remains in `ProductGridFragment`.
- Product images render (no blank cards).
- `PrductDAO`/`ProductDAO` queries target the `products` table and are actually used.
- Add-to-cart → cart (plan_2) shows the correct items.
- `./gradlew assembleDebug` passes; fresh-install smoke test verified.
- `FEATURES.md`, `ARCHITECTURE.md`, `CODEBASE.md` updated (including moving the catalog/DAO out of "dead code").
