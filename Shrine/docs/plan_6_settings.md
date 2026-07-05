---
title: Settings screen with product-grid layout toggle
status: completed
last_updated: 2026-06-27
scope: Add a Settings screen with a toggle that switches the product grid between the regular and staggered layouts, repurposing the previously-dead staggered adapter.
---

# plan_6_settings — Settings screen with product-grid layout toggle

> **Completed 2026-06-27.** Implemented and verified on-device. This plan arose from a scope change to plan_5_cleanup: rather than deleting the dead staggered-grid code, it is repurposed into a user-facing feature. The staggered mode is a **horizontal, two-row scrollable carousel** (see the redesign note in step 5). Verified end-to-end: toggle on→staggered / off→regular, add-to-cart in both, horizontal scroll, and the toggle **surviving sign-out** (the `shrine_settings` file is untouched by sign-out, which only clears the per-Activity session prefs).

## Goal

Give users a Settings screen with a toggle to switch the product grid between the regular grid and the asymmetric **staggered** layout, turning the previously-unreachable `StaggeredProductCardRecyclerViewAdapter` into a live feature.

## Scope

**In:**
- A `SettingsFragment` + layout with a Material switch.
- Persist the choice in a named `SharedPreferences` file (`shrine_settings`) so it survives sign-out.
- A toolbar entry point (gear icon) to reach Settings.
- `ProductGridFragment` picks the layout manager + adapter from the saved preference and re-applies it on resume.
- Repurpose `StaggeredProductCardRecyclerViewAdapter` to the Room `Product` model with `submit(...)`, add-to-cart, and the `shr_logo` placeholder (parity with the regular grid).

**Out:**
- Other settings (theme, account, etc.).
- Removing `network/ProductEntry.kt` (left in place by this plan; subsequently removed as a follow-up — see BUG_INVENTORY B11).
- Any change to cart/checkout/auth behavior.

## Step-by-step changes (as implemented)

1. Added `res/drawable/ic_settings_24.xml` (gear) and a `settings_icon` item in `res/menu/shr_toolbar_menu.xml` (also removed the dead commented `search` item).
2. `MainActivity.onOptionsItemSelected` routes `R.id.settings_icon` → `navigateTo(SettingsFragment(), true)`.
3. New `fragments/SettingsFragment.kt` + `res/layout/shr_settings_fragment.xml`: a `SwitchMaterial` bound to `shrine_settings`/`staggered_grid`. Constants live in `SettingsFragment` (`PREFS_FILE`, `KEY_STAGGERED_GRID`).
4. `StaggeredProductCardRecyclerViewAdapter` rewritten to take `(Context, MutableList<Product>)`, with `submit(...)`, `shr_logo` default/error image, and add-to-cart via `(context as AppCompatActivity).lifecycleScope`.
5. `ProductGridFragment`: added `renderGrid()` (reads the pref; configures a vertical `GridLayoutManager(2)`+`ProductCardRecyclerViewAdapter` or a **horizontal** `StaggeredGridLayoutManager(2, HORIZONTAL)`+`StaggeredProductCardRecyclerViewAdapter`; no-op if unchanged) called from `onResume`, and a callback-based `loadCatalog`. The staggered mode bounds the RecyclerView height (`shr_staggered_recycler_height`) so it scrolls as a horizontal 2-row carousel; the three staggered card layouts were redesigned (fixed image height, varied widths) for that orientation.
6. Strings: `shr_settings_title`, `shr_settings_staggered_label`, `shr_settings_staggered_description`.

## Files / modules touched

- New: `fragments/SettingsFragment.kt`, `res/layout/shr_settings_fragment.xml`, `res/drawable/ic_settings_24.xml`.
- Edited: `MainActivity.kt`, `fragments/ProductGridFragment.kt`, `adapters/staggeredgridlayout/StaggeredProductCardRecyclerViewAdapter.kt`, `res/menu/shr_toolbar_menu.xml`, `res/values/strings.xml`.
- Docs: `FEATURES.md` (new feature + "Not features" update), `ARCHITECTURE.md`, `CODEBASE.md`.

## Dependencies on other plans

- Builds on **plan_3_catalog** (the DB-backed `Product` catalog and the `submit`/placeholder adapter pattern).

## Risks and rollback

- **Risk:** the toggle not taking effect when returning from Settings. Mitigation: `renderGrid()` runs in `onResume` and re-applies when the preference changed (tracked by `staggeredMode`). Verified both directions on-device.
- **Rollback:** revert the listed files; the staggered adapter can return to its prior `ProductEntry` form. No schema/data impact.

## Definition of done

- Settings reachable from the product-grid toolbar; toggle persists across navigation and sign-out. ✅
- Turning it on shows the staggered layout; off shows the regular grid; add-to-cart works in both. ✅ (verified on-device)
- `./gradlew assembleDebug` passes; no crashes. ✅
- Docs updated. ✅
