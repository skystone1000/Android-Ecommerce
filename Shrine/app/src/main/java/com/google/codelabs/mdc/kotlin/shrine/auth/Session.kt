package com.google.codelabs.mdc.kotlin.shrine.auth

import android.app.Activity
import android.content.Context

/**
 * Reads the logged-in user's id from the per-Activity session [android.content.SharedPreferences]
 * populated by `LoginFragment.saveSession`. Used to scope the cart to the current user so accounts
 * never see each other's items on a shared device.
 */
object Session {
    const val NO_USER = -1L

    fun currentUserId(activity: Activity): Long =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getString("user_id", null)?.toLongOrNull() ?: NO_USER
}
