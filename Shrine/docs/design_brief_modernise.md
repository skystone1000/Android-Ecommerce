---
title: UI Design Brief / Generation Prompt — Shrine modernisation
last_updated: 2026-06-28
scope: A self-contained prompt to generate the complete new Shrine UI (design system + all screens, light + dark) for an Android Jetpack Compose / Material 3 app. Companion to plan_8_modernise.md.
---

# Design generation prompt

Paste the block below into Claude (design) or your UI generation tool. It is self-contained.

---

> **Role & goal.** You are a senior product designer. Design the complete UI for **Shrine**, an Android shopping app, as a cohesive, production-ready design system plus every screen. The app will be built in **Jetpack Compose with Material 3**, so everything must be implementable with M3 primitives (Scaffold, TopAppBar, NavigationBar, Cards, TextFields, Chips, etc.). Deliver both **light and dark** themes.
>
> **Brand & aesthetic.** "Shrine" is a premium boutique marketplace (electronics, fashion, fragrance, lifestyle). Aesthetic: **premium minimal retail** — calm, editorial, expensive-feeling. Lots of whitespace, large product imagery as the hero, restrained UI chrome, refined typography, one quiet accent colour. Think high-end fashion/concept-store app, not a busy bargain marketplace. Keep a subtle nod to the existing Shrine identity (a faceted "diamond" logomark) but modernised. Avoid generic AI-app gradients, heavy shadows, and clutter.
>
> **Platform constraints.** Android, Compose, Material 3. 8dp spacing grid. Respect system status/navigation bars and safe areas. Touch targets ≥ 48dp. Support dynamic type. Motion should be subtle and physical (M3 easing), never decorative.
>
> ## 1. Design system (define tokens first)
>
> **Color — light:** near-white background (e.g. `#FAFAFA`), pure-white surfaces, near-black primary text (`#1A1A1A`), muted grey secondary text, hairline dividers (`#ECECEC`), one accent for primary actions/price/active states (propose a refined accent — a muted terracotta/blush or deep ink works with Shrine's heritage). Semantic colors: success, error, warning, info — desaturated, premium.
> **Color — dark:** near-black background (`#0E0E0E`), elevated charcoal surfaces (`#1A1A1A`/`#222`), off-white text (`#F2F2F2`), low-contrast dividers, the same accent re-tuned for dark. Ensure WCAG AA contrast in both themes. Provide the full token table (background, surface, surfaceVariant, primary, onPrimary, secondary, outline, text-primary/secondary/disabled, accent, success/error/warning) for light AND dark.
> **Typography:** a refined sans (e.g. Inter/Plus Jakarta Sans) with an optional serif for display/headings to add editorial warmth. Define a scale: Display, Headline, Title, Body L/M/S, Label, Caption — with size/weight/line-height/letter-spacing. Prices use tabular figures.
> **Spacing & layout:** 4/8/12/16/24/32/48 scale; standard screen padding 16–20dp; generous section spacing.
> **Shape:** soft, consistent radii (cards ~16dp, buttons ~12dp, sheets ~24dp top); no harsh corners.
> **Elevation:** mostly flat; use subtle tonal surface shifts instead of heavy drop shadows.
> **Iconography:** thin, consistent line icons (Material Symbols rounded). **Motion:** shared-element-style product image transitions, gentle fades/slides, button press states, skeleton shimmer for loading.
>
> ## 2. Core components (design each, light + dark, with all states)
> Buttons (primary / secondary / tertiary-text / icon; default/pressed/disabled/loading), Text fields (label, helper, error, focused), Search bar, Chips & filter chips (selectable), **Product card** (image, name, price, wishlist heart, rating), **Quantity stepper** (− value +), Rating stars, Top app bar (large + small variants, with back/cart/search actions), **Bottom navigation bar** (Home, Search, Cart with badge, Wishlist, Profile), Cart badge, Price text, Section header ("see all"), Category pill/tile, Promotion/hero banner, Bottom sheet (filters), Snackbar/toast, Dialog, **Empty state**, **Loading (skeleton)** , **Error state**, Avatar, List row, Divider.
>
> ## 3. Screens (design all, light + dark; show empty/loading/error where relevant)
> 1. **Splash** — logomark, brand wordmark, quiet.
> 2. **Login** — email + password fields, primary "Sign in", link to Register, inline validation errors.
> 3. **Register** — name, email, phone, organisation, password, confirm password; inline validation; success → back to login.
> 4. **Home / Catalog** — large search entry, hero promotion banner/carousel, horizontal **category** row, then a product grid ("Featured" / "New"). Top bar with cart icon (badge). Bottom nav.
> 5. **Category** — header, filter/sort entry, product grid for one category.
> 6. **Search** — active search field, recent/suggested queries, results grid, **filter bottom sheet** (price range, category, rating, sort).
> 7. **Product detail** — full-bleed image gallery (pager + dots), name, price, rating, description, quantity stepper, **Add to cart** (sticky bottom bar), wishlist heart, "you may also like" row.
> 8. **Cart** — line items with image, name, price, quantity stepper, remove; order summary (subtotal, total); sticky **Checkout** bar; empty-cart state.
> 9. **Checkout** — order review (items, totals), a simple address/payment placeholder, **Place order**.
> 10. **Order placed** — animated success confirmation, order number, "Continue shopping" + "View order".
> 11. **Order history** — list of past orders (date, item count, total, status) → tappable to order detail.
> 12. **Wishlist** — saved products grid, move-to-cart, empty state.
> 13. **Profile** — avatar, name/email/phone, entries to Order history, Wishlist, Edit profile, Settings, Sign out.
> 14. **Edit profile** — editable fields, save.
> 15. **Settings** — theme (System / Light / Dark), grid layout toggle, notifications placeholder, about.
>
> ## 4. Deliverables
> For each: high-fidelity mockups in **light and dark**, at mobile size; the **token table** (colors, type, spacing, radius) up front; a **component sheet**; annotations for key states and interactions; and notes that tie each component to its Material 3 equivalent so engineering can implement it directly in Compose. Keep it consistent, minimal, and premium throughout.

---

## How this maps to engineering
The screen list above is intentionally identical to Phase 4 of [plan_8_modernise.md](plan_8_modernise.md). The token table feeds `:core:designsystem` (`ShrineTheme`, color schemes, typography, shapes); the component sheet feeds the core composables; each screen maps to a `Screen` + `ViewModel` + `UiState`.
