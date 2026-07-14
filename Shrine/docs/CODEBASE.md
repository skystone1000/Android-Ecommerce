---
title: Codebase Map
last_updated: 2026-06-29
scope: Directory tree, module responsibilities, entry points, build/run commands, conventions, and "where to look" recipes.
---

# Codebase Map

The Android project lives in the `Shrine/` directory of the repository (the repo root contains only `README.md`, `CLAUDE.md`, and `Shrine/`). All paths below are relative to `Shrine/`.

## Directory tree & responsibilities

```
Shrine/
├── build.gradle                 # Root Gradle: declares plugins (AGP 8.6.1, Kotlin 2.0.21, KSP, Hilt) via plugins {} + version catalog
├── settings.gradle              # pluginManagement + dependencyResolutionManagement (repos); include ':app'
├── gradle.properties            # AndroidX + Jetifier flags, JVM args
├── gradle/libs.versions.toml    # Version catalog: all plugin/library versions
├── gradle/wrapper/...           # Gradle 8.7 wrapper
├── local.properties             # sdk.dir (machine-local, not committed)
├── docs/                        # ← documentation (this folder)
├── core/                        # plan_8 modernisation modules (com.android.library); consumed by the live Compose UI in :app
│   ├── designsystem/            # Phase 1: Compose design system
│   │   └── src/main/java/.../designsystem/
│   │       ├── theme/           # Color, Type, Shape, Dimens, ExtendedColors, ShrineTheme
│   │       ├── component/       # Buttons, TextFields, Selection, Chips, Product, Bars, Lists, Feedback, States
│   │       └── gallery/         # ComponentGallery.kt (@Preview light + dark — Phase 1 exit gate)
│   ├── model/                   # Phase 2: Room @Entity classes + value types (Variant, enums, Money)
│   ├── database/                # Phase 2: ShrineDatabase (shrine.db), DAOs, Converters, CatalogSeed, DatabaseModule
│   └── data/                    # Phase 2: repositories, Session/Settings (DataStore), PasswordHasher, Hilt modules
│       └── src/test/            # Robolectric repository tests (in-memory Room) — Phase 2 exit gate
└── app/
    ├── build.gradle             # Module Gradle: SDKs (compile/target 35, min 24), deps (Hilt+KSP, Compose, Coil, DataStore, Nav-Compose, coroutines, Material window theme). No Room/Volley/Gson/Fragment (removed Phase 5)
    ├── proguard-rules.pro       # ProGuard rules (release; minify disabled)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml         # Permissions, Application, MainActivity (LAUNCHER)
        │   ├── java/com/skystone1000/shrine/
        │   │   ├── MainActivity.kt             # Phase 3: ComponentActivity → setContent { ShrineApp() }
        │   │   ├── ui/                          # Compose entry point (Phase 3 nav skeleton + Phase 4 screens)
        │   │   │   ├── ShrineApp.kt             #   NavHost (auth/main graphs) + bottom-bar Scaffold; theme from settings
        │   │   │   ├── AppViewModel.kt          #   session gate (Splash→Auth/Main), theme preference, sign-out
        │   │   │   ├── UiHelpers.kt             #   category-icon map, initials, greeting, session scopeId
        │   │   │   ├── navigation/Routes.kt     #   @Serializable type-safe destinations
        │   │   │   └── screens/                 #   Phase 4: stateless XxxContent + @HiltViewModel + UiState + @Preview per destination
        │   │   │       ├── PreviewData.kt       #     static sample entities for @Preview (no Hilt/DB)
        │   │   │       ├── AuthScreens.kt       #     Login / Register / ForgotPassword (Auth + Session)
        │   │   │       ├── HomeScreen.kt        #     greeting, hero promo, category tiles, product grid
        │   │   │       ├── CategoryScreen.kt    #     products by category + sort + result count
        │   │   │       ├── SearchScreen.kt      #     live results, recent searches, filter bottom-sheet
        │   │   │       ├── ProductDetailScreen.kt #   image pager, variants, wishlist, related, add-to-cart
        │   │   │       ├── CartScreen.kt        #     steppers, summary, sticky checkout
        │   │   │       ├── CheckoutScreen.kt    #     address, delivery options, payment, place order
        │   │   │       ├── OrderScreens.kt      #     OrderPlaced / OrderHistory (status tabs) / OrderDetail
        │   │   │       ├── WishlistScreen.kt    #     saved grid, quick-add, remove
        │   │   │       ├── ProfileScreen.kt     #     avatar + stats + nav rows + sign out
        │   │   │       ├── EditProfileScreen.kt #     name/email/phone/DOB (DatePicker), save
        │   │   │       ├── SettingsScreen.kt    #     theme segmented control, switches, about
        │   │   │       ├── AddressesScreen.kt   #     CRUD over AddressRepository
        │   │   │       ├── PaymentMethodsScreen.kt # CRUD over PaymentRepository (masked cards)
        │   │   │       ├── HelpCenterScreen.kt  #     static placeholder
        │   │   │       └── SkeletonScreens.kt   #     SplashScreen (Phase 3 stubs removed)
        │   │   └── application/
        │   │       └── ShrineApplication.kt     # Application subclass (@HiltAndroidApp) — bare shell
        │   └── res/
        │       ├── drawable/ + drawable-v24/   # Launcher background/foreground only
        │       ├── mipmap-*/      # Launcher icons
        │       └── values/        # colors.xml, dimens.xml, strings.xml, styles.xml (Theme.Shrine window theme)
        ├── test/                  # ExampleUnitTest.kt (template only)
        └── androidTest/           # ExampleInstrumentedTest.kt (template only)
```

