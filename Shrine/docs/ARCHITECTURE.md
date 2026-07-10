---
title: Architecture
last_updated: 2026-06-28
scope: System design, components, data/control flow, dependencies, and key design decisions for the Shrine Android app.
---

# Architecture

Shrine is an Android application written in Kotlin. It originates from the Google Material Components (MDC) "Shrine" codelab and has been extended with user authentication and a shopping cart backed by a local Room database. A modernisation to Jetpack Compose is in progress (see [plan_8_modernise.md](plan_8_modernise.md)); new modules (`:core:designsystem`, `:core:model`, `:core:database`, `:core:data`) exist alongside the original `:app`, which still hosts the live Fragment/XML UI.

> All paths below are relative to the `Shrine/` project directory unless noted.

## High-level shape

- **Gradle modules:** `:app`, `:core:designsystem`, `:core:model`, `:core:database`, `:core:data` (see [settings.gradle](../settings.gradle)). The `:core:*` modules are plan_8 modernisation scaffolding; the live UI is still the `:app` Fragment/XML stack and does not consume them yet (the legacy `models/`, `database/`, `auth/`, and `network/` packages in `:app` remain the app's runtime data layer until Phase 5).
- **Single-Activity architecture:** [`MainActivity`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/MainActivity.kt) is the only `Activity`. All screens are `Fragment`s swapped into a single `FrameLayout` container (`R.id.container`, defined in [shr_main_activity.xml](../app/src/main/res/layout/shr_main_activity.xml)).
- **Manual navigation:** there is no Jetpack Navigation component. Navigation is performed through the [`NavigationHost`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/NavigationHost.kt) interface, which `MainActivity` implements via `supportFragmentManager` transactions.
- **Local persistence:** a Room database named `contactDB` (version 3, [`ShrineDatabase`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/database/ShrineDatabase.kt)) stores users, products, and per-user cart items.
- **Session state:** the logged-in user's details are cached in `SharedPreferences` (per-Activity `getPreferences(MODE_PRIVATE)`), not in the database.

## Major components

| Component | Type | Responsibility |
|-----------|------|----------------|
| `ShrineApplication` | `Application` (`@HiltAndroidApp`) | Process entry point. Holds a static `instance` and enables vector-drawable support. Annotated `@HiltAndroidApp` (Hilt DI root, added in plan_8 Phase 0). Registered in the manifest as `android:name`. |
| `MainActivity` | `Activity` + `NavigationHost` | Hosts all fragments; performs fragment transactions; routes the toolbar cart/settings menu items to `CartFragment` / `SettingsFragment`. |
| `NavigationHost` | interface | Contract for "navigate to fragment (optionally add to back stack)". Decouples fragments/adapters from `MainActivity`. |
| Fragments (`fragments/`) | UI screens | One fragment per screen: Login, Register, ProductGrid, Cart, OrderPlaced, Profile, Settings. |
| RecyclerView adapters (`adapters/`) | UI binding | Bind product/cart lists to views and handle item taps (add to cart, remove from cart). |
| Models (`models/`) | Room `@Entity` data classes | `User`, `CartItem`, `Product` — table row shapes. |
| DAOs (`database/`) | Room `@Dao` interfaces | `UserDAO`, `CartItemDAO`, `ProductDAO` — database queries. |
| `ShrineDatabase` | Room `@Database` | Singleton database with the three DAOs. |
| `ImageRequester` | object | Loads remote product images over HTTP via Volley, with an in-memory `LruCache`. |
| `PasswordHasher` (`auth/`) | object | Salted PBKDF2 hashing/verification for the local account store. |
| `NavigationIconClickListener` | `View.OnClickListener` | Animates the product-grid "backdrop" reveal when the toolbar nav icon is tapped. |

## Control flow (navigation graph)

`MainActivity.onCreate` adds `LoginFragment` as the first screen. From there:

```
LoginFragment ──register──▶ RegisterFragment ──(on submit)──▶ LoginFragment
LoginFragment ──(valid login)──▶ ProductGridFragment
ProductGridFragment ──profile button──▶ ProfileFragment
ProductGridFragment ──toolbar cart icon──▶ CartFragment   (via MainActivity options menu)
ProductGridFragment ──toolbar settings icon──▶ SettingsFragment   (via MainActivity options menu)
CartFragment ──checkout / clear──▶ OrderPlacedFragment / CartFragment
OrderPlacedFragment ──continue shopping──▶ ProductGridFragment
ProfileFragment ──sign out──▶ restart MainActivity (clears task → LoginFragment)
```

Each hop calls `(<host> as NavigationHost).navigateTo(fragment, addToBackstack)` which runs `supportFragmentManager.beginTransaction().replace(R.id.container, fragment)` and optionally `addToBackStack`.

## Data flow

1. **Authentication.** `RegisterFragment` hashes the password (salted PBKDF2 via `PasswordHasher`) and writes a `User` row (`user_pass_hash` + `user_pass_salt`) via `UserDAO.insertUser`, rejecting duplicate emails. `LoginFragment` reads the user back with `UserDAO.getLogin(email)`, recomputes the hash of the entered password, and compares it to the stored hash. On success it caches `user_id`, `user_name`, `user_email`, `user_phone` in `SharedPreferences`.
2. **Catalog.** `ProductGridFragment` loads the catalog from the Room `products` table via `ProductDAO`, seeding it from `res/raw/products.json` (`ProductSeed`) on first run, and passes it to `ProductCardRecyclerViewAdapter`. Product images are attempted by URL through `ImageRequester`, falling back to a bundled `shr_logo` placeholder.
3. **Cart.** The cart is **scoped to the logged-in user** (`cart.user_id`, read from the session via `auth/Session`). Tapping a product card calls `CartItemDAO.addOrIncrement(userId, product)`: the first tap inserts a `CartItem` (quantity `"1"`), later taps increment that product's `product_quantity` — so the table holds **one row per product per user**. `CartFragment` loads the current user's rows with `CartItemDAO.getAll(userId)` and renders count + total price (currency-safe parsing); remove/clear/checkout are also user-scoped.
4. **Profile/session.** `ProfileFragment` reads the cached user fields from `SharedPreferences`. Sign-out clears those preferences and restarts the Activity.

## External dependencies & integrations

Declared in [app/build.gradle](../app/build.gradle); all versions are centralised in the **Gradle version catalog** [gradle/libs.versions.toml](../gradle/libs.versions.toml) (added in plan_8 Phase 0).

| Dependency | Version | Used for |
|------------|---------|----------|
| Material Components (`com.google.android.material`) | 1.6.0 | Theming, `TextInputLayout`, `MaterialButton`, toolbar, cards. |
| AndroidX Room (`room-runtime`, `room-ktx`, `room-compiler`) | 2.6.1 | Local SQLite persistence + DAOs (**KSP** annotation processing). |
| Kotlin Coroutines (`coroutines-core`, `coroutines-android`) | 1.9.0 | Off-main-thread DB calls. |
| Volley (`com.android.volley`) | 1.2.1 | `ImageRequester` HTTP image loading (to be replaced by Coil in plan_8 Phase 2). |
| Gson (`com.google.code.gson`) | 2.9.0 | JSON parsing of the bundled catalog in `ProductSeed` (seeds the `products` table). |

**Modernisation scaffolding (plan_8 Phase 0 — present but not yet wired into the live Fragment/XML UI):** Hilt (`hilt-android` 2.52, `hilt-navigation-compose` 1.2.0), Jetpack Compose (`compose-bom` 2024.12.01 → `ui`/`material3`, `activity-compose`, `lifecycle-viewmodel-compose` 2.8.7), Navigation-Compose 2.8.4, DataStore Preferences 1.1.1, Coil 3.0.4 (`coil-compose` + `coil-network-okhttp`), and `kotlinx-serialization-json` 1.7.3. The legacy `androidx.legacy:legacy-support-v4` dependency was **removed** (minSdk raised to 24).

**Design system module (`:core:designsystem`, plan_8 Phase 1).** A standalone `com.android.library` module exposing the Compose UI foundation: `ShrineTheme` (brand light/dark `ColorScheme`, `Typography`, `Shapes`, plus extended success/warning colors and spacing/elevation tokens — dynamic color is deliberately disabled), and the reusable component set (buttons, text/password/search fields, chips, selection controls, product card, price/rating/stepper, app bars, bottom nav, tabs, list rows, feedback dialogs/snackbar, and empty/loading-skeleton/error states). A `@Preview` gallery (`gallery/ComponentGallery.kt`) renders every component in light + dark. Color/type/spacing/shape tokens come 1:1 from the figma design doc.

**Data layer (`:core:model` · `:core:database` · `:core:data`, plan_8 Phase 2).** A clean Room + DataStore stack for the new app, packaged as `com.google.codelabs.mdc.kotlin.shrine.core.*`:
- `:core:model` — Room `@Entity` classes (`UserEntity`, `ProductEntity`, `CategoryEntity`, `PromotionEntity`, `CartItemEntity`, `OrderEntity`/`OrderLineEntity`, `WishlistItemEntity`, `AddressEntity`, `PaymentMethodEntity`, `RecentSearchEntity`) plus value types (`Variant`, `OrderStatus`, `DeliveryOption`, `ThemePreference`) and the `Money` cents formatter. **Prices are integer cents**, not strings.
- `:core:database` — `ShrineDatabase` (db file **`shrine.db`**, separate from the legacy `contactDB`), the DAOs, kotlinx-serialization `Converters`, the in-code `CatalogSeed`, and a Hilt `DatabaseModule`. KSP processes Room.
- `:core:data` — repository interfaces + impls (`Auth`, `Catalog`, `Cart`, `Order`, `Wishlist`, `Promotion`, `Search`, `Address`, `Payment`, plus DataStore-backed `Session` and `Settings`), the ported `PasswordHasher`, and Hilt `DataStoreModule` + `RepositoryModule`. Repositories expose `Flow`s + suspend functions; per-user data is scoped by a `userId` argument. Coil (added in Phase 0) replaces Volley image loading when the Compose screens arrive (Phase 4); the legacy `ImageRequester` stays until Phase 5. Repositories are unit-tested with an in-memory Room DB (Robolectric).

**Network use:** the only outbound network traffic is product-image loading via Volley to `storage.googleapis.com` / other image hosts. The manifest declares `INTERNET` and `ACCESS_NETWORK_STATE` permissions. There is no application backend/API — all app data is local.

## Build/toolchain architecture

| Layer | Version | Notes |
|-------|---------|-------|
| Gradle | 8.7 | [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties). Required for JDK 21 support. |
| Android Gradle Plugin | 8.6.1 | Declared in the version catalog, applied via the `plugins {}` block in [build.gradle](../build.gradle). |
| Kotlin | 2.0.21 | With the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`), KSP (`2.0.21-1.0.28`), and the serialization plugin. |
| compileSdk / targetSdk / minSdk | 35 / 35 / 24 | [app/build.gradle](../app/build.gradle). minSdk raised from 16 in plan_8 Phase 0. |
| Java / Kotlin JVM target | 17 | `compileOptions` + `kotlinOptions`. |

## Key design decisions (with rationale)

- **Single Activity + Fragments + `NavigationHost` interface.** Inherited from the MDC Shrine codelab. The `NavigationHost` indirection lets fragments and RecyclerView adapters trigger navigation without holding a concrete `MainActivity` reference (they cast the host/context to `NavigationHost`).
- **Room over raw SQLite.** Compile-time-checked queries and DAO abstraction; chosen when login/register/cart features were added on top of the codelab.
- **`SharedPreferences` for the session, DB for credentials.** The current user "session" is intentionally lightweight (just cached profile strings) and kept separate from the credential store. Sign-out clears preferences and relaunches the task to reset the back stack.
- **Backdrop reveal animation via `NavigationIconClickListener`.** A codelab-style Material "backdrop": the product grid sheet translates down on the Y-axis to reveal a menu behind it, toggled by the toolbar nav icon.
- **`vectorDrawables.useSupportLibrary = true` + `setCompatVectorFromResourcesEnabled(true)`.** Enables vector drawable assets (logo, icons, animated "done" check); originally needed for the old low `minSdk`, retained after the plan_8 Phase 0 bump to `minSdk` 24.

## Known issues & technical debt

These are accurate observations of the current code, surfaced so docs match reality (do not assume they are intentional design):

- ~~**Fragment transactions from background threads.**~~ _Resolved (plan_1_login + plan_2_cart): all `navigateTo(...)` calls — login, cart clear/checkout, and cart-item removal — now run on the main thread via lifecycle scopes._
- ~~**`GlobalScope` usage** for DB writes is not lifecycle-aware.~~ _Resolved: every coroutine now uses `viewLifecycleOwner.lifecycleScope` (fragments) or the host activity's `lifecycleScope` (adapters). No `GlobalScope` remains._
- ~~**Plaintext passwords.**~~ _Resolved (plan_4_security): passwords are stored as salted PBKDF2 hashes (`user_pass_hash` + `user_pass_salt`) and verified by hash; duplicate emails are rejected via a unique index._
- ~~**Hardcoded catalog.**~~ _Resolved (plan_3_catalog): the catalog is now seeded into the Room `products` table from `res/raw/products.json` and loaded via `ProductDAO`._
- ~~**Dead code paths.**~~ _Resolved: the staggered-grid adapter became active (plan_6_settings); the `products` table/`ProductDAO`/`products.json` became active (plan_3_catalog); and the last unused class, `network/ProductEntry.kt`, was removed. No dead code paths remain._
- ~~**Live image URLs may be stale.**~~ _Mitigated (plan_3_catalog): all known product image hosts return 404/000, so cards now render the bundled `shr_logo` placeholder via the `NetworkImageView` default/error image instead of appearing blank._

See [FEATURES.md](FEATURES.md) for per-feature flow and [CODEBASE.md](CODEBASE.md) for the file-by-file map.
