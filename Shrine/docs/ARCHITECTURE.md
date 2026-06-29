---
title: Architecture
last_updated: 2026-06-27
scope: System design, components, data/control flow, dependencies, and key design decisions for the Shrine Android app.
---

# Architecture

Shrine is a single-module Android application written in Kotlin. It originates from the Google Material Components (MDC) "Shrine" codelab and has been extended with user authentication and a shopping cart backed by a local Room database.

> All paths below are relative to the `Shrine/` project directory unless noted.

## High-level shape

- **One Gradle module:** `:app` (see [settings.gradle](../settings.gradle)).
- **Single-Activity architecture:** [`MainActivity`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/MainActivity.kt) is the only `Activity`. All screens are `Fragment`s swapped into a single `FrameLayout` container (`R.id.container`, defined in [shr_main_activity.xml](../app/src/main/res/layout/shr_main_activity.xml)).
- **Manual navigation:** there is no Jetpack Navigation component. Navigation is performed through the [`NavigationHost`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/NavigationHost.kt) interface, which `MainActivity` implements via `supportFragmentManager` transactions.
- **Local persistence:** a Room database named `contactDB` ([`ShrineDatabase`](../app/src/main/java/com/google/codelabs/mdc/kotlin/shrine/database/ShrineDatabase.kt)) stores users and cart items.
- **Session state:** the logged-in user's details are cached in `SharedPreferences` (per-Activity `getPreferences(MODE_PRIVATE)`), not in the database.

## Major components

| Component | Type | Responsibility |
|-----------|------|----------------|
| `ShrineApplication` | `Application` | Process entry point. Holds a static `instance` and enables vector-drawable support. Registered in the manifest as `android:name`. |
| `MainActivity` | `Activity` + `NavigationHost` | Hosts all fragments; performs fragment transactions; routes the toolbar cart menu item to `CartFragment`. |
| `NavigationHost` | interface | Contract for "navigate to fragment (optionally add to back stack)". Decouples fragments/adapters from `MainActivity`. |
| Fragments (`fragments/`) | UI screens | One fragment per screen: Login, Register, ProductGrid, Cart, OrderPlaced, Profile. |
| RecyclerView adapters (`adapters/`) | UI binding | Bind product/cart lists to views and handle item taps (add to cart, remove from cart). |
| Models (`models/`) | Room `@Entity` data classes | `User`, `CartItem`, `Product` — table row shapes. |
| DAOs (`database/`) | Room `@Dao` interfaces | `UserDAO`, `CartItemDAO`, `PrductDAO` — database queries. |
| `ShrineDatabase` | Room `@Database` | Singleton database with the three DAOs. |
| `ImageRequester` | object | Loads remote product images over HTTP via Volley, with an in-memory `LruCache`. |
| `NavigationIconClickListener` | `View.OnClickListener` | Animates the product-grid "backdrop" reveal when the toolbar nav icon is tapped. |

## Control flow (navigation graph)

`MainActivity.onCreate` adds `LoginFragment` as the first screen. From there:

```
LoginFragment ──register──▶ RegisterFragment ──(on submit)──▶ LoginFragment
LoginFragment ──(valid login)──▶ ProductGridFragment
ProductGridFragment ──profile button──▶ ProfileFragment
ProductGridFragment ──toolbar cart icon──▶ CartFragment   (via MainActivity options menu)
CartFragment ──checkout / clear──▶ OrderPlacedFragment / CartFragment
OrderPlacedFragment ──continue shopping──▶ ProductGridFragment
ProfileFragment ──sign out──▶ restart MainActivity (clears task → LoginFragment)
```

Each hop calls `(<host> as NavigationHost).navigateTo(fragment, addToBackstack)` which runs `supportFragmentManager.beginTransaction().replace(R.id.container, fragment)` and optionally `addToBackStack`.

## Data flow

1. **Authentication.** `RegisterFragment` writes a `User` row via `UserDAO.insertUser`. `LoginFragment` reads it back with `UserDAO.getLogin(email)` and compares the stored `user_pass` to the entered password. On success it caches `user_id`, `user_name`, `user_email`, `user_phone` in `SharedPreferences`.
2. **Catalog.** `ProductGridFragment` builds a **hardcoded** in-memory `List<Product>` (17 items) and passes it to `ProductCardRecyclerViewAdapter`. Product images are fetched by URL through `ImageRequester`.
3. **Cart.** Tapping a product card inserts a `CartItem` (quantity `"1"`) via `CartItemDAO.insertCartItem`. `CartFragment` loads all cart rows with `CartItemDAO.getAll()`, then **regroups duplicate `product_id`s in memory** to compute per-product quantities, and renders count + total price.
4. **Profile/session.** `ProfileFragment` reads the cached user fields from `SharedPreferences`. Sign-out clears those preferences and restarts the Activity.

