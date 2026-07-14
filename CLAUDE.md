# CLAUDE.md — Working protocol for this repository

The Android app lives in `Shrine/`. Its documentation lives in `Shrine/docs/`.

## Read docs before code

1. Before reading any source files, read these three docs first:
   - `Shrine/docs/ARCHITECTURE.md`
   - `Shrine/docs/CODEBASE.md`
   - `Shrine/docs/FEATURES.md`
2. Use those docs to decide **which** source files to open. Only read the files the docs point you to — do **not** re-scan the whole tree.
3. Treat the docs as the **source of truth**. If the code and the docs disagree, **flag the discrepancy** to the user instead of silently trusting either.

## Keep docs in sync (same session)

When you change code that affects **architecture**, a **module's responsibility**, or a **feature**, update the corresponding doc in the **same session** — `ARCHITECTURE.md`, `CODEBASE.md`, and/or `FEATURES.md`. Edit **surgically**: change only the affected sections, not the whole file. Every claim in a doc must be verifiable in the code.

## Plans & audits — naming convention

All planning and audit documents live in `Shrine/docs/` and follow:

```
Shrine/docs/plan_<N>_<feature-slug>.md
Shrine/docs/audit_<N>_<feature-slug>.md
```

- `<feature-slug>` is the **feature name** (e.g. `cart`, `login`). It is descriptive, not a ranking.
- `<N>` is a zero-padded-free integer that **sequences** documents. Plans and audits each use their own continuous numbering space.
- **Continue from the highest existing `N`; never reuse or restart numbers.** A new plan for an existing feature reuses that feature's slug with a **fresh** `N`.
- As of 2026-06-27 there are **no existing plans or audits**, so the next plan is `plan_1_<slug>.md` and the next audit is `audit_1_<slug>.md`.
- If a newer plan supersedes an older one for the same feature, set the old file's front-matter `status: superseded` and name the superseding file.

### Front matter (required)

Every `plan_*` / `audit_*` file starts with YAML front matter:

```yaml
---
title: <human title>
status: active | completed | superseded | abandoned
last_updated: YYYY-MM-DD
scope: <one line on what this covers>
---
```

The three core docs (`ARCHITECTURE.md`, `CODEBASE.md`, `FEATURES.md`) carry `title`, `last_updated`, and `scope` front matter (no `status`).

## Project quick facts

- Package / application id: `com.skystone1000.shrine`.
- Build from `Shrine/`: `./gradlew assembleDebug`. Toolchain: Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, JVM target 17, compileSdk 34 / minSdk 16. Needs SDK platforms 33 + 34.
- Conventions: `shr_` resource prefix; access views with `findViewById` (the project was migrated off `kotlin-android-extensions` — do not reintroduce `kotlinx.android.synthetic`).
