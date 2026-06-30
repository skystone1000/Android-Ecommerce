---
title: Authentication hardening (password hashing)
status: active
last_updated: 2026-06-27
scope: Hash passwords with a salted KDF, compare hashes at login, and prevent duplicate-email registrations.
---

# plan_4_security — Authentication hardening

## Goal

Stop storing and comparing passwords in plaintext (B3) and prevent duplicate accounts per email (B9). After this plan, the `users` table holds only salted hashes, login verifies against the hash, and registering an already-used email fails with a clear message.

Fourth in sequence: high value but higher risk than the earlier plans because it changes how credentials are persisted (a Room schema/migration concern) and touches the auth paths that plan_1 just stabilized. Doing it after plan_1 means the login flow is already reliable and easy to re-test.

## Scope

**In:**
- Hash passwords with a salted, accepted KDF before insert (`RegisterFragment`).
- Verify entered password against the stored hash at login (`LoginFragment`).
- Store salt + hash on the `User` row (schema change → Room migration).
- Enforce unique `user_email` and reject duplicate registration with a visible error (B9).

**Out:**
- Account recovery / password reset (backlog).
- Biometric or remote auth (backlog).
- Session persistence changes (backlog: persistent login).

## Step-by-step changes

1. **Choose a KDF.** Use a salted hash suitable for on-device storage — PBKDF2 via `javax.crypto.SecretKeyFactory` (no new dependency) or a vetted library. Generate a per-user random salt (`SecureRandom`).
2. **Model/schema change** (`models/User.kt`): replace `user_pass: String` with a stored hash, and add a salt column (e.g. `user_pass_hash`, `user_pass_salt`). Bump `ShrineDatabase` `version` to 2 and add a Room `Migration(1, 2)`. For this demo's local data, `fallbackToDestructiveMigration()` is an acceptable simpler alternative (document the choice).
3. **Hashing helper.** Add a small `auth/PasswordHasher.kt` (object) with `hash(password, salt)` and `newSalt()`; keep it pure/testable.
4. **Registration** (`fragments/RegisterFragment.kt:104`): compute `salt` + `hash` and store those instead of the raw password. Keep the existing field validation from the current `check()`/`isPasswordValid()`.
5. **Duplicate-email guard (B9):** add a unique index on `user_email` (`@Entity(indices=[Index(value=["user_email"], unique=true)])`) and, before insert, check `getLogin(email) != null`; if present, show "An account with this email already exists." and abort.
6. **Login** (`fragments/LoginFragment.kt:72`): fetch the user by email, recompute `hash(enteredPassword, user.salt)`, and compare to the stored hash. Reuse plan_1's failure-feedback path for mismatches.
7. **Tests (recommended):** unit-test `PasswordHasher` (same input+salt → same hash; different salts → different hashes) — see the testing backlog item.
8. **Smoke test:** register → row stores hash not plaintext (inspect DB); correct login works; wrong password fails; re-registering same email is rejected.

## Files / modules touched

- `app/src/main/java/.../models/User.kt` (hash + salt fields, unique index).
- `app/src/main/java/.../database/ShrineDatabase.kt` (version bump + migration or destructive fallback).
- `app/src/main/java/.../database/UserDAO.kt` (unchanged query, but relies on unique email).
- `app/src/main/java/.../auth/PasswordHasher.kt` (new).
- `app/src/main/java/.../fragments/RegisterFragment.kt` (hash on insert + duplicate guard + error string).
- `app/src/main/java/.../fragments/LoginFragment.kt` (hash-compare auth).
- `app/src/main/res/values/strings.xml` (duplicate-email error string).
- Docs: update `FEATURES.md` §1–2 (hashed storage, duplicate-email rule), `ARCHITECTURE.md` data-flow §1 + "Known issues" (remove plaintext-password item).

## Dependencies on other plans

- **Hard dependency on plan_1_login**: builds directly on plan_1's reworked, lifecycle-scoped login flow and its failure-feedback mechanism. Do plan_1 first.
- Independent of plan_2/plan_3.

## Risks and rollback

- **Risk:** schema migration. Existing `User` rows have `user_pass` (plaintext) and no salt. Mitigation: ship a `Migration(1,2)`; or, since these are throwaway local accounts, `fallbackToDestructiveMigration()` (users re-register). Document whichever is chosen.
- **Risk:** users created before this change can no longer log in (no salt/hash). Mitigation: destructive migration clears them, or the migration marks them invalid. Acceptable for a demo; call it out in release notes.
- **Risk:** weak KDF parameters. Mitigation: use a sufficient PBKDF2 iteration count and a 16-byte random salt.
- **Rollback:** revert the model/DAO/fragment changes and the DB version. Note that any rows written post-migration use the new columns; rolling back requires another destructive step.

## Definition of done

- No plaintext password is ever written to or read from the DB; `users` rows store salt + hash only.
- Login succeeds only when the recomputed hash matches; failures use plan_1's visible error.
- Registering an email that already exists is rejected with a clear message.
- `./gradlew assembleDebug` passes; DB inspection confirms hashed storage; auth smoke tests pass.
- `FEATURES.md` / `ARCHITECTURE.md` updated (plaintext-password item removed from "Known issues").

## Definition of done — security note

Document explicitly that this is on-device credential hardening for a local demo app, not a substitute for server-side auth.
