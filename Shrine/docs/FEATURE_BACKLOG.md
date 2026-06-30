---
title: Feature Backlog
last_updated: 2026-06-27
scope: Candidate new features for the Shrine app, with rationale and rough effort. Source for future plan_* files.
---

# Feature Backlog

Effort key: **S** ≈ <1 day · **M** ≈ 1–3 days · **L** ≈ ~1 week+. Items marked "→ plan_N" already have an implementation plan.

| Feature | Rationale | Effort | Status |
|---------|-----------|--------|--------|
| Data-driven product catalog | The catalog is hardcoded in `ProductGridFragment`; a real source (Room-seeded from `products.json`, or a remote API) makes it maintainable and fixes blank images. | M | → plan_3_catalog |
| Password hashing & auth hardening | Plaintext credentials are a real security hole; also adds duplicate-email protection. | M | → plan_4_security |
| Cart quantity stepper | Today each tap inserts a new row and "remove" deletes the whole product. A +/- stepper with a real `quantity` column is the expected e-commerce UX. | M | backlog |
| Persistent login session | Login state lives in per-Activity `SharedPreferences` and is lost easily; a DataStore-based session with auto-login improves UX. | S–M | backlog |
| Order history | Checkout currently just clears the cart. Persisting orders and showing past orders is core e-commerce value. | M | backlog |
| Product search & filter | Toolbar search/filter icons and strings already exist but are commented out; wiring them adds real catalog navigation. | M | backlog |
| Product detail screen | A tappable detail page (description, larger image, add-to-cart) is standard and enables richer data. | M | backlog |
| Empty / loading / error states | Cart and grid have no empty or error UI; adding them makes the app feel finished and surfaces failures (e.g., image load). | S | backlog |
| Edit profile | Profile is read-only; allowing edits (and persisting to the `users` row) rounds out account management. | S–M | backlog |
| Automated test suite | Only template tests exist. Unit tests for validation/auth and instrumented tests for the register→login→cart flow guard all the above. | M | backlog |
| MVVM + ViewBinding + Navigation Component | Current code mixes DB/UI/navigation in fragments. Introducing ViewModels, a repository, ViewBinding, and the Navigation component would remove most structural bugs at the source. | L | backlog |
| Dependency refresh | Material 1.6.0, coroutines 1.6.4, etc. are dated; updating reduces security/compat risk. | S | backlog |

## Notes on sequencing

- The two backlog items that double as correctness/security fixes (**catalog**, **security**) are planned now because they unblock or de-risk later features (stepper, order history, search all assume a real catalog and safe auth).
- **Cart quantity stepper** and **order history** should follow plan_2_cart and plan_3_catalog, since they build on a correct cart and a real product model.
- The **MVVM refactor** is deliberately deferred: high value but high risk, and best attempted once tests (above) exist to catch regressions.

See [BUG_INVENTORY.md](BUG_INVENTORY.md) for defects and the `plan_*` files for sequenced work.
