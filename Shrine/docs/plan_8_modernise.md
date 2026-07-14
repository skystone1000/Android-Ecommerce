---
title: Modernisation — Compose + Jetpack rewrite
status: active
last_updated: 2026-06-29
scope: Full migration of the Shrine app from single-Activity/Fragments/XML/Volley to a modern Jetpack stack — 100% Jetpack Compose (Material 3), Navigation-Compose, MVVM + Hilt, repository layer over Room + DataStore, Coil. Targets a premium-minimal light/dark UI and a full e-commerce feature set. Progress: Phases 0–6 implemented (build + unit tests verified — legacy deleted, package renamed to com.skystone1000.shrine); Phase 7 pending.
---

# Plan — Modernisation to Compose + Jetpack

This plan rebuilds the UI and architecture on a modern stack while preserving the local-first data model. It supersedes the Fragment/XML UI; the Room entities and business rules carry over behind a new repository layer.

> Companion: the **UI/design brief** for generating the new screens lives in [design_brief_modernise.md](design_brief_modernise.md). This plan is the *engineering* side; that brief is the *visual* side. They share the same screen list and design tokens.

## Decisions (locked)

| Area | Decision |
|------|----------|
| UI toolkit | **100% Jetpack Compose**, Material 3 (M3 expressive components where they fit). No XML layouts, no Fragments. |
| Min / target SDK | **minSdk 24**, target/compile **35** (latest stable). Drops `legacy-support-v4`. |
| Visual direction | **Premium minimal retail**, full **light + dark** theming (see design brief). |
| Feature scope | **Full e-commerce**: existing 9 + product detail (with **color/variant swatches**), search/filter (with **recent searches** + suggestions), categories, cart quantity stepper, checkout (**shipping address + delivery option + payment method**), order history (**status tabs, track, reorder**), wishlist, promotions (**hero banner**), richer profile (**stats, addresses, payment methods, help**), notification & appearance settings, **guest/"Skip" browsing**. See the [UI coverage map](#ui-coverage-map-figma--plan) for the full figma→plan trace. |
| Architecture | **MVVM** (ViewModel + immutable `UiState` via `StateFlow`), unidirectional data flow. |
| DI | **Hilt**. |
| Data | **Room behind a repository layer**; **DataStore** (Preferences) for session + settings. No backend (local-first), but repositories are network-ready. |
| Navigation | **Navigation-Compose** with **type-safe routes** (Kotlin serialization). Bottom-bar scaffold + auth/main nested graphs. |
| Async | Coroutines + Flow end-to-end. |
| Images | **Coil 3** (replaces Volley/`ImageRequester`). |
| Build | Gradle **version catalog** (`libs.versions.toml`), **KSP** (replaces kapt) for Room + Hilt, Compose Compiler Gradle plugin (Kotlin 2.x). |
| Modularisation | Pragmatic multi-module: `:app`, `:core:designsystem`, `:core:data`, `:core:database`, `:core:model`, `:feature:*`. (Start single-module-with-packages if preferred; the package structure below maps 1:1 to modules.) |
| Package / app id | Migrate **`com.google.codelabs.mdc.kotlin.shrine` → `com.skystone1000.shrine`** (both the Gradle `applicationId` and the source package / module `namespace`s). New modules use `com.skystone1000.shrine.{core.*, designsystem, feature.*}`. Executed as a single pass in **Phase 6** (after legacy is deleted, so only the surviving Compose code is touched). The `shr_` resource prefix, `Theme.Shrine` style names, and the user-facing app **display name** are out of scope here (brand renaming is tracked separately). |

## Target architecture

```
┌──────────────────────────── :app ────────────────────────────┐
│  MainActivity (single, ComponentActivity)                     │
│  └─ ShrineApp() : NavHost + Scaffold(bottomBar)               │
└───────────────────────────────────────────────────────────────┘
        │ navigates (type-safe routes)
        ▼
┌──────────────── :feature:* (per screen) ─────────────────────┐
│  Screen(Composable, stateless)  ◀── collectAsStateWithLifecycle
│  ViewModel(@HiltViewModel)  ── UiState(StateFlow) / onEvent() │
└───────────────────────────────────────────────────────────────┘
        │ calls
        ▼
┌──────────────── :core:data (repositories) ───────────────────┐
│  AuthRepository · CatalogRepository · CartRepository ·        │
│  OrderRepository · WishlistRepository · SessionRepository ·   │
│  SettingsRepository · AddressRepository · PaymentRepository · │
│  PromotionRepository · SearchRepository                       │
└───────────────────────────────────────────────────────────────┘
     │ Room DAOs                       │ DataStore
     ▼                                 ▼
┌──── :core:database ────┐     ┌──── session/settings ────┐
│ ShrineDatabase + DAOs  │     │ Preferences DataStore     │
│ entities (:core:model) │     └───────────────────────────┘
└────────────────────────┘
```

**Layering rules:** Compose never touches Room/DataStore directly — only ViewModels, and ViewModels only touch repositories. Repositories own the data sources and expose `Flow`s + suspend functions. `UiState` is immutable; screens are stateless and hoisted.

## Navigation model (the "better navigation")

Single `NavHost` with two nested graphs and type-safe destinations:

- **AuthGraph** (start when no session): `Splash → Login ⇄ Register`. Login's **"Skip"** enters MainGraph as a **guest** (browse-only; add-to-cart/wishlist/checkout prompt sign-in). `Forgot password` and `Continue with email link` are placeholder destinations (see coverage map).
- **MainGraph** (start when session exists), hosted in a `Scaffold` with a **bottom navigation bar** (Home · Search · Cart `badge` · Saved · Profile):
  - **Home** (catalog + hero promo + category tiles) → `Category` → `ProductDetail` → `Cart`
  - **Search** (query + recent/suggestions + filter sheet) → `ProductDetail`
  - **Cart** → `Checkout` → `OrderPlaced` (→ `OrderHistory`/Track)
  - **Wishlist (Saved)** → `ProductDetail`
  - **Profile** → `OrderHistory` (→ `OrderDetail`), `Addresses`, `PaymentMethods`, `EditProfile`, `Settings`, `HelpCenter` (placeholder), `Sign out`
- Routes are `@Serializable` data classes/objects (e.g. `data class ProductDetail(val id: Long)`), so arguments are compile-time checked. Deep-link ready.
- Session-driven start destination; sign-out clears the back stack by switching graphs (no Activity restart).

## Data & domain model (carried over, extended)

Existing entities evolve; nothing is thrown away. Fields in **bold** are new requirements surfaced by the figma design:

- `User` — keep (hash + salt, unique email); add **`phone`, `dateOfBirth`, `avatarUri`** (Edit-profile screen exposes name, email, phone, DOB + avatar photo).
- `Product` — add `category`, `description`, `rating`, **`reviewCount`** ("4.0 · 218 reviews"), `imageUrls: List<String>` (TypeConverter; detail is a 4-image pager), **`originalPrice`** (cents, nullable — strikethrough sale price `$1,299 / $1,499`), **`variants: List<Variant>`** (TypeConverter) where a `Variant` carries a label such as `Sand`/`Ivory` and a type such as `Over-ear`/`Ceramic` (detail shows "Color · Sand" swatches; cart/lines show "Sand · Over-ear"). Keep price as **integer minor units (cents)** instead of free-form `String` (fixes the class of bugs behind B7/B19).
- `CartItem` — keep `user_id` + real `quantity: Int` (already added in plan_7); price in cents; add **`selectedVariant`** so a line can read "Sand · Over-ear".
- **New `Order` + `OrderLine`** — order has **`orderNumber` (e.g. `SH-4821`), `status` (`PLACED`/`IN_TRANSIT`/`DELIVERED`), `placedAt`, `subtotalCents`, `shippingCents`, `totalCents`, `deliveryOption`, `estimatedArrival`**; lines snapshot product name/variant/price/qty/image. Drives history tabs, Track, Reorder.
- **New `Address`** (per-user shipping address: name, lines, city/state/zip, default flag) and **New `PaymentMethod`** (per-user, masked card `•••• 4291` + expiry) — selected at checkout ("Change"), managed from Profile.
- **New `Promotion`/`Banner`** (hero "SUMMER EDIT — The quiet luxury edit"), **New `Category`** (Audio/Fashion/Beauty/Home tiles), and **New `RecentSearch`** (per-user query history for the Search screen's RECENT list; suggestions derive from the catalog).
- Session (`user_id`, name, email, phone) and settings move from per-Activity `SharedPreferences` to **DataStore**. Settings now hold **`themeMode` (System/Light/Dark)**, **`largeImagery` (grid density)**, and **notification prefs (`orderUpdates`, `promotions`)**.
- DB **version bump + Room migrations** (or destructive fallback for the demo) with a migration test.

> **Local-first / demo placeholders.** With no backend, a few figma affordances are intentionally non-functional UI: **Forgot password**, **Continue with email link**, **Track order**, **Help center**, and live payment authorisation. They render and navigate but show a placeholder/snackbar. They are listed in the [coverage map](#ui-coverage-map-figma--plan) and pushed to [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md) rather than built now.

## UI coverage map (figma → plan)

Every screen/element in the figma design doc (`figma/Shrine.dc.html`, 15 screens + design-system + component sheet) is accounted for below. **Real** = built as working functionality; **Placeholder** = renders and navigates but is non-functional in this local-first demo (routed to [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md)).

| Figma element | Covered by | Status |
|---------------|-----------|--------|
| Light/Dark tokens, type scale, spacing/shape/elevation/motion | Phase 1 `ShrineTheme` (color schemes, `Typography`, `Shapes`, tokens) | Real |
| Component sheet (buttons, fields, search, chips, stepper, rating, price, product card, category tile, section header, app bars, bottom nav, list row, banner, snackbar, dialog, states, filter sheet) | Phase 1 core composables (expanded list) | Real |
| **Splash** — facet mark + loader | Screen 0 | Real |
| **Login** — email/password, visibility toggle | Screen 1 | Real |
| **Login** — Skip (guest browsing) | Screen 1 + guest path in nav model | Real |
| **Login** — Forgot password, Continue with email link | Screen 1 | Placeholder |
| **Register** — name/email/password, strength meter, Terms checkbox | Screen 2 | Real |
| **Home** — greeting + name, hero promo, category tiles, New-arrivals grid, cart badge | Screen 3, `Promotion`/`Category` | Real |
| **Category** — filter chips, result count, Sort, skeleton | Screen 4 | Real |
| **Search** — recent + suggestions; filter sheet (category, price range, rating, sort, live count, reset) | Screen 5, `RecentSearch`/`SearchRepository` | Real |
| **Product detail** — image pager + dots, rating + review count, color/variant swatches, description, share, wishlist, "you may also like", sticky add-to-cart | Screen 6; `Product.variants`/`reviewCount`/`imageUrls` | Real |
| **Cart** — line items + variant, stepper, remove, Edit, Subtotal/Shipping/Total, sticky checkout | Screen 7 | Real |
| **Checkout** — shipping address, delivery options (free/express → total), payment method, place order | Screen 8; `Address`/`PaymentMethod`/`Order` | Real |
| **Order placed** — animated check, order number, estimated arrival, Continue shopping | Screen 9 | Real |
| **Order placed / history** — Track order | Screens 9–10 | Placeholder |
| **Order history** — status tabs, status chips, thumbnails, Reorder | Screen 10; `Order.status` | Real |
| **Wishlist** — saved grid, heart toggle, quick add, count | Screen 11 | Real |
| **Profile** — avatar, stats (Orders/Saved/Reviews), menu, sign out | Screen 12 | Real |
| **Profile** — Addresses, Payment methods entries | Screens 15–16 | Real |
| **Profile** — Help center | Screen 12 entry | Placeholder |
| **Edit profile** — avatar photo, name/email/phone, DOB date picker | Screen 13; `User.dateOfBirth`/`avatarUri` | Real |
| **Settings** — theme segmented, large-imagery toggle, notification switches, about/version, Terms link | Screen 14; `SettingsRepository` | Real |

**Discrepancy flagged:** the *design brief* (§3) lists Register fields as name/email/phone/organisation/password/confirm-password, but the *figma* Register screen shows only name/email/password (+ terms). The plan follows the **figma** (the concrete design) — phone/organisation/confirm-password are dropped from Register; phone is collected later in Edit Profile. If the richer Register form is wanted, say so and the plan/brief will be reconciled.

## Phased migration

Each phase is independently shippable and leaves the app building. Old and new code can coexist (Compose hosted in the existing Activity) until the final cleanup.

### Phase 0 — Build foundation (no behaviour change)
- Adopt `libs.versions.toml`; bump Gradle/AGP/Kotlin (2.x) ; add the **Compose Compiler** Gradle plugin.
- minSdk → 24; remove `legacy-support-v4`.
- Migrate Room from **kapt → KSP**; add **Hilt**, **DataStore**, **Coil 3**, **Navigation-Compose**, **Compose BOM**, **lifecycle-viewmodel-compose**, **kotlinx-serialization**.
- Add `Application` `@HiltAndroidApp`.
- _Exit:_ `assembleDebug` green, app behaves exactly as today.

### Phase 1 — Design system (`:core:designsystem`)
- M3 `ColorScheme` for **light + dark** from the design brief tokens; `Typography`; `Shapes`; spacing/elevation tokens.
- `ShrineTheme { }` with dynamic-color opt-out (brand palette wins) and dark-mode following system + a user override.
- Core composables: `ShrineButton` (filled/tonal/outlined/text/icon, with pressed/disabled/**loading** states), `ShrineTextField` (label/helper/error/focused + **password visibility toggle**), **`PasswordStrengthMeter`**, **`SearchBar`** (inactive + active, with `tune` filter action), `PriceText` (+ strikethrough original), `QuantityStepper`, `ProductCard` (image, name, price, **wishlist `IconToggleButton`**, rating, optional `NEW` badge), `RatingBar`, `Chip`/`FilterChip`, **`AssistChip`** (order status), **`SegmentedButton`** (theme), **`Switch`**, **`RadioButton`**, **`Checkbox`** (terms), **`Slider`/RangeSlider** (price filter), `TopBar` (large + small, back/cart/search/more actions), `BottomBar`, **`TabRow`** (order history), **`SectionHeader`** ("see all"), **`CategoryTile`**, **`HeroBanner`** (promo), **`ListRow` + `Avatar` + `Divider`**, **`Snackbar` host**, **`AlertDialog`**, **`DatePicker`** (DOB), `EmptyState`, `LoadingState` (**skeleton shimmer**), `ErrorState`, `Badge`.
- _Exit:_ a `@Preview` gallery (light + dark) renders every component.

### Phase 2 — Data layer (`:core:data`, `:core:database`, `:core:model`)
- Extract entities to `:core:model`; move `ShrineDatabase`/DAOs to `:core:database` (KSP).
- Repositories with interfaces + Hilt bindings: `AuthRepository`, `CatalogRepository`, `CartRepository`, `OrderRepository`, `WishlistRepository`, `SessionRepository` (DataStore), `SettingsRepository` (DataStore), `AddressRepository`, `PaymentRepository`, `PromotionRepository`, `SearchRepository` (recent searches + suggestions).
- Port `PasswordHasher` as-is. Replace `ImageRequester` (Volley) usage with Coil.
- Seed catalog from `products.json` via repository on first run.
- _Exit:_ repositories unit-tested with an in-memory Room DB.

### Phase 3 — Navigation skeleton (`:app`)
- `MainActivity` → `setContent { ShrineTheme { ShrineApp() } }`.
- `ShrineApp`: `NavHost` + auth/main graphs + bottom bar; start destination from `SessionRepository`.
- Stub each destination with a placeholder screen.
- _Exit:_ can navigate the whole graph (auth ⇄ main, bottom tabs, sign-out) with stubs.

### Phase 4 — Screen-by-screen migration
Each screen = stateless `Screen` + `@HiltViewModel` + `UiState` + events, wired to a repository. Order:

| # | Screen | ViewModel reads/writes | Notes / new vs port (figma specifics) |
|---|--------|------------------------|---------------------|
| 0 | **Splash** | SessionRepository | new — facet logomark + indeterminate loader; routes to Auth/Main by session |
| 1 | **Login** | AuthRepository, SessionRepository | port; email + ≥8 pwd validation, hash verify, **password visibility toggle**, **"Skip" (guest)**; *placeholder:* Forgot password, Continue with email link |
| 2 | **Register** | AuthRepository | port; dup-email guard, no password trim, **password strength meter**, **required Terms & Privacy checkbox**, visibility toggle |
| 3 | **Home / Catalog** | CatalogRepository, PromotionRepository, CartRepository, SettingsRepository | port + **time-based greeting + user name**, **hero promo banner ("Shop now")**, **category tiles**, "New arrivals" section header (See all), 2-up grid, cart badge |
| 4 | **Category** | CatalogRepository | new — products by category; **filter chips, result count, swap_vert Sort**, loading skeleton |
| 5 | **Search** | SearchRepository, CatalogRepository | new — active query, **RECENT searches + SUGGESTIONS**, results; **filter bottom sheet**: category, **price RangeSlider**, rating (4+/3+), sort, live "Show N results", Reset |
| 6 | **Product Detail** | CatalogRepository, CartRepository, WishlistRepository | new — **image pager (1/4 dots)**, name/price, **rating + review count**, **color/variant swatches**, description, **share**, wishlist heart, **"you may also like" row**, sticky add-to-cart with stepper |
| 7 | **Cart** | CartRepository | port + **QuantityStepper**, per-user, decimal-safe totals, **variant subtitle**, remove, **Edit mode**, summary (**Subtotal / Shipping / Total**), sticky "Checkout · $total" |
| 8 | **Checkout** | CartRepository, OrderRepository, AddressRepository, PaymentRepository | new — **shipping address (Change)**, **delivery options (Standard Free / Express $12 → affects total)**, **payment method (Change)**, review + place order (persists an `Order` with number/status) |
| 9 | **Order Placed** | OrderRepository | port; animated check confirmation, **order number**, **estimated arrival**; *placeholder:* Track order; Continue shopping |
| 10 | **Order History** | OrderRepository | new — **status tabs (All/Active/Delivered)**, status `AssistChip`, thumbnails, **Track order / Reorder**, → Order detail |
| 11 | **Wishlist (Saved)** | WishlistRepository, CartRepository | new — per-user saved grid, filled-heart `IconToggleButton`, **quick "add" to cart**, count, empty state |
| 12 | **Profile** | SessionRepository, OrderRepository, WishlistRepository | port; **avatar + stats (Orders/Saved/Reviews)**; entries to My orders, **Addresses, Payment methods**, Edit profile, Settings, **Help center** (placeholder), Sign out |
| 13 | **Edit Profile** | AuthRepository, SessionRepository | new — **avatar photo**, name, email, phone, **date of birth (DatePicker)**; Save |
| 14 | **Settings** | SettingsRepository | port + **theme SegmentedButton (System/Light/Dark)**, **Large imagery (grid density) Switch**, **notification switches (Order updates, Promotions)**, **About (version, Terms & Privacy)** |
| 15 | **Addresses** | AddressRepository | new — list/add/edit/select shipping addresses (reached from Profile & Checkout "Change") |
| 16 | **Payment methods** | PaymentRepository | new — list/add/select masked cards (reached from Profile & Checkout "Change") |

Every screen ships with empty/loading/error states (closes B23 permanently) and a Compose UI test + ViewModel unit test. Screens 15–16 are lightweight CRUD over local data; **Help center** and **Track order** are static placeholder destinations (see coverage map). Screen numbers here sequence work and do not change the figma-aligned 15-screen design set (Splash + 14) — addresses/payment-methods are the management screens behind Checkout's "Change" affordances.

> **Status (2026-06-29):** all 17 screens (0–16) are implemented as stateless `@Composable` + `@HiltViewModel` + `UiState`, wired to `:core:data` repositories, replacing the Phase 3 stubs in `app/.../ui/screens/`; each carries empty/loading/error states. `ShrineApp` now routes to the real screens and the root `ShrineTheme` honors the Settings theme preference. `assembleDebug` verified green. The **per-screen automated test matrix** (ViewModel unit + Compose UI tests) is **deferred to Phase 7 — Quality gates** to keep this phase's diff focused on the migration; the design-system Robolectric repository tests from Phase 2 remain green.

### Phase 5 — Delete legacy
- Remove all Fragments, XML layouts, RecyclerView adapters, `NavigationHost`, `NavigationIconClickListener`, `ImageRequester`/Volley, per-Activity `SharedPreferences` usage.
- _Exit:_ no `androidx.fragment`/Volley/XML-layout references remain.

> **Status (2026-06-29): done.** Deleted from `:app`: `fragments/`, `adapters/` (linear + staggered), `database/` (legacy `contactDB` Room DB + DAOs + `ProductSeed`), `models/` entities, `network/ImageRequester` (Volley), `auth/` (`PasswordHasher` + `Session`), `NavigationHost.kt`, `NavigationIconClickListener.kt`, all `res/layout/*` + `res/menu/` + `res/animator/` + `res/raw/products.json` + legacy `shr_*` drawables, and the `Widget.Shrine.*` styles (kept only `Theme.Shrine` as the Activity window theme). `ShrineApplication` simplified to a bare `@HiltAndroidApp` shell. `app/build.gradle` dropped Room/Volley/Gson/`fragment-ktx` (Material retained for the window theme). The only surviving `:app` Kotlin is `MainActivity`, `ShrineApplication`, and the Compose `ui/` package. `assembleDebug` verified green; `grep` confirms no `androidx.fragment`/Volley/Gson/Room/`R.layout` references remain in `:app`.

### Phase 6 — Package & application-id migration
Rename `com.google.codelabs.mdc.kotlin.shrine*` → `com.skystone1000.shrine*` in one mechanical pass. Done **here**, after Phase 5, because the legacy Fragment code is already gone — so only the surviving Compose modules are renamed, once, over a stable tree.

> **Current state:** Phases 0–2 created `:core:model` / `:core:database` / `:core:data` (and `:core:designsystem`) under the *old* package `com.google.codelabs.mdc.kotlin.shrine.{core.*, designsystem}`. This phase renames those too — it is the single place the whole codebase moves to the new package.

Checklist:
- **Gradle:** set `applicationId` **and** `namespace` in `:app/build.gradle` to `com.skystone1000.shrine`; set each module `namespace` to `com.skystone1000.shrine.{core.model, core.database, core.data, designsystem, feature.*}`.
- **Source:** move each module's source dir to the new package path and update every `package` / `import` declaration (prefer the IDE's *Refactor → Rename package*; otherwise a scripted find-replace of the package prefix across `*.kt`).
- **Manifest:** update the `application android:name` reference (it currently uses the fully-qualified legacy name); relative component refs like `.MainActivity` track the new `namespace` automatically.
- **DI / generated code:** Hilt (`@HiltAndroidApp`, components) and Room/KSP regenerate under the new package automatically once sources move.
- **Tests:** rename test source packages to match.
- _Out of scope:_ `shr_` resources, `Theme.Shrine`, and the display name (separate brand task).
- _Exit:_ `assembleDebug` + unit tests green, and `grep -r "com.google.codelabs.mdc.kotlin.shrine"` over the repo (excluding build output) returns **no** matches.

> **Status (2026-06-29): done.** All 8 source roots (`:app` main/test/androidTest, `:core:{model,database,data,designsystem}`) were moved from `…/com/google/codelabs/mdc/kotlin/shrine` to `…/com/skystone1000/shrine` via `git mv`, and every `package`/`import` plus the 5 module `namespace`s, the `applicationId`, and the manifest `application android:name` were rewritten to `com.skystone1000.shrine`. A `clean assembleDebug` + `:core:data:testDebugUnitTest` (5/5 pass) verified green; Hilt/Room/KSP regenerated under the new package; the built APK reports `package: name='com.skystone1000.shrine'`. The grep exit is satisfied for all **code/config/manifests**; the only remaining matches are *intentional historical references* in docs — this plan's own Phase-6 description, the `audit_*`/`BUG_INVENTORY.md`/`plan_7` snapshots (which document the code as it was at audit time), all of which keep the old name as their subject. Per the out-of-scope note, `shr_` resources, `Theme.Shrine`, and the display name are unchanged.

> **If a stable store identity is wanted sooner,** the `applicationId` alone can be flipped earlier (it is independent of the source package); the source/namespace rename is still best batched here to avoid churn while screens are in flight.

### Phase 7 — Quality gates
- **Unit:** ViewModels (Turbine for `StateFlow`), repositories, `PasswordHasher`, money math.
- **UI:** Compose tests for each screen; navigation test (auth→main, sign-out).
- **Screenshot:** Roborazzi/Paparazzi for the design-system gallery (light + dark) and key screens.
- **DB:** Room migration test.
- **CI:** GitHub Actions running `lint`, unit, and instrumented tests.

## Dependencies (indicative — pin to latest stable in the catalog)
Compose BOM, `material3`, `navigation-compose`, `hilt-android` + `hilt-navigation-compose`, `room-runtime`/`room-ktx` (KSP), `datastore-preferences`, `coil-compose`, `lifecycle-viewmodel-compose`, `kotlinx-serialization-json`, `kotlinx-coroutines`, `androidx.compose.ui:ui-test-junit4`, `turbine`, `roborazzi`.

## Risks & mitigations
- **Big-bang risk** → phased; Compose interop means old screens keep working until Phase 5.
- **Schema/migration** → migration tests; destructive fallback acceptable for the demo.
- **Price model change (String→cents)** → one-time data/seed migration; covered by money-math unit tests.
- **Scope creep (full e-commerce)** → Phase 4 table is the contract; backlog beyond it stays in [FEATURE_BACKLOG.md](FEATURE_BACKLOG.md).
- **minSdk 24** drops <1% legacy devices — accepted.
- **Package rename** (`…mdc.kotlin.shrine` → `…skystone1000.shrine`) is a wide mechanical refactor → done as one IDE-assisted pass in Phase 6 after legacy deletion (fewer files, stable tree), verified by a full-repo grep + green build/tests. Note: changing `applicationId` resets the installed-app identity (a fresh install, not an upgrade) — fine for the demo.

## Definition of done
Single-Activity Compose app shipped under applicationId/package **`com.skystone1000.shrine`**; the full figma screen set on the new stack with light/dark (15 designed screens + Addresses/Payment-methods management; placeholders rendered per the [coverage map](#ui-coverage-map-figma--plan)); Fragments/XML/Volley deleted; repositories + Hilt + DataStore in place; no references to the old `com.google.codelabs.mdc.kotlin.shrine` package remain; tests green in CI; docs (`ARCHITECTURE`, `CODEBASE`, `FEATURES`) updated to describe the Compose architecture.

See [design_brief_modernise.md](design_brief_modernise.md) for the UI generation prompt (design system + every screen).