## External dependencies & integrations

Declared in [app/build.gradle](../app/build.gradle):

| Dependency | Version | Used for |
|------------|---------|----------|
| Material Components (`com.google.android.material`) | 1.6.0 | Theming, `TextInputLayout`, `MaterialButton`, toolbar, cards. |
| AndroidX Room (`room-runtime`, `room-ktx`, `room-compiler`) | 2.6.1 | Local SQLite persistence + DAOs (kapt annotation processing). |
| Kotlin Coroutines (`coroutines-core`, `coroutines-android`) | 1.6.4 | Off-main-thread DB calls. |
| Volley (`com.android.volley`) | 1.2.1 | `ImageRequester` HTTP image loading. |
| Gson (`com.google.code.gson`) | 2.9.0 | JSON parsing in `ProductEntry.initProductEntryList` (see dead-code note). |
| `androidx.legacy:legacy-support-v4` | 1.0.0 | Legacy support libs. |

**Network use:** the only outbound network traffic is product-image loading via Volley to `storage.googleapis.com` / other image hosts. The manifest declares `INTERNET` and `ACCESS_NETWORK_STATE` permissions. There is no application backend/API — all app data is local.

## Build/toolchain architecture

| Layer | Version | Notes |
|-------|---------|-------|
| Gradle | 8.7 | [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties). Required for JDK 21 support. |
| Android Gradle Plugin | 8.5.2 | [build.gradle](../build.gradle). |
| Kotlin | 1.9.24 | `ext.kotlin_version`. |
| compileSdk / targetSdk / minSdk | 34 / 33 / 16 | [app/build.gradle](../app/build.gradle). |
| Java / Kotlin JVM target | 17 | `compileOptions` + `kotlinOptions`. |

## Key design decisions (with rationale)

- **Single Activity + Fragments + `NavigationHost` interface.** Inherited from the MDC Shrine codelab. The `NavigationHost` indirection lets fragments and RecyclerView adapters trigger navigation without holding a concrete `MainActivity` reference (they cast the host/context to `NavigationHost`).
- **Room over raw SQLite.** Compile-time-checked queries and DAO abstraction; chosen when login/register/cart features were added on top of the codelab.
- **`SharedPreferences` for the session, DB for credentials.** The current user "session" is intentionally lightweight (just cached profile strings) and kept separate from the credential store. Sign-out clears preferences and relaunches the task to reset the back stack.
- **Backdrop reveal animation via `NavigationIconClickListener`.** A codelab-style Material "backdrop": the product grid sheet translates down on the Y-axis to reveal a menu behind it, toggled by the toolbar nav icon.
- **`vectorDrawables.useSupportLibrary = true` + `setCompatVectorFromResourcesEnabled(true)`.** Enables vector drawable assets (logo, icons, animated "done" check) down to the low `minSdk` (16).

## Known issues & technical debt

These are accurate observations of the current code, surfaced so docs match reality (do not assume they are intentional design):

- **Fragment transactions from background threads.** `LoginFragment.userLogin`, `CartFragment`, and `CartRecyclerViewAdapter` call `navigateTo(...)` from inside `Dispatchers.IO` / `GlobalScope` coroutines. UI/fragment transactions should run on the main thread; this is fragile.
- **`GlobalScope` usage** for DB writes (add to cart, register, remove item) is not lifecycle-aware.
- **Plaintext passwords.** `User.user_pass` is stored and compared in plaintext; failed logins are silent (the `else` branch is commented out).
- **Hardcoded catalog.** `ProductGridFragment` ignores the Room `products` table and the bundled `products.json`; it uses an inline `List<Product>`.
- **Dead code paths.** `PrductDAO`/`products` table, `ProductEntry` + `R.raw.products` (`products.json`), and the staggered-grid adapters (`StaggeredProductCardRecyclerViewAdapter`, `StaggeredProductCardViewHolder`) are defined but never reached from the active UI flow. `PrductDAO.getAll()`/`clearCart()` also query the `cart` table, not `products`.
- **Live image URLs may be stale.** Product image URLs (both the hardcoded list's `dummyjson.com` URLs and `products.json`'s `material-vignettes` URLs) may no longer resolve, so product images can render blank.

See [FEATURES.md](FEATURES.md) for per-feature flow and [CODEBASE.md](CODEBASE.md) for the file-by-file map.
