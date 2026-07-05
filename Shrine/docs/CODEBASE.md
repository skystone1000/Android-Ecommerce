---
title: Codebase Map
last_updated: 2026-06-27
scope: Directory tree, module responsibilities, entry points, build/run commands, conventions, and "where to look" recipes.
---

# Codebase Map

The Android project lives in the `Shrine/` directory of the repository (the repo root contains only `README.md`, `CLAUDE.md`, and `Shrine/`). All paths below are relative to `Shrine/`.

## Directory tree & responsibilities

```
Shrine/
├── build.gradle                 # Root Gradle: AGP 8.5.2, Kotlin 1.9.24, repos
├── settings.gradle              # include ':app'
├── gradle.properties            # AndroidX + Jetifier flags, JVM args
├── gradle/wrapper/...           # Gradle 8.7 wrapper
├── local.properties             # sdk.dir (machine-local, not committed)
├── docs/                        # ← documentation (this folder)
└── app/
    ├── build.gradle             # Module Gradle: SDKs, deps (Room, Volley, Gson, coroutines)
    ├── proguard-rules.pro       # ProGuard rules (release; minify disabled)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml         # Permissions, Application, MainActivity (LAUNCHER)
        │   ├── java/com/google/codelabs/mdc/kotlin/shrine/
        │   │   ├── MainActivity.kt             # Single Activity, NavigationHost impl
        │   │   ├── NavigationHost.kt           # navigateTo(...) interface
        │   │   ├── NavigationIconClickListener.kt  # Backdrop reveal animation
        │   │   ├── application/
        │   │   │   └── ShrineApplication.kt     # Application subclass, static instance
        │   │   ├── auth/
        │   │   │   └── PasswordHasher.kt         # Salted PBKDF2 hashing/verification
        │   │   ├── fragments/                   # One file per screen (see below)
        │   │   ├── adapters/
        │   │   │   ├── lineargridlayout/        # Regular-grid RecyclerView adapters
        │   │   │   └── staggeredgridlayout/     # Staggered-grid adapter (used when the setting is on)
        │   │   ├── database/                    # Room @Database + @Dao interfaces
        │   │   ├── models/                      # Room @Entity data classes
        │   │   └── network/                     # ImageRequester (Volley image loading)
        │   └── res/
        │       ├── layout/        # XML screens & list-item layouts (shr_*.xml)
        │       ├── drawable/ + drawable-v24/   # Vectors, icons, animated-vector "done"
        │       ├── menu/shr_toolbar_menu.xml   # Toolbar settings + cart icons
        │       ├── mipmap-*/      # Launcher icons
        │       ├── animator/      # Button state-list animator
        │       ├── raw/products.json           # Product catalog seed (loaded by ProductSeed)
        │       └── values/        # colors.xml, dimens.xml, strings.xml, styles.xml
        ├── test/                  # ExampleUnitTest.kt (template only)
        └── androidTest/           # ExampleInstrumentedTest.kt (template only)
```

### `fragments/` — one fragment per screen

| File | Screen | Notes |
|------|--------|-------|
| `LoginFragment.kt` | Login | "Username" field is matched against `user_email`. |
| `RegisterFragment.kt` | Sign-up | Validates 6 fields + password rules, inserts a `User`. |
| `ProductGridFragment.kt` | Product catalog | Hosts the toolbar/backdrop; hardcoded product list. |
| `CartFragment.kt` | Cart | Loads + regroups cart rows; clear/checkout. |
| `OrderPlacedFragment.kt` | Order confirmation | Animated "done" check; continue shopping. |
| `ProfileFragment.kt` | Profile | Reads session from `SharedPreferences`; sign-out. |
| `SettingsFragment.kt` | Settings | Toggle for staggered vs regular product grid; persists to `shrine_settings` prefs. |

### `database/`, `models/`, `adapters/`, `network/`

- **`models/`**: `User` (`users` table; stores `user_pass_hash` + `user_pass_salt`, with a unique index on `user_email`), `CartItem` (`cart` table), `Product` (`products` table). All are Room `@Entity` data classes with an auto-generated primary key.
- **`database/`**: `ShrineDatabase` (singleton, db file `contactDB`, **version 2**, `fallbackToDestructiveMigration`). `UserDAO` (insert/getLogin), `CartItemDAO` (insert/update/getAll/clearCart/deleteCartItem), `ProductDAO` (insertAll/getAll/count — backs the product grid), and `ProductSeed` (parses `res/raw/products.json` to seed the `products` table on first run).
- **`adapters/lineargridlayout/`**: `ProductCardRecyclerViewAdapter` (grid of products → tap adds to cart) and `CartRecyclerViewAdapter` (cart rows → tap remove deletes item). **Active.**
- **`adapters/staggeredgridlayout/`**: `StaggeredProductCardRecyclerViewAdapter` + `StaggeredProductCardViewHolder`. **Active** — used by `ProductGridFragment` when the "Staggered product grid" setting is on (asymmetric layout, `Product`-backed, with add-to-cart).
- **`network/`**: `ImageRequester` (Volley image loading; **active**, used by the adapters).
- **`auth/`**: `PasswordHasher` (salted PBKDF2 hashing/verification for login + registration).

