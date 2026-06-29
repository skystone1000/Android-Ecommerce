# Android-Ecommerce (Shrine)

A small Android e-commerce demo app, in Kotlin, with **login / sign-up**, a **product catalog**, and a **shopping cart** backed by a local Room database. It builds on Google's Material Components "Shrine" codelab and extends it with authentication and persistence.

The Android Studio project lives in the [`Shrine/`](Shrine/) directory.

## What this repo is

- **Single-module Android app** (`:app`), single-Activity + Fragments.
- **Local-only data:** a Room SQLite database (`contactDB`) stores users and cart items; the logged-in session is cached in `SharedPreferences`. There is no backend server.
- **Screens:** Login → Register, Product Grid (with a Material "backdrop" reveal), Cart, Order Placed, Profile.

For the full picture, read the docs in order under **[`Shrine/docs/`](Shrine/docs/)** (see "Documentation" below).

## Set up & run locally

**Prerequisites**
- JDK **17–21** (Android Studio's bundled JBR works).
- Android SDK with **platforms 33 and 34** and **build-tools 33** installed.
- An emulator or a connected device.

**Android Studio**
1. Open the `Shrine/` folder (not the repo root) as the project.
2. Set **Settings → Build Tools → Gradle → Gradle JDK** to a JDK 17–21 (e.g. the embedded JBR).
3. Run the `app` configuration.

**Command line**
```bash
cd Shrine
./gradlew assembleDebug
# install & launch on a running emulator/device:
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.google.codelabs.mdc.kotlin.shrine/.MainActivity
```

**Try it:** Register a user, then log in with that **email** (the "Username" field) and password to reach the product grid.

## Documentation

Deep-dive docs live in [`Shrine/docs/`](Shrine/docs/). Read them in this order:

1. **[ARCHITECTURE.md](Shrine/docs/ARCHITECTURE.md)** — system design, components, data/control flow, dependencies, key decisions, and known tech debt.
2. **[CODEBASE.md](Shrine/docs/CODEBASE.md)** — directory tree, module responsibilities, entry points, build/run commands, conventions, and "where to look" recipes.
3. **[FEATURES.md](Shrine/docs/FEATURES.md)** — every user-facing feature and the modules that implement it end to end.

Plans and audits (when they exist) live alongside these as `Shrine/docs/plan_<N>_<feature-slug>.md` and `Shrine/docs/audit_<N>_<feature-slug>.md`. _None exist yet._

## Contributing

1. Read **ARCHITECTURE.md**, **CODEBASE.md**, and **FEATURES.md** first — they tell you which source files matter for a given change.
2. Make your change, following the conventions in CODEBASE.md (package `com.google.codelabs.mdc.kotlin.shrine`, `shr_` resource prefix, `findViewById` — no `kotlinx.android.synthetic`).
3. If your change affects architecture, a module's responsibility, or a feature, **update the matching doc in the same change**.
4. Build and smoke-test (`./gradlew assembleDebug`, then run the affected screen).

Project-wide working rules for AI/code agents are in [CLAUDE.md](CLAUDE.md).
