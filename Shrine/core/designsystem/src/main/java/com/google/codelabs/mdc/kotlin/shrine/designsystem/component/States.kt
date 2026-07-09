package com.google.codelabs.mdc.kotlin.shrine.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Animated shimmer fill for skeleton placeholders (figma "Skeleton · 1.2s shimmer loop"). */
@Composable
fun Modifier.shrineShimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Restart),
        label = "shimmer-progress",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val width = 600f
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(progress * 2 * width - width, 0f),
        end = Offset(progress * 2 * width, 0f),
    )
    return this.background(brush)
}

/** A single shimmering skeleton block. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, height: Int = 16) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
            .shrineShimmer(),
    )
}

/** Generic loading state: a few stacked skeleton rows. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 180)
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.6f), height = 20)
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f), height = 20)
    }
}

/** Centered empty state with icon, message and optional action (figma "Your cart is empty"). */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    CenteredState(icon, title, subtitle, modifier, actionLabel, onAction)
}

/** Centered error state (figma "Something went wrong · Retry"). */
@Composable
fun ErrorState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    CenteredState(icon, title, subtitle, modifier, if (onRetry != null) "Retry" else null, onRetry)
}

@Composable
private fun CenteredState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            ShrineButton(text = actionLabel, onClick = onAction, variant = ShrineButtonVariant.Tonal)
        }
    }
}
