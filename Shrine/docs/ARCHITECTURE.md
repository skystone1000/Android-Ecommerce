---
title: Architecture
last_updated: 2026-06-29
scope: System design, components, data/control flow, dependencies, and key design decisions for the Shrine Android app.
---

# Architecture

Shrine is an Android application written in Kotlin. It originates from the Google Material Components (MDC) "Shrine" codelab and has been rebuilt onto a modern Jetpack stack (see [plan_8_modernise.md](plan_8_modernise.md)): 100% Jetpack Compose UI + MVVM + Hilt over a repository layer on Room + DataStore. As of plan_8 **Phase 5** the original Fragment/XML/Volley stack has been **deleted** — `:app` now contains only the Compose entry point and the `:core:*` modules hold the design system, model, database, and data layers.

> All paths below are relative to the `Shrine/` project directory unless noted.

## High-level shape

- **Gradle modules:** `:app`, `:core:designsystem`, `:core:model`, `:core:database`, `:core:data` (see [settings.gradle](../settings.gradle)). The `:app` Compose entry point (`ui/`) consumes `:core:designsystem` + `:core:data` (+ `:core:model`). After Phase 5 there is **no** legacy `models/`/`database/`/`auth/`/`network/`/`fragments/`/`adapters/` package in `:app` — that logic lives in the `:core:*` modules.
- **Single-Activity architecture:** [`MainActivity`](../app/src/main/java/com/skystone1000/shrine/MainActivity.kt) is the only `Activity` — a `ComponentActivity` (`@AndroidEntryPoint`) that `setContent { ShrineApp() }`, a 100% Compose host. No XML layouts remain.
- **Navigation:** [`ShrineApp`](../app/src/main/java/com/skystone1000/shrine/ui/ShrineApp.kt) hosts a **Navigation-Compose** `NavHost` with type-safe routes (kotlinx-serialization, see `ui/navigation/Routes.kt`) split into nested **auth** and **main** graphs, under a bottom-bar `Scaffold`.
- **Local persistence:** a Room database named **`shrine.db`** ([`ShrineDatabase`](../core/database/src/main/java/com/skystone1000/shrine/core/database/ShrineDatabase.kt) in `:core:database`) stores users, catalog (products/categories/promotions), per-user cart/wishlist/orders, addresses, payment methods, and recent searches, behind the `:core:data` repositories.
- **Session & settings state:** held in **DataStore Preferences** (`SessionRepository`, `SettingsRepository` in `:core:data`), not `SharedPreferences`.

## Major components

| Component | Type | Responsibility |
|-----------|------|----------------|
| `ShrineApplication` | `Application` (`@HiltAndroidApp`) | Process entry point and Hilt DI root. A bare shell after Phase 5 (the legacy static `instance` + vector-drawable shim were removed). Registered in the manifest as `android:name`. |
| `MainActivity` | `ComponentActivity` (`@AndroidEntryPoint`) | Compose host: `setContent { ShrineApp() }`. |
| `ShrineApp` (`ui/`) | `@Composable` | Root Compose UI: `ShrineTheme` (theme honors `SettingsRepository`) + `NavHost` (auth/main nested graphs, type-safe routes) + bottom-bar `Scaffold`. Every destination is a real screen (`ui/screens/`), each a stateless `XxxContent` + `@HiltViewModel` + `UiState` wired to a `:core:data` repository. `AppViewModel` drives the Splash → Auth/Main gate from `SessionRepository` and exposes the theme + sign-out. |
| Screen ViewModels (`ui/screens/`) | `@HiltViewModel` | One per screen (Login/Register, Home, Category, Search, ProductDetail, Cart, Checkout, Order*, Wishlist, Profile, EditProfile, Settings, Addresses, PaymentMethods). Expose an immutable `UiState` `StateFlow`; read type-safe nav args via `SavedStateHandle.toRoute<T>()`; scope per-user data by session `userId` (guests use `-1`). |
| Repositories (`:core:data`) | interfaces + impls | `Auth`, `Catalog`, `Cart`, `Order`, `Wishlist`, `Promotion`, `Search`, `Address`, `Payment`, plus DataStore-backed `Session` + `Settings`. Own the data sources; expose `Flow`s + suspend functions. |
| `ShrineDatabase` (`:core:database`) | Room `@Database` | Singleton (`shrine.db`) with all DAOs + kotlinx-serialization `Converters` + `CatalogSeed`. KSP-processed. |
| `PasswordHasher` (`:core:data`) | object | Salted PBKDF2 hashing/verification (ported from the legacy `auth/` package). |

## Control flow (navigation graph)

The app launches into the Compose `ShrineApp` `NavHost`: a `Splash` destination reads `SessionRepository` and routes to the **auth graph** or the **main graph**.

