---
title: Features
last_updated: 2026-06-29
scope: Every user-facing feature, what it does, and the end-to-end modules that implement it.
---

# Features

This documents the **actually wired-up** user-facing behavior. Each feature lists the modules involved end to end.

All paths are relative to `Shrine/`.

> **Stack (plan_8 Phases 0–5).** The app is 100% **Jetpack Compose**: `MainActivity` → [`ShrineApp`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/ShrineApp.kt) → a `NavHost` (auth/main nested graphs, type-safe routes in `ui/navigation/Routes.kt`) under a bottom-bar `Scaffold`. Each screen is a stateless `XxxContent` + a `@HiltViewModel` exposing an immutable `UiState`, in `app/.../ui/screens/`, backed by `:core:data` repositories over the `shrine.db` Room database + DataStore. Every screen carries empty/loading/error states. The legacy Fragment/XML/Volley stack was **deleted in Phase 5**. The per-screen automated test matrix is consolidated in Phase 7.

---

## 1. Registration (sign up)

**What it does:** Creates a local user account from name, email, and password (+ a required Terms checkbox), then signs the user in.

**End to end:**
- UI: `RegisterScreen`/`RegisterContent` in [`AuthScreens.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/AuthScreens.kt) — includes a password strength meter and visibility toggle.
- Validation (`RegisterViewModel`): email must contain `@`, password ≥ 8 chars, Terms & Privacy checkbox required.
- Persistence: `AuthRepository.register` generates a salt and stores a salted PBKDF2 hash (`user_pass_hash` + `user_pass_salt`), rejecting duplicate emails (unique index on `user_email`). On success it logs in to obtain the `userId` and calls `SessionRepository.signIn(...)`.

> Fields follow the figma (name/email/password + terms); phone/DOB are collected later in Edit Profile.

---

## 2. Login

**What it does:** Authenticates an existing user and starts a session, or enters as a guest.

**End to end:**
- UI: `LoginScreen`/`LoginContent` in [`AuthScreens.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/AuthScreens.kt) — email + password fields, visibility toggle, **Skip** (guest browsing), and a placeholder Forgot password destination.
- Validation: email non-empty/contains `@`; password ≥ 8 chars.
- Auth: `AuthRepository.login` looks up the user, recomputes the salted PBKDF2 hash, and compares; on success `LoginViewModel` calls `SessionRepository.signIn(...)`. **Skip** enters the main graph as a guest (`userId = -1`).
- Navigation: a session/guest sends the user into the main graph; sign-out/sign-in switch graphs via `popUpTo`.

---

## 3. Home / catalog

**What it does:** A 2-column product grid with a time-based greeting, hero promotion banner, and category tiles.

**End to end:**
- UI: `HomeScreen`/`HomeContent` in [`HomeScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/HomeScreen.kt).
- Data: `HomeViewModel` combines flows from `CatalogRepository` (products + categories), `PromotionRepository` (hero banner), `CartRepository` (cart-count badge), `SessionRepository` (greeting/name), `SettingsRepository` (grid density), and `WishlistRepository` (`wishlistedIds`).
- Images load via Coil. Tapping a category tile → `Category`; a product → `ProductDetail`. The wishlist heart on each card toggles via `setWishlisted(productId, checked)`.

---

## 4. Category

**What it does:** Products filtered to one category, with a result count and a Sort control.

**End to end:**
- UI: `CategoryScreen`/`CategoryContent` in [`CategoryScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/CategoryScreen.kt).
- `CategoryViewModel` reads the `Category(id)` nav arg via `SavedStateHandle.toRoute<>()`, combines `CatalogRepository.productsByCategory` with a `SortOption` (Featured / Price ↑ / Price ↓ / Top rated) and `wishlistedIds`.

---

## 5. Search

**What it does:** Live search with recent searches/suggestions and a filter bottom sheet.

**End to end:**
- UI: `SearchScreen`/`SearchContent` + `FilterSheet` in [`SearchScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/SearchScreen.kt).
- `SearchViewModel` combines the query, recent searches, filters (price `RangeSlider`, min rating, sort), and `wishlistedIds`; `SearchRepository` serves results + recent-search history (`recordCurrent`/`clearRecent`). The filter sheet shows a live "Show N results" count and Reset.

---

## 6. Product detail

**What it does:** Full product page with an image pager, variant swatches, wishlist, and add-to-cart.

**End to end:**
- UI: `ProductDetailScreen`/`ProductDetailContent` in [`ProductDetailScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/ProductDetailScreen.kt) — image pager with dots, rating + review count, color/variant chips, quantity stepper, sticky add-to-cart bar, and a "you may also like" row.
- `ProductDetailViewModel` reads `ProductDetail(id)`, exposes the product + wishlist state, and writes via `CartRepository.addToCart` and `WishlistRepository` toggle.

