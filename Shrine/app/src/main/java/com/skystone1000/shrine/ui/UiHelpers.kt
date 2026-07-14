package com.skystone1000.shrine.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.ui.graphics.vector.ImageVector
import com.skystone1000.shrine.core.data.SessionRepository
import com.skystone1000.shrine.core.data.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** No signed-in user (guest / signed-out). Mirrors [DataStoreSessionRepository.NO_USER]. */
const val NO_USER: Long = -1L

/** Maps a [CategoryEntity.iconKey] (figma category tiles) to a Material icon. */
fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "headphones" -> Icons.Rounded.Headphones
    "checkroom" -> Icons.Rounded.Checkroom
    "spa" -> Icons.Rounded.Spa
    "chair" -> Icons.Rounded.Chair
    else -> Icons.Rounded.Category
}

/** Two-letter initials for an avatar (e.g. "Ava Morgan" → "AM"). */
fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}

/** Time-of-day greeting (figma Home: "Good morning"). */
fun greeting(hourOfDay: Int): String = when (hourOfDay) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

/** The active scope id for per-user data; guests/signed-out resolve to [NO_USER]. */
fun SessionRepository.userIdFlow(): Flow<Long> = session.map { it?.userId ?: NO_USER }

/** Convenience: the active session or null. */
val SessionState?.scopeId: Long get() = this?.userId ?: NO_USER