> **Phase 5 (2026-06-29) deleted the entire legacy stack** from `:app`: `fragments/`, `adapters/` (linear + staggered RecyclerView), `database/` (the app-local Room DB `contactDB` + DAOs + `ProductSeed`), `models/` (`User`/`CartItem`/`Product` entities), `network/ImageRequester` (Volley), `auth/` (`PasswordHasher` + `Session`), `NavigationHost.kt`, `NavigationIconClickListener.kt`, every `res/layout/*`, `res/menu/`, `res/animator/`, `res/raw/products.json`, and all legacy `shr_*` drawables. The catalog/auth/cart logic and data now live in `:core:data` + `:core:database` + `:core:model`; the only `:app` Kotlin left is `MainActivity`, `ShrineApplication`, and the Compose `ui/` package.

## Entry points

1. **Process:** `ShrineApplication` (manifest `android:name`; `@HiltAndroidApp`) — a bare Hilt shell.
2. **UI:** `MainActivity` — the `LAUNCHER` activity; `onCreate` calls `setContent { ShrineApp() }` (Compose).
3. **First screen seen by the user:** the Compose `Splash` destination in `ShrineApp`, which routes to `Login` (no session) or `Home` (session/guest).

## Build & run commands

Run from the `Shrine/` directory. Requires a JDK **17–21** (Android Studio's bundled JBR 21 works) and an Android SDK with platform **35** + build-tools 35 installed.

```bash
# From repo root:
cd Shrine

# Build a debug APK
./gradlew assembleDebug

# Install onto a running emulator/device
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
$ANDROID_HOME/platform-tools/adb shell am start -n \
  com.skystone1000.shrine/.MainActivity

# Unit tests (template only) / lint
./gradlew testDebugUnitTest
```

In Android Studio: open the `Shrine/` folder, set **Gradle JDK** to the bundled JBR (17–21), then Run `app`.

The APK's application id and launch component is `com.skystone1000.shrine`.

## Naming & code conventions

- **Package:** everything is under `com.skystone1000.shrine` (the namespace set in `app/build.gradle`; modules use `com.skystone1000.shrine.{core.*, designsystem}`). Renamed from the codelab's `com.google.codelabs.mdc.kotlin.shrine` in plan_8 Phase 6.
- **Resource prefix:** the only `shr_`-prefixed resource still present is `Theme.Shrine` (`styles.xml`) + `shr_app_name`/other strings; the `shr_*.xml` layouts/menus/drawables were removed in Phase 5. New UI is Compose and carries no XML resources.
- **UI:** screens are stateless `@Composable`s in `ui/screens/` (`findViewById`/`kotlinx.android.synthetic` no longer apply — there are no XML views in `:app`).
- **Models:** entity fields use `snake_case` (e.g. `user_email`, `product_price`) to match column names; entities now live in `:core:model`.
- **Async:** Coroutines + Flow end-to-end; ViewModels use `viewModelScope`, repositories own `withContext(Dispatchers.IO)`. Do not use `GlobalScope`.
- **Styles/theme:** the app **window** theme is the Material Components `Theme.Shrine` (`styles.xml`), used only as the Activity background before Compose draws; the live in-app theme is the Compose `ShrineTheme` (`:core:designsystem`). Legacy palette in `colors.xml`.

## Where to look to do common tasks

| Task | Start here |
|------|-----------|
| Add/modify a screen | Add/edit a `XxxScreen.kt` in `app/.../ui/screens/` (stateless `XxxContent` + `@HiltViewModel` + `UiState` + `@Preview`); register the route in `ui/navigation/Routes.kt` + `ui/ShrineApp.kt`. |
| Change navigation between screens | `ui/ShrineApp.kt` (`NavHost`, type-safe `navigate(...)` calls) and `ui/navigation/Routes.kt`. |
| Add a DB table/query | Add an `@Entity` in `:core:model`, a DAO in `:core:database`, register both in `ShrineDatabase` (and bump `version`); expose via a repository in `:core:data`. |
| Change the product catalog | `:core:database` `CatalogSeed` (programmatic seed) → read via `CatalogRepository` in `:core:data`. |
| Change cart logic/totals | `CartRepository` (`:core:data`) + `ui/screens/CartScreen.kt` (`CartViewModel`). |
| Change login/registration rules | `AuthRepository` (`:core:data`, hashing via ported `PasswordHasher`) + `ui/screens/AuthScreens.kt`. |
| Change product image loading | Coil in the Compose `ProductCard`/`AsyncImage` (`:core:designsystem`); Volley/`ImageRequester` is gone. |
| Change colors/theme | Compose tokens in `:core:designsystem` `theme/`; the Activity window theme in `res/values/styles.xml`. |
| Change settings (theme/density/notifications) | `SettingsRepository` (DataStore, `:core:data`) + `ui/screens/SettingsScreen.kt`. |
| Change build config / dependencies | `app/build.gradle` (module) and `build.gradle` (root); bump versions in `gradle/libs.versions.toml` (version catalog). |

See [FEATURES.md](FEATURES.md) for end-to-end feature behavior and [ARCHITECTURE.md](ARCHITECTURE.md) for the system design.
