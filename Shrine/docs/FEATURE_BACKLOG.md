---
title: Feature Backlog
last_updated: 2026-06-29
scope: Candidate new features for the Shrine app, with rationale and rough effort. Source for future plan_* files.
---

# Feature Backlog

Effort key: **S** ≈ <1 day · **M** ≈ 1–3 days · **L** ≈ ~1 week+. Items marked "→ plan_N" already have an implementation plan.

## Delivered (removed from the backlog)

Every item that previously sat here has shipped — the bulk via the Compose modernisation ([plan_8_modernise.md](plan/plan_8_modernise.md), Phases 0–7). They are now documented as live behaviour in [FEATURES.md](FEATURES.md) (user-facing) or [ARCHITECTURE.md](ARCHITECTURE.md) / [CODEBASE.md](CODEBASE.md) (structural):

| Former backlog item | Delivered by | Now documented in |
|---------------------|--------------|-------------------|
| Data-driven product catalog | plan_3_catalog → plan_8 (`CatalogRepository`/`CatalogSeed`) | FEATURES §3 |
| Password hashing & auth hardening | plan_4_security → plan_8 (`PasswordHasher`, dup-email guard) | FEATURES §1 |
| Cart quantity stepper | plan_8 (`CartRepository` quantity + `QuantityStepper`) | FEATURES §7 |
| Persistent login session | plan_8 (DataStore `SessionRepository` + Splash gate) | FEATURES §2 (Session persistence) |
| Order history | plan_8 (`OrderRepository` + Order screens) | FEATURES §9 |
| Product search & filter | plan_8 (`SearchRepository` + filter sheet) | FEATURES §5 |
| Product detail screen | plan_8 (`ProductDetailScreen`) | FEATURES §6 |
| Empty / loading / error states | plan_8 (every screen) | FEATURES (stack banner) |
| Edit profile | plan_8 (`EditProfileScreen` + `AuthRepository.updateProfile`) | FEATURES §11 |
| Automated test suite | plan_8 Phase 7 (44 JVM tests + CI) | CODEBASE (build & test) |
| MVVM + Navigation (+ ViewBinding → Compose) | plan_8 (MVVM + Hilt + Navigation-Compose) | ARCHITECTURE |
| Dependency refresh | plan_8 Phase 0 (AGP 8.6.1 / Kotlin 2.0.21 / Compose BOM / KSP) | ARCHITECTURE (build/toolchain) |

> Technical-debt and UI-polish follow-ups surfaced by the post-modernisation audit (system-bar insets, KDF upgrade, R8, search debounce, lifecycle-aware collectors, Coil placeholders, gutter token) are **not** features — they are tracked in [plan_9_ui_audit.md](plan/plan_9_ui_audit.md).

## Open candidate features

These are the genuinely-unbuilt items. Several were rendered as **non-functional placeholders** in the local-first demo (see the plan_8 [coverage map](plan/plan_8_modernise.md#ui-coverage-map-figma--plan)); turning them real is the natural next step and mostly depends on introducing a backend.

| Feature | Rationale | Effort | Status |
|---------|-----------|--------|--------|
| Backend / remote catalog API | The catalog is a local in-code seed and product image URLs don't resolve (blank cards). A real API + image host makes the catalog dynamic and fixes imagery. Unblocks most items below. | L | backlog |
| Forgot password / reset | Currently a placeholder destination. A real reset flow needs email/OTP delivery (backend). | M–L | backlog |
| Continue with email link (passwordless) | Placeholder on Login. Magic-link/passwordless sign-in; needs backend + deep links. | M–L | backlog |
| Track order | Placeholder on Order placed/history. Real shipment tracking needs order status updates / a carrier feed. | M | backlog |
| Help center content | Placeholder screen. FAQ/articles + contact/support entry points. | M | backlog |
| Live payment authorisation | Checkout stores only masked cards and never charges. Real payment needs a PSP (Stripe/etc.) and PCI-safe entry. | L | backlog |
| Product reviews & ratings | The Profile "Reviews" stat is always 0 and ratings are read-only. Let users read/write reviews. | M–L | backlog |
| Push notifications | Settings exposes Order-updates / Promotions toggles, but nothing sends notifications. Wire FCM + honour the prefs. | M | backlog |
| Biometric unlock / encrypted session | Optional fingerprint/face gate and at-rest encryption for the session (currently plaintext app-private DataStore). | S–M | backlog |

## Notes on sequencing

- **Backend / remote catalog** is the keystone: Forgot-password, email-link, track-order, live payment, reviews, and push all assume a server. Land it (or a mock API) first.
- The repository layer is already network-ready (`:core:data` exposes `Flow`s + suspend funcs over interfaces), so adding a remote data source is additive rather than a rewrite.
- Ship the [plan_9_ui_audit.md](plan/plan_9_ui_audit.md) inset/hardening fixes before pursuing new features — they are quality gates on the existing surface.

See [BUG_INVENTORY.md](BUG_INVENTORY.md) for historical defects and the `plan/` directory for sequenced work.