```
Splash ──no session──▶ AuthGraph:  Login ⇄ Register / ForgotPassword,  Login "Skip" ──▶ MainGraph (guest, userId = -1)
Splash ──session/guest──▶ MainGraph (bottom-bar Scaffold)

MainGraph bottom tabs:  Home · Search · Cart · Saved · Profile
  Home   ──▶ Category ──▶ ProductDetail ──▶ Cart
  Search ──▶ ProductDetail
  Cart   ──▶ Checkout ──▶ OrderPlaced ──▶ OrderHistory ──▶ OrderDetail
  Saved  ──▶ ProductDetail
  Profile──▶ OrderHistory / Addresses / PaymentMethods / EditProfile / Settings / HelpCenter
  Profile "Sign out" ──▶ AuthGraph (back stack cleared via popUpTo)
```

Navigation is type-safe: routes are `@Serializable` data classes/objects in `ui/navigation/Routes.kt`; sign-out/sign-in switch graphs with `popUpTo` rather than restarting the Activity.

## Data flow

Compose never touches Room/DataStore directly — screens render a `UiState`, ViewModels call repositories, repositories own the data sources and expose `Flow`s + suspend functions. `UiState` is immutable; screens are stateless and hoisted.

1. **Authentication.** `AuthRepository.register` hashes the password (salted PBKDF2 via `PasswordHasher`) and writes a `UserEntity` (`user_pass_hash` + `user_pass_salt`), rejecting duplicate emails; `login` reads the user back, recomputes the hash, and compares. On success the `LoginViewModel` calls `SessionRepository.signIn(...)` (DataStore). Login's **Skip** enters the main graph as a guest (`userId = -1`).
2. **Catalog.** `CatalogRepository` serves products/categories/promotions from `shrine.db`, seeded in-code by `CatalogSeed` on first run; Compose `ProductCard` loads images with Coil. Home/Category/Search ViewModels combine catalog + session + wishlist flows into their `UiState`.
3. **Cart & wishlist.** Both are **scoped to the session `userId`** (guests use `-1`). `CartRepository.addOrIncrement` upserts one row per product+variant per user; `CartViewModel` exposes lines + totals (integer-cent math) and quantity steppers. Wishlist toggles (`WishlistRepository`) drive the filled/outlined heart on catalog cards and the Saved tab reactively.
4. **Checkout & orders.** `CheckoutViewModel` combines address/delivery/payment/cart flows; `OrderRepository.placeOrder` persists an `OrderEntity` (+ `OrderLineEntity` snapshots) with an order number/status, which drives OrderPlaced and the OrderHistory status tabs.
5. **Profile/session/settings.** `ProfileViewModel` reads the session + order/saved counts; `SettingsRepository` (DataStore) holds theme/density/notification prefs, with the theme honored at the `ShrineTheme` root. Sign-out clears the session and switches graphs.

## External dependencies & integrations

Declared in [app/build.gradle](../app/build.gradle); all versions are centralised in the **Gradle version catalog** [gradle/libs.versions.toml](../gradle/libs.versions.toml) (added in plan_8 Phase 0).

**`:app` dependencies (post-Phase-5):** Hilt (`hilt-android` 2.52, `hilt-navigation-compose` 1.2.0; KSP-processed), Jetpack Compose (`compose-bom` 2024.12.01 → `ui`/`material3`, `activity-compose`, `lifecycle-viewmodel-compose` 2.8.7), Navigation-Compose 2.8.4, DataStore Preferences 1.1.1, Coil 3.0.4 (`coil-compose` + `coil-network-okhttp`), `kotlinx-serialization-json` 1.7.3, Kotlin Coroutines 1.9.0, and the three `:core:*` modules it consumes. Material Components (`com.google.android.material`) is retained **only** to supply the `Theme.Shrine` window theme on the Activity. **Removed in Phase 5:** Room (`room-runtime`/`ktx`/`compiler`), Volley, Gson, and `androidx.fragment:fragment-ktx` — Room now lives only in `:core:database`, and image loading is Coil. (`legacy-support-v4` was already dropped in Phase 0 with the minSdk-24 bump.)

**Design system module (`:core:designsystem`, plan_8 Phase 1).** A standalone `com.android.library` module exposing the Compose UI foundation: `ShrineTheme` (brand light/dark `ColorScheme`, `Typography`, `Shapes`, plus extended success/warning colors and spacing/elevation tokens — dynamic color is deliberately disabled), and the reusable component set (buttons, text/password/search fields, chips, selection controls, product card, price/rating/stepper, app bars, bottom nav, tabs, list rows, feedback dialogs/snackbar, and empty/loading-skeleton/error states). A `@Preview` gallery (`gallery/ComponentGallery.kt`) renders every component in light + dark. Color/type/spacing/shape tokens come 1:1 from the figma design doc.