## Entry points

1. **Process:** `ShrineApplication.onCreate` (manifest `android:name`).
2. **UI:** `MainActivity` — the `LAUNCHER` activity. `onCreate` adds `LoginFragment` to `R.id.container`.
3. **First screen seen by the user:** `LoginFragment`.

## Build & run commands

Run from the `Shrine/` directory. Requires a JDK 17–21 and an Android SDK with platforms **33 and 34** + build-tools 33 installed.

```bash
# From repo root:
cd Shrine

# Build a debug APK
./gradlew assembleDebug

# Install onto a running emulator/device
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
$ANDROID_HOME/platform-tools/adb shell am start -n \
  com.google.codelabs.mdc.kotlin.shrine/.MainActivity

# Unit tests (template only) / lint
./gradlew testDebugUnitTest
```

In Android Studio: open the `Shrine/` folder, set **Gradle JDK** to the bundled JBR (17–21), then Run `app`.

The APK's application id and launch component is `com.google.codelabs.mdc.kotlin.shrine`.

## Naming & code conventions

- **Package:** everything is under `com.google.codelabs.mdc.kotlin.shrine` (the namespace set in `app/build.gradle`).
- **Resource prefix:** most XML resources use the `shr_` prefix (`shr_login_fragment.xml`, `shr_toolbar_menu.xml`, `Theme.Shrine`, `Widget.Shrine.*`). Some newer additions do not (e.g. `baseline_shopping_cart_24.xml`, `delete_24.xml`).
- **Layout ↔ view IDs:** views are accessed with `findViewById(R.id.<snake_case_id>)`. (This project was migrated off `kotlin-android-extensions` synthetics; do **not** reintroduce `kotlinx.android.synthetic` imports — they are removed in Kotlin 1.9.)
- **Models:** entity fields use `snake_case` (e.g. `user_email`, `product_price`) to match column names.
- **Async:** DB calls use Kotlin coroutines scoped to a lifecycle — `viewLifecycleOwner.lifecycleScope` in fragments, `(context as AppCompatActivity).lifecycleScope` in adapters — with `withContext(Dispatchers.IO)` for the DB work. Do not use `GlobalScope` (none remains).
- **Styles/theme:** Material Components light theme `Theme.Shrine` (`styles.xml`); palette in `colors.xml` (primary `#E0E0E0`, accent/pink `#FEDBD0`, text `#442C2E`).

## Where to look to do common tasks

| Task | Start here |
|------|-----------|
| Add/modify a screen | Create a `Fragment` in `fragments/` + a `shr_*.xml` layout; navigate via `navigateTo(...)`. |
| Change navigation between screens | The `navigateTo(...)` call sites in fragments/adapters; `MainActivity.navigateTo`. |
| Add a DB table/query | Add an `@Entity` in `models/`, a DAO in `database/`, register both in `ShrineDatabase` (and bump `version`). |
| Change the product catalog | `res/raw/products.json` (seed data) → loaded by `ProductSeed` into the `products` table; read in `ProductGridFragment.loadCatalog()` via `ProductDAO`. |
| Change cart logic/totals | `CartFragment` (load + regroup + totals) and `CartRecyclerViewAdapter` (per-row remove). |
| Change login/registration rules | `LoginFragment.isPasswordValid` / `login`; `RegisterFragment.check` / `userRegister`. Password hashing lives in `auth/PasswordHasher`. |
| Change product image loading | `network/ImageRequester`. |
| Change toolbar items | `res/menu/shr_toolbar_menu.xml` + `ProductGridFragment.onCreateOptionsMenu` + `MainActivity.onOptionsItemSelected`. |
| Change colors/theme/strings | `res/values/colors.xml`, `styles.xml`, `strings.xml`. |
| Change settings / grid layout toggle | `SettingsFragment` (+ `shr_settings_fragment.xml`); applied in `ProductGridFragment.renderGrid()`. |
| Change build config / dependencies | `app/build.gradle` (module) and `build.gradle` (root). |

See [FEATURES.md](FEATURES.md) for end-to-end feature behavior and [ARCHITECTURE.md](ARCHITECTURE.md) for the system design.