---

## 7. Cart

**What it does:** Lists cart lines with quantity steppers and an order summary, and starts checkout.

**End to end:**
- UI: `CartScreen`/`CartContent` in [`CartScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/CartScreen.kt) — per-line variant subtitle, stepper, remove; summary (Subtotal / Shipping (Free) / Total) and a sticky "Checkout" bar. Empty state when no items.
- `CartRepository` keeps one row per product+variant per user (`addOrIncrement`); totals use integer-cent math. Cart is scoped to the session `userId` (guests `-1`).

---

## 8. Checkout

**What it does:** Reviews shipping address, delivery option, and payment method, then places an order.

**End to end:**
- UI: `CheckoutScreen`/`CheckoutContent` in [`CheckoutScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/CheckoutScreen.kt) — address/delivery/payment/summary cards; total = subtotal + delivery shipping.
- `CheckoutViewModel` combines `AddressRepository`, `PaymentRepository`, `CartRepository`, and the chosen delivery option; `OrderRepository.placeOrder` persists an `Order` (+ `OrderLine` snapshots) with an order number and status, then routes to `OrderPlaced`.

---

## 9. Order placed / history / detail

**What it does:** Confirms an order, then lets the user browse order history and open an order.

**End to end:**
- UI in [`OrderScreens.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/OrderScreens.kt): **OrderPlaced** (animated check, order number, estimated arrival; Track order is a placeholder), **OrderHistory** (status tabs All/Active/Delivered with status `AssistChip`s and thumbnails), **OrderDetail**.
- All three read `OrderRepository` (history scoped per user; detail/placed by `orderId`).

---

## 10. Wishlist (Saved)

**What it does:** A grid of saved products with quick-add-to-cart and heart toggle.

**End to end:**
- UI: `WishlistScreen`/`WishlistContent` in [`WishlistScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/WishlistScreen.kt) — filled-heart toggle removes; QuickAddButton adds to cart; empty state.
- `WishlistViewModel` joins `WishlistRepository.wishlist(userId)` with `CatalogRepository` products. Toggling a heart anywhere (Home/Search/Category/detail) updates the Saved tab reactively.

---

## 11. Profile, Edit profile & sign out

**What it does:** Shows avatar + stats and the account menu; edits profile; signs out (or, as a guest, offers Sign in / Create account).

**End to end:**
- UI: `ProfileScreen`/`ProfileContent` in [`ProfileScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/ProfileScreen.kt) — avatar, Orders/Saved/Reviews stats, rows to My orders / Addresses / Payment methods / Edit profile / Settings / Help center. **Guests** see Sign in + Create account; signed-in users see Sign out.
- `ProfileViewModel` reads `SessionRepository` + order/saved counts. **Edit profile** ([`EditProfileScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/EditProfileScreen.kt)) edits name/email/phone/DOB (DatePicker) via `AuthRepository`/`SessionRepository`.

---

## 12. Settings

**What it does:** Theme, grid density, notification preferences, and About.

**End to end:**
- UI: `SettingsScreen`/`SettingsContent` in [`SettingsScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/SettingsScreen.kt) — theme `SegmentedButton` (System/Light/Dark), Large-imagery switch, Order-updates/Promotions switches, About/version.
- `SettingsRepository` (DataStore) persists the prefs; the theme is honored at the `ShrineTheme` root via `AppViewModel.themeMode`.

---

## 13. Addresses & Payment methods

**What it does:** CRUD over shipping addresses and masked payment cards, reached from Profile and from Checkout's "Change".

**End to end:**
- UI: [`AddressesScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/AddressesScreen.kt) and [`PaymentMethodsScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/PaymentMethodsScreen.kt) — list + FloatingActionButton + ModalBottomSheet add/edit forms, per-user.
- Backed by `AddressRepository` / `PaymentRepository`.

---

## Placeholders (local-first, intentionally non-functional)

These render and navigate but are non-functional in this no-backend demo: **Forgot password** / Continue with email link (Login), **Track order** (Order placed/history), **Help center** ([`HelpCenterScreen.kt`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/ui/screens/HelpCenterScreen.kt)), and live payment authorisation. See the coverage map in [plan_8_modernise.md](plan_8_modernise.md).

See [ARCHITECTURE.md](ARCHITECTURE.md) for design context and [CODEBASE.md](CODEBASE.md) for the file map.
