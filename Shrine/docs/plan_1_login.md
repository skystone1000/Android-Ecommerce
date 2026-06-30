---
title: Login reliability and feedback
status: active
last_updated: 2026-06-27
scope: Make login navigation reliable (main-thread), give failure feedback, fix the username/email mismatch, and establish the lifecycle-safe coroutine→navigation pattern reused by later plans.
---

# plan_1_login — Login reliability and feedback

## Goal

Make the login gate trustworthy: a correct email+password reliably navigates to the product grid, a wrong one shows a clear error, and the field labels match what is actually required. Along the way, establish the **"do DB work off-main, navigate on main, scoped to the view lifecycle"** pattern that plan_2 and plan_4 reuse.

This is the highest-leverage, lowest-risk starting point: login is the entry gate (everything downstream is unreachable if it misbehaves), the changes are confined to one fragment plus a small Gradle/string addition, and it sets the concurrency convention for the codebase.

## Scope

**In:**
- Fix background-thread navigation in `LoginFragment` (B2).
- Add an explicit error message on failed login (B4).
- Resolve the "Username" vs email mismatch (B5).
- Switch login's coroutine from `CoroutineScope(Dispatchers.IO)` to `viewLifecycleOwner.lifecycleScope` (B6, login portion).
- Add the `androidx.lifecycle:lifecycle-runtime-ktx` (and `androidx.fragment:fragment-ktx`) dependencies needed for `lifecycleScope`/`viewLifecycleOwner`.

**Out:**
- Password hashing and duplicate-email handling (→ plan_4_security).
- Cart and registration coroutine fixes (registration insert keeps working as-is here; cart is plan_2).
- Any ViewModel/architecture refactor.

## Step-by-step changes

1. **Add dependencies** in `app/build.gradle`:
   - `implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"`
   - `implementation "androidx.fragment:fragment-ktx:1.6.2"` (provides `viewLifecycleOwner` ergonomics).
2. **Rewrite the login click handler** in `fragments/LoginFragment.kt`:
   - Replace `CoroutineScope(Dispatchers.IO).launch { userLogin() }` (`:46`) with `viewLifecycleOwner.lifecycleScope.launch { ... }`.
   - Inside, run the DB lookup with `withContext(Dispatchers.IO) { database.userDao().getLogin(email) }`, then evaluate the result back on the main thread.
3. **Rework `userLogin()`** (`:64`–end) to *return a result* rather than navigate internally:
   - On match: write the `SharedPreferences` session (unchanged keys), then call `navigateTo(ProductGridFragment(), false)` **on the main thread**.
   - On no-match/null user: show an error. Uncomment/replace the dead `else` branch (`:72`+) with a visible message — set `passwordTextInput.error = getString(R.string.shr_error_invalid_credentials)` and/or a `Toast`.
4. **Add the error string** to `res/values/strings.xml`: `shr_error_invalid_credentials` = "Incorrect email or password."
5. **Fix the username/email label** (B5): in `res/layout/shr_login_fragment.xml`, change the username `TextInputLayout` hint to the existing `@string/label_email` ("Email") so the field matches the `getLogin(email)` query. (Keep the view id to avoid touching `findViewById` calls.)
6. **Smoke test** the three paths: valid login → grid; wrong password → error; unknown email → error.

## Files / modules touched

- `app/build.gradle` (2 dependencies).
- `app/src/main/java/.../fragments/LoginFragment.kt` (coroutine scope, navigation thread, failure feedback).
- `app/src/main/res/values/strings.xml` (new error string).
- `app/src/main/res/layout/shr_login_fragment.xml` (hint relabel).
- Docs: update `FEATURES.md` §2 (login feedback + email label) and `ARCHITECTURE.md` "Known issues" (remove the login portion of the background-thread item).

## Dependencies on other plans

None. This plan is the foundation; plan_2 and plan_4 reuse its `lifecycleScope` + main-thread-navigation pattern.

## Risks and rollback

- **Risk:** `viewLifecycleOwner.lifecycleScope` used before the view exists. Mitigation: only launch from within the click listener (view is alive).
- **Risk:** dependency version skew with AGP 8.5.2/Kotlin 1.9.24. Mitigation: the pinned versions (lifecycle 2.6.2, fragment 1.6.2) are compatible; bump only if Gradle resolves a conflict.
- **Rollback:** revert `LoginFragment.kt`, the two layout/string edits, and the Gradle dependency lines. No schema or persisted-data changes, so rollback is clean.

## Definition of done

- Logging in with correct credentials navigates to the product grid every time (no flakiness), with navigation running on the main thread.
- Wrong password and unknown email each show a visible error; no silent no-op.
- The login field is labeled "Email".
- No `GlobalScope`/`Dispatchers.IO`-thread `navigateTo` remains in `LoginFragment`.
- `./gradlew assembleDebug` passes; manual smoke test of all three login paths succeeds.
- `FEATURES.md` and `ARCHITECTURE.md` updated to match.
