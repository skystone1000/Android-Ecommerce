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

## Plans, audits & prompts — where they live

Documents are organised into subdirectories of `Shrine/docs/`:

- **Plans and audits** live in **`Shrine/docs/plan/`**.
- **Prompts / design briefs** (e.g. UI-generation briefs) live in **`Shrine/docs/prompts/`**.
- The three core docs (`ARCHITECTURE.md`, `CODEBASE.md`, `FEATURES.md`) plus `FEATURE_BACKLOG.md` and `BUG_INVENTORY.md` stay at the top level of `Shrine/docs/`.

```
Shrine/docs/plan/plan_<N>_<feature-slug>.md
Shrine/docs/plan/audit_<N>_<feature-slug>.md
Shrine/docs/prompts/<prompt-slug>.md
```

- **When you create a new plan or audit, write it under `Shrine/docs/plan/`** (create the directory if it is missing). **When you create a new prompt or design brief, write it under `Shrine/docs/prompts/`.** Update any cross-links accordingly: a file in `plan/` links up to a top-level doc as `../FEATURE_BACKLOG.md` and to a prompt as `../prompts/<name>.md`.
- `<feature-slug>` is the **feature name** (e.g. `cart`, `login`). It is descriptive, not a ranking.
- `<N>` is a zero-padded-free integer that **sequences** documents. Plans and audits each use their own continuous numbering space.
- **Continue from the highest existing `N`; never reuse or restart numbers.** A new plan for an existing feature reuses that feature's slug with a **fresh** `N`.
- Plans `plan_1`–`plan_8` already exist in `Shrine/docs/plan/`, so the next plan is `plan_9_<slug>.md`. No audits exist yet, so the next audit is `audit_1_<slug>.md`.
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
