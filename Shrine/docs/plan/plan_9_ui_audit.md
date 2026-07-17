---
title: UI Audit & Remediation — insets, spacing, figma parity, hardening
status: active
last_updated: 2026-06-29
# Phase A (insets & system bars) implemented 2026-06-29; Phases B–D outstanding.
scope: Findings from an emulator + code + figma audit of the Compose app (system-bar insets, padding/margins, figma parity) plus answers to the security/efficiency/regression/test questions, and a phased plan to fix them.
---

# Plan 9 — UI Audit & Remediation

This plan records a hands-on audit of the modernised Compose app and lays out the fixes. It follows [plan_8_modernise.md](plan_8_modernise.md) (which delivered the Compose rewrite, Phases 0–7). The design source of truth is [`figma/Shrine.dc.html`](../../../figma/Shrine.dc.html) (15 screens) and the engineering contract in plan_8's [coverage map](plan_8_modernise.md#ui-coverage-map-figma--plan).

## Method

- **Live device:** built APK installed on `emulator-5554` (1080×2400, density 420, gesture+3-button nav, **dark** system theme). Captured screenshots of: Home, Search, Cart, Saved, Profile (guest), Product detail, Category, Checkout. Sub-screens reached by tap were flaky to automate; the rest were audited from source.
- **Figma tokens:** extracted from the design HTML — dominant screen gutter `padding:0 24px`, card padding `16px`, list gaps `12–14px`, section gaps `32px`.
- **Code:** read the inset-critical paths — [ShrineApp.kt](../../app/src/main/java/com/skystone1000/shrine/ui/ShrineApp.kt), [MainActivity.kt](../../app/src/main/java/com/skystone1000/shrine/MainActivity.kt), [styles.xml](../../app/src/main/res/values/styles.xml), the design-system [Bars.kt](../../core/designsystem/src/main/java/com/skystone1000/shrine/designsystem/component/Bars.kt), and the sticky-bottom-bar screens.

## Root cause (the headline)

`targetSdk = 35` opts the app into **forced edge-to-edge** (the app draws under the status and navigation bars and must inset its own content). The Phase-4 inset fix in [ShrineApp.kt:104](../../app/src/main/java/com/skystone1000/shrine/ui/ShrineApp.kt) zeroes **all** insets on the outer `Scaffold`:

```kotlin
Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = { if (selectedTab >= 0) ShrineBottomBar(...) }) { padding -> NavHost(Modifier.padding(padding)) }
```

That correctly removes the *double top inset* on the five **tab** screens (each re-adds the status-bar inset through its own `TopAppBar`, and the M3 `NavigationBar` in `ShrineBottomBar` consumes the bottom inset). But for **pushed** screens (Product detail, Checkout, Order placed, Edit profile, Addresses, Payment methods, Order history/detail) there is **no app bottom bar**, so nothing reintroduces the navigation-bar inset — their content and sticky action bars draw **under** the system navigation bar. No screen anywhere calls `navigationBarsPadding()` (only `statusBarsPadding()` is used, for the few top-bar-less screens). `MainActivity` never calls `enableEdgeToEdge()`, so the system-bar icon appearance is left to the legacy XML window theme.

## Findings

Severity: **P0** blocks/obscures a primary action · **P1** clearly wrong, visible · **P2** polish/standards · **P3** minor.