**Data layer (`:core:model` · `:core:database` · `:core:data`, plan_8 Phase 2).** A clean Room + DataStore stack for the new app, packaged as `com.skystone1000.shrine.core.*`:
- `:core:model` — Room `@Entity` classes (`UserEntity`, `ProductEntity`, `CategoryEntity`, `PromotionEntity`, `CartItemEntity`, `OrderEntity`/`OrderLineEntity`, `WishlistItemEntity`, `AddressEntity`, `PaymentMethodEntity`, `RecentSearchEntity`) plus value types (`Variant`, `OrderStatus`, `DeliveryOption`, `ThemePreference`) and the `Money` cents formatter. **Prices are integer cents**, not strings.
- `:core:database` — `ShrineDatabase` (db file **`shrine.db`**, separate from the legacy `contactDB`), the DAOs, kotlinx-serialization `Converters`, the in-code `CatalogSeed`, and a Hilt `DatabaseModule`. KSP processes Room.
- `:core:data` — repository interfaces + impls (`Auth`, `Catalog`, `Cart`, `Order`, `Wishlist`, `Promotion`, `Search`, `Address`, `Payment`, plus DataStore-backed `Session` and `Settings`), the ported `PasswordHasher`, and Hilt `DataStoreModule` + `RepositoryModule`. Repositories expose `Flow`s + suspend functions; per-user data is scoped by a `userId` argument (guests use `-1`). Image loading is **Coil** in the Compose layer. Repositories are unit-tested with an in-memory Room DB (Robolectric).

**Network use:** the only outbound network traffic is product-image loading via **Coil** to the catalog's image hosts. The manifest declares `INTERNET` and `ACCESS_NETWORK_STATE` permissions. There is no application backend/API — all app data is local.

## Build/toolchain architecture

| Layer | Version | Notes |
|-------|---------|-------|
| Gradle | 8.7 | [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties). Required for JDK 21 support. |
| Android Gradle Plugin | 8.6.1 | Declared in the version catalog, applied via the `plugins {}` block in [build.gradle](../build.gradle). |
| Kotlin | 2.0.21 | With the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`), KSP (`2.0.21-1.0.28`), and the serialization plugin. |
| compileSdk / targetSdk / minSdk | 35 / 35 / 24 | [app/build.gradle](../app/build.gradle). minSdk raised from 16 in plan_8 Phase 0. |
| Java / Kotlin JVM target | 17 | `compileOptions` + `kotlinOptions`. |

## Key design decisions (with rationale)

- **Single-Activity + 100% Compose + Navigation-Compose (type-safe routes).** Replaces the codelab's Fragment/`NavigationHost`/backdrop approach (deleted in Phase 5). Navigation is driven by `@Serializable` routes; no `FragmentManager`.
- **MVVM + Hilt + unidirectional data flow.** ViewModels expose immutable `UiState` `StateFlow`s; screens are stateless. Layering rule: Compose → ViewModel → repository; nothing else touches Room/DataStore.
- **Room behind a repository layer (`:core:data`).** Compile-time-checked queries + DAO abstraction, isolated from the UI so screens depend only on repository interfaces. Prices are integer cents.
- **DataStore for session + settings.** Replaces per-Activity `SharedPreferences`; sign-out clears the session and switches nav graphs (no Activity restart). Per-user data is scoped by `userId` (guests `-1`).
- **Coil for image loading.** Replaces Volley/`ImageRequester`.

## Known issues & technical debt

These are accurate observations of the current code, surfaced so docs match reality (do not assume they are intentional design):

- **On-device instrumented tests are deferred.** Phase 7 implemented the quality gates as a **44-test JVM suite** (`testDebugUnitTest`): repository / `PasswordHasher` / money-math unit tests, Turbine-based ViewModel tests, Robolectric Compose UI tests, a Roborazzi design-system screenshot gate, and a Room DB integrity test — all wired into `.github/workflows/ci.yml`. True on-device (`androidTest`) Compose/navigation runs remain deferred (they'd need an emulator in CI); the equivalent assertions run under Robolectric.
- **Package rename done (Phase 6).** Source/`namespace`/`applicationId` are now `com.skystone1000.shrine` (modules under `com.skystone1000.shrine.{core.*, designsystem}`). The `shr_` resource prefix, `Theme.Shrine` style name, and the display name are intentionally unchanged (separate brand task).
- **Product images may not resolve.** The seeded catalog uses placeholder/remote image URLs that may 404; Coil falls back to its placeholder. (Historical resolved items — plaintext passwords, hardcoded catalog, `GlobalScope`, dead code — were fixed in plan_1–plan_7 and then carried into `:core:*`; the Fragment code that hosted them was deleted in Phase 5.)

See [FEATURES.md](FEATURES.md) for per-feature flow and [CODEBASE.md](CODEBASE.md) for the file-by-file map.
