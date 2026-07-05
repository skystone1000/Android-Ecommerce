---
title: Dead-code removal and deprecation cleanup
status: completed
last_updated: 2026-06-27
scope: Remove confirmed dead code, fix deprecated API usage, correct minor build/annotation issues, and align docs with the cleaned tree.
---

> **Completed 2026-06-27, with a scope change.** Fixes B10, B12, B13 and removes the dead commented `search` menu item. **Per a decision during execution, the staggered grid was NOT deleted** — instead it was repurposed into a feature (see [plan_6_settings](plan_6_settings.md)). `ProductEntry` was the only remaining unused class (later removed in a follow-up — see BUG_INVENTORY B11). Verified on-device: backdrop reveal still works (B10), order-placed animation unaffected (B12), and the app runs end-to-end on `targetSdk 34` (B13).

# plan_5_cleanup — Dead-code removal and deprecation cleanup

## Goal

Reduce confusion and maintenance cost by deleting confirmed dead code, fixing deprecated/minor issues (B10, B12, B13), and leaving the tree matching the docs. Low risk, low coupling — but lower leverage than the functional fixes, so it runs last and after plan_3 has decided which catalog paths are actually used.

## Scope

**In:**
- Remove the staggered-grid adapters if still unused after plan_3 (`adapters/staggeredgridlayout/StaggeredProductCardRecyclerViewAdapter.kt`, `StaggeredProductCardViewHolder.kt`).
- Remove `network/ProductEntry.kt` + its `initProductEntryList` and the `R.raw.products` loader **iff** plan_3 did not adopt them (B11).
- Delete leftover commented-out blocks (e.g., commented `ProductEntry` list and toolbar search/filter items) that remain dead after plan_3.
- Fix the deprecated `defaultDisplay.getMetrics` (B10).
- Fix the misplaced `@RequiresApi` in `OrderPlacedFragment` (B12).
- Align `targetSdk` to `compileSdk` (B13).

**Out:**
- Any behavior change to live features.
- The `PrductDAO` correction — owned by plan_3 (this plan only deletes it if plan_3 concluded it's unnecessary).
- Reviving search/filter (that's a backlog feature, not cleanup).

## Step-by-step changes

1. **Re-confirm dead code** with a usage check (grep for each symbol) *after plan_3 has merged*, since plan_3 may have adopted `products.json`/the products DAO. Only delete what is still unreferenced.
2. **Delete unused staggered adapters** and their layouts if those layouts (`shr_staggered_product_card_*.xml`) are unreferenced.
3. **Delete `ProductEntry`/raw-JSON loader** if plan_3 used a different mechanism; otherwise leave them.
4. **Fix `NavigationIconClickListener.kt:27`:** replace `(context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)` with `context.resources.displayMetrics.heightPixels` (or `WindowMetrics` on API 30+) to drop the deprecated call.
5. **Fix `OrderPlacedFragment.kt:45`:** move/remove the `@RequiresApi(LOLLIPOP)` so it annotates the appropriate scope (or drop it, since `minSdk 16` and the `AnimatedVectorDrawable` branch is already type-guarded).
6. **Align SDK levels:** set `targetSdkVersion 34` in `app/build.gradle` after a quick check of API-34 behavior changes for this simple app.
7. **Lint pass:** run `./gradlew lint` and clear easy wins (unused imports/resources surfaced by the deletions).

## Files / modules touched

- Delete: `adapters/staggeredgridlayout/*` (if unused), `network/ProductEntry.kt` (if unused), and any now-orphaned `res/layout/shr_staggered_product_card_*.xml` / `res/raw/products.json` (only if plan_3 didn't adopt them).
- Edit: `NavigationIconClickListener.kt` (deprecation), `fragments/OrderPlacedFragment.kt` (annotation), `app/build.gradle` (`targetSdk`).
- Docs: update `CODEBASE.md` (remove deleted modules from the tree + "dead code" notes), `FEATURES.md` "Not features" (remove what was deleted), `ARCHITECTURE.md` "Known issues" (drop dead-code/deprecation items).

## Dependencies on other plans

- **Hard dependency on plan_3_catalog**: plan_3 determines whether `products.json`, `ProductEntry`, and the products DAO are live. Running cleanup first risks deleting something plan_3 wants. Do plan_3 first.
- Independent of plan_1/plan_2.

## Risks and rollback

- **Risk:** deleting something still referenced → compile break. Mitigation: the step-1 usage re-check + a full `assembleDebug` after each deletion.
- **Risk:** `targetSdk 34` introduces a runtime behavior change. Mitigation: smoke-test the full flow; revert just the `targetSdk` line if anything regresses.
- **Rollback:** all changes are deletions/small edits; `git revert` restores them. No data/schema impact.

## Definition of done

- No unreferenced adapters/loaders/layouts remain; `grep` for the deleted symbols returns nothing.
- `defaultDisplay.getMetrics` and the misplaced `@RequiresApi` are gone.
- `targetSdk` equals `compileSdk` (34) and the app still runs end to end.
- `./gradlew assembleDebug` and `./gradlew lint` pass with no new errors.
- `CODEBASE.md`, `FEATURES.md`, `ARCHITECTURE.md` reflect the cleaned tree (no stale "dead code" references).