| # | Sev | Screen(s) | Finding | Evidence |
|---|-----|-----------|---------|----------|
| F1 | **P0** | Product detail, Checkout (and by code: Order placed) | Sticky bottom action bar (**"Add to cart"**, **"Place order"**, "Continue shopping") renders **under the system navigation bar** — the system nav buttons overlap the CTA, partly obscuring it. | Screenshots `06_product_detail`, `10_checkout` |
| F2 | **P1** | All screens | **Status-bar icons are dark-on-dark** in the dark theme (clock/5G/battery nearly invisible). The XML `Theme.Shrine` sets `android:windowLightStatusBar=true` (dark icons) unconditionally; it never follows the Compose dark theme. | Every screenshot (status bar) |
| F3 | **P1** | Pushed scrollable screens (Category, Order history, Addresses, Payment methods, Edit profile) | Scroll content has **no bottom `navigationBars` content-padding**, so the last row/field/FAB can sit under the system nav bar. (Visible only with enough items; Category showed 3 so it cleared.) | Code (`ShrineApp.kt:104`, no `navigationBarsPadding` in screens) |
| F4 | **P1** | Profile (guest) | The primary **"Sign in" CTA is below the fold and clipped at rest** — the avatar+stats+6 menu rows+2 buttons exceed the viewport, so a first-time guest sees only a sliver of the peach button. | `05_profile`, `11_profile_scrolled` |
| F5 | **P2** | All screens | **System navigation bar is not themed** to the app — a light-grey 3-button strip under a dark app on tab screens, and an un-scrimmed translucent bar on pushed screens. Inconsistent and off-brand. | Every screenshot (bottom) |
| F6 | **P2** | All content screens | **Gutter mismatch vs figma:** app uses **16dp** horizontal padding; figma's dominant screen gutter is **24dp** (`padding:0 24px`). The app reads tighter than the design. Decide one standard and apply via a token. | Figma tokens vs `HomeScreen.kt`/others (`PaddingValues(16.dp)`) |
| F7 | **P3** | Wishlist / Saved card | The quick-add **"+"** button slightly **overlaps the "4.0" rating** text on the product card. | `04_saved` |
| F8 | **P3** | Product cards everywhere | Product images are blank boxes (seed/remote URLs don't resolve) with **no branded placeholder** — looks unfinished. Coil has no `placeholder`/`error` drawable or crossfade configured. | All product screens |

> Top-inset handling on screens that **do** have a `TopAppBar` (Home, Cart, Saved, Profile, Category, Settings, etc.) is correct — titles sit clear of the status bar. The bug is specific to the **bottom/navigation-bar** inset on pushed screens and to **system-bar icon/colour theming**.

## Per-screen figma parity (verified screens)

- **Home** ✅ greeting + hero promo + 4 category tiles + "New arrivals" grid + cart badge — matches figma intent; gutter 16 vs 24 (F6).
- **Search** ✅ search field + suggestions chips + filter (tune) action; uses `statusBarsPadding()` so the top is clear. Large empty area as guest (no recent) is expected.
- **Cart** ✅ line item + variant + stepper + Subtotal/Shipping/Total + sticky "Checkout · $total". Bottom CTA OK here because Cart is a **tab** (nav bar reserves space).
- **Saved** ✅ grid + filled heart + quick-add; F7 overlap.
- **Profile** ✅ avatar + stats + menu; guest CTA overflow (F4).
- **Product detail** ✅ pager dots, strikethrough price, rating·reviews, variant chips, description, "you may also like" — but F1 bottom overlap.
- **Category** ✅ sort chips + result count + grid; F3 risk on long lists.
- **Checkout** ✅ address/delivery/payment/summary cards — but F1 bottom overlap.
- **Code-audited (not screenshotted):** Order placed, Order history/detail, Edit profile, Settings, Addresses, Payment methods, Help center, Splash, Login/Register/Forgot — structurally match plan_8's coverage map; subject to F1–F3/F5 by the same inset code paths (Order placed/Edit profile bottom CTAs, Addresses/Payments FAB).

---

## Audit questions

### Did you build this in the most secure way?

Mostly sound for a **local-first demo**, with a few hardening gaps:

- ✅ **Passwords** are never stored plaintext — salted PBKDF2 ([PasswordHasher.kt](../../core/data/src/main/java/com/skystone1000/shrine/core/data/PasswordHasher.kt)), random 16-byte salt, hash+salt Base64 in Room.
- ⚠️ **KDF parameters are dated:** `PBKDF2WithHmacSHA1` at **100k** iterations. OWASP (2023) recommends PBKDF2-HMAC-**SHA256** at ~600k, or PBKDF2-HMAC-SHA1 at ~1.3M. Move to SHA256 + raise iterations (with a stored algorithm/version tag so existing hashes still verify).
- ✅ **SQL injection:** all DAO queries are parameterised (`LIKE '%' || :query || '%'`), no string concatenation.
- ✅ **Attack surface:** no backend; only `INTERNET`/`ACCESS_NETWORK_STATE` for Coil images. Only `MainActivity` is exported (launcher). `android:allowBackup="false"`. No card data stored (masked only).
- ⚠️ **Session/settings** live in plaintext DataStore (app-private, acceptable for a demo; note it's not encrypted). No screen-capture flag on any "sensitive" screen (none truly sensitive here).
- ⚠️ **Release build has `minifyEnabled false`** — no R8 shrinking/obfuscation. Fine for debug; enable R8 + resource shrinking before any real release.
- ➕ Consider a `network-security-config` pinning cleartext to `false` explicitly, even though images are HTTPS.

### Did you build this in the most efficient way?

Largely yes; a few wins:

- ✅ MVVM with `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), ...)` — shared, lifecycle-scoped flows; no redundant work while unsubscribed.
- ⚠️ **Collectors are not lifecycle-aware:** all 16 screens use `collectAsState()` rather than `collectAsStateWithLifecycle()` (the artifact `lifecycle-runtime-compose` isn't a dependency). They keep collecting while the app is in the background. Add the dep and switch.
- ⚠️ **Search runs a DB query per keystroke** — `searchRepository.results(q)` is invoked inside the query `combine` with no **debounce** ([SearchScreen.kt:105](../../app/src/main/java/com/skystone1000/shrine/ui/screens/SearchScreen.kt)). Add `debounce(~250ms)` + `distinctUntilChanged` (and ideally `flatMapLatest` to cancel stale queries).
- ⚠️ **Coil** has no crossfade/placeholder; configure an `ImageLoader` once (memory/disk cache defaults are fine).
- ✅ Lists use `LazyVerticalGrid`/`LazyRow`; integer-cent money math avoids float drift.

### What regressions could this introduce?

The inset fixes touch global layout, so:

- **Re-introducing the double top inset** that Phase 4 fixed — if we drop `contentWindowInsets = 0` or add `enableEdgeToEdge()` carelessly, tab-screen titles could gain a status-bar-sized gap again. **Mitigation:** keep each screen owning its top inset; verify all 17 screens after the change.
- **Double bottom padding** on tab screens if `navigationBarsPadding()` is added somewhere the M3 `NavigationBar` already insets.
- **Keyboard/IME behaviour** on Login/Register/Edit profile/Address & payment sheets — inset changes can shift how the keyboard pushes content; verify fields stay visible.
- **Light-theme status bar** could break if the dark-theme icon fix isn't conditioned on the resolved theme.
- **ModalBottomSheet** (filter, address/payment forms) insets — sheets have their own inset handling; confirm they still clear the nav bar.
- **Existing 44-test JVM suite** won't catch any of this (Robolectric doesn't render system bars) — see below.

### What tests do we need before we ship this?

- **Screenshot tests per screen (Roborazzi), light + dark** — extend the Phase-7 gallery harness to each `XxxScreen` over fakes (the pattern already exists in `ScreenRenderTest`/`GalleryScreenshotTest`). Catches layout/padding regressions.
- **Instrumented inset tests on a device/emulator** across nav modes (gesture vs 3-button) and a notch/cutout profile — assert the sticky CTAs and last list item are **not** under the system bars (e.g., assert bottom bar `top >= content bottom`, or use an Espresso visibility/overlap check). This is the only layer that reproduces F1/F3.
- **Status-bar appearance test** — verify `isAppearanceLightStatusBars` tracks the resolved theme.
- **Search debounce test** — assert N keystrokes within the window collapse to one query (Turbine).
- **PasswordHasher migration test** — old SHA1 hashes still verify after introducing the SHA256/versioned scheme.
- **Navigation/edge-to-edge smoke** — auth→main→pushed→back returns insets correctly.

---

## Remediation plan (phased)

### Phase A — Insets & system bars (P0/P1) — *do first, ship-blocking*

> **Status: implemented (2026-06-29).** Steps 1, 2, 4 and 6 are done; F1/F2/F5 are fixed in code (`assembleDebug` + the 44-test JVM suite stay green). Step 3 is intentionally **not** done as written — the per-screen inner `Scaffold`s already inset the bottom of their scroll content via the default `contentWindowInsets = systemBars`, so step 5 (and F3) is already covered and adding extra `navigationBarsPadding()` there would *double* the inset. The remaining open item is **step 7** (on-device re-verification across gesture/3-button × light/dark). Changes: `MainActivity` (`enableEdgeToEdge()`), `ShrineApp` (theme-driven `WindowInsetsControllerCompat` icon appearance), `styles.xml` (dropped hard-coded `windowLightStatusBar`), and `navigationBarsPadding()` on the sticky CTAs in `ProductDetailScreen`/`CheckoutScreen`/`OrderScreens` (OrderPlaced). Tab screens were deliberately left untouched.

1. **Adopt explicit edge-to-edge.** In `MainActivity.onCreate` call `enableEdgeToEdge()` before `setContent`.
2. **Drive system-bar icon appearance from the theme.** In `ShrineTheme` (or `MainActivity` via a `SideEffect`), set `WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars / isAppearanceLightNavigationBars = !darkTheme`. Remove the hard-coded `android:windowLightStatusBar=true` from [styles.xml](../../app/src/main/res/values/styles.xml) (or make it theme-conditional). Fixes **F2**.
3. **Fix the bottom inset for pushed screens.** Keep the outer `Scaffold` top inset at 0, but stop zeroing the **bottom**: change `contentWindowInsets` to expose `WindowInsets.navigationBars` (or simpler, give the `NavHost` content `Modifier.consumeWindowInsets`/`navigationBarsPadding` only when there is **no** app bottom bar). Concretely: when `selectedTab < 0`, the content area must carry the navigation-bar inset. Fixes **F1**.
4. **Sticky bottom bars consume the nav inset.** Add `Modifier.navigationBarsPadding()` (or `.windowInsetsPadding(WindowInsets.navigationBars)`) to the bottom-bar content in `ProductDetailScreen`, `CheckoutScreen`, `OrderScreens` (Order placed), and any inner-`Scaffold` `bottomBar`. Belt-and-suspenders with #3 — pick one source of truth to avoid double padding (F-regression).
5. **Scroll content bottom padding.** Add `contentPadding`/`navigationBarsPadding` to the lazy lists/columns on Category, Order history, Addresses, Payment methods, Edit profile so the last item clears the nav bar. Fixes **F3**.
6. **Theme the navigation bar.** With edge-to-edge the nav bar is transparent; ensure the app background extends behind it so it reads as part of the dark UI (no light strip). Fixes **F5**.
7. **Re-verify all 17 screens** on the emulator (gesture + 3-button) for no overlap and no double top gap.

### Phase B — Spacing & figma parity (P2/P3)
8. **Introduce a screen-gutter token** in the design system (e.g., `Dimens.screenGutter`) and apply it uniformly. Decide 16dp vs figma's 24dp with the designer; default to **24dp** to match figma unless density feels too loose. Fixes **F6**.
9. **Guest Profile CTA** — make the "Sign in"/"Create account" actions reachable without hunting: either reduce the menu density, move the CTAs into a non-scrolling bottom region, or show them above the menu for guests. Fixes **F4**.
10. **Wishlist card** — give the rating row trailing padding so the quick-add "+" never overlaps "4.0". Fixes **F7**.
11. **Coil placeholders** — configure a brand placeholder/error drawable + crossfade. Fixes **F8**.

### Phase C — Efficiency & hardening (P2)
12. Add `androidx.lifecycle:lifecycle-runtime-compose` and replace `collectAsState()` → `collectAsStateWithLifecycle()` across the 16 screens.
13. Add `debounce(250)` + `distinctUntilChanged()` to the search query flow.
14. `PasswordHasher` → `PBKDF2WithHmacSHA256`, raise iterations (~600k), store an algorithm/version tag; keep SHA1 verification path for existing accounts (migration test).
15. Enable R8 (`minifyEnabled true` + `shrinkResources true`) for `release` with a tested keep-rules set; add a `network-security-config` disallowing cleartext.

### Phase D — Test coverage (gates for the above)
16. Per-screen Roborazzi screenshots (light + dark).
17. Instrumented inset/overlap tests across nav modes + cutout.
18. Search-debounce (Turbine) and PasswordHasher-migration unit tests.
19. Wire the new instrumented job into `.github/workflows/ci.yml` (emulator matrix) or document it as a manual pre-release gate.

## Definition of done

- On a real device in **both** gesture and 3-button nav, **dark and light**: no screen's content or sticky CTA overlaps the status or navigation bar; status-bar/nav-bar icons are legible against the current theme; no double gap under any title.
- Screen gutters use a single token consistent with figma; guest can act on "Sign in" without scrolling; wishlist "+" never overlaps rating; product images show a branded placeholder.
- Collectors are lifecycle-aware; search debounces; KDF upgraded with a passing migration test; release build shrinks/obfuscates.
- New screenshot + instrumented inset tests are green in CI (or documented as the pre-release gate).

## Out of scope

Brand rename (`shr_` resources / display name), backend integration, and the placeholder features already tracked in plan_8 (Forgot password, Track order, Help center, live payment) — see [FEATURE_BACKLOG.md](../FEATURE_BACKLOG.md).
