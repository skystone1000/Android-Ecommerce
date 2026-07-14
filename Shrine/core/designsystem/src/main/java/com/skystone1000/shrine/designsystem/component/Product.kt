@file:OptIn(ExperimentalMaterial3Api::class)

package com.skystone1000.shrine.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skystone1000.shrine.designsystem.theme.ShrineChipShape
import com.skystone1000.shrine.designsystem.theme.ShrinePriceTextStyle

/** Price text with tabular figures; shows an optional struck-through original price for sales. */
@Composable
fun PriceText(
    price: String,
    modifier: Modifier = Modifier,
    originalPrice: String? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(price, style = ShrinePriceTextStyle, color = MaterialTheme.colorScheme.primary)
        if (originalPrice != null) {
            Text(
                originalPrice,
                style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Star rating with the numeric value and optional review count (figma "4.0 · 218 reviews"). */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null,
    starCount: Int = 5,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(starCount) { i ->
            val filled = i < rating.toInt()
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )
        }
        Text(
            buildString {
                append(rating.toString())
                if (reviewCount != null) append(" · $reviewCount reviews")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Quantity stepper: − value + (figma cart / product detail). */
@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
) {
    Surface(
        modifier = modifier,
        shape = ShrineChipShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = quantity > minQuantity) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity")
            }
            Text("$quantity", style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onIncrement) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase quantity")
            }
        }
    }
}

/** Small "NEW" pill overlaid on product imagery. */
@Composable
fun NewBadge(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = ShrineChipShape, color = MaterialTheme.colorScheme.primary) {
        Text(
            "NEW",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/** Wraps an icon (e.g. cart) with a numeric badge. */
@Composable
fun ShrineBadgedIcon(
    icon: ImageVector,
    contentDescription: String?,
    count: Int,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        modifier = modifier,
        badge = { if (count > 0) Badge { Text("$count") } },
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

/**
 * Product card: image (provided via [image] slot so the data layer can plug in Coil),
 * wishlist heart, name, price and rating. Optionally flags a [isNew] product.
 */
@Composable
fun ProductCard(
    name: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rating: Float? = null,
    wishlisted: Boolean = false,
    onWishlistToggle: (Boolean) -> Unit = {},
    isNew: Boolean = false,
    image: @Composable () -> Unit = { ProductImagePlaceholder() },
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { image() }
            if (isNew) NewBadge(modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            IconToggleButton(
                checked = wishlisted,
                onCheckedChange = onWishlistToggle,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    if (wishlisted) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (wishlisted) "Remove from wishlist" else "Add to wishlist",
                    tint = if (wishlisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PriceText(price)
            if (rating != null) RatingBar(rating = rating)
        }
    }
}

/** Neutral placeholder used when a product has no image (or before it loads). */
@Composable
fun ProductImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Category tile: an icon over a label (figma Home category row). */
@Composable
fun CategoryTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Hero / promotion banner (figma "SUMMER EDIT — The quiet luxury edit — Shop now"). */
@Composable
fun HeroBanner(
    eyebrow: String,
    title: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(eyebrow, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.headlineMedium)
            ShrineButton(text = ctaLabel, onClick = onCtaClick, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Section header with an optional "See all" action (figma "New arrivals · See all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("See all")
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Circular "add to cart" action used on wishlist cards (figma Wishlist quick add). */
@Composable
fun QuickAddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = "Add to cart")
    }
}

/** A bordered outlined icon button, e.g. the cart stepper edges in some layouts. */
@Composable
fun ShrineOutlinedIconButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedIconButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription = contentDescription)
    }
}
