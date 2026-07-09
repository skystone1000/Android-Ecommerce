package com.google.codelabs.mdc.kotlin.shrine.designsystem.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search

import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.BottomNavItem
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.CategoryTile
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.EmptyState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ErrorState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.HeroBanner
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.LoadingState
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.OrderStatusKind
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PasswordStrength
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PasswordStrengthMeter
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.PriceText
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ProductCard
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.QuantityStepper
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.QuickAddButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.RatingBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.SectionHeader
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.SegmentOption
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineAvatar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineBottomBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButton
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButtonVariant
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineCheckboxRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineDateField
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineDivider
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineFilterChip
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineListRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrinePasswordField
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrinePriceRangeSlider
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineRadioRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSearchBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSearchField
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSegmentedButtons
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineStatusChip
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineSwitchRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTabRow
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTextField
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineTopBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ThemeMode

/**
 * A scrollable gallery exercising every design-system component. Rendered by the two previews
 * below in both light and dark — this is the Phase 1 exit gate of plan_8.
 */
@Composable
fun ComponentGallery() {
    var quantity by remember { mutableIntStateOf(2) }
    var checked by remember { mutableStateOf(true) }
    var switchOn by remember { mutableStateOf(true) }
    var filterSelected by remember { mutableStateOf(true) }
    var radioIndex by remember { mutableIntStateOf(0) }
    var themeIndex by remember { mutableIntStateOf(0) }
    var tabIndex by remember { mutableIntStateOf(0) }
    var navIndex by remember { mutableIntStateOf(0) }
    var wishlisted by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("head") }
    var range by remember { mutableStateOf(120f..900f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        GroupTitle("Buttons")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShrineButton("Add to cart", onClick = {})
            ShrineButton("Tonal", onClick = {}, variant = ShrineButtonVariant.Tonal)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShrineButton("Outlined", onClick = {}, variant = ShrineButtonVariant.Outlined)
            ShrineButton("Text", onClick = {}, variant = ShrineButtonVariant.Text)
            ShrineButton("Loading", onClick = {}, loading = true)
        }

        GroupTitle("Text fields")
        ShrineTextField(value = "ava@shrine.com", onValueChange = {}, label = "Email", helperText = "We'll never share it")
        ShrineTextField(value = "abc", onValueChange = {}, label = "Email", errorText = "Enter a valid email")
        ShrinePasswordField(value = "secret12", onValueChange = {})
        PasswordStrengthMeter(strength = PasswordStrength.Strong)

        GroupTitle("Search")
        ShrineSearchBar(placeholder = "Search Shrine", onClick = {}, onTuneClick = {})
        ShrineSearchField(query = search, onQueryChange = { search = it })

        GroupTitle("Chips & status")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShrineFilterChip(selected = filterSelected, onClick = { filterSelected = !filterSelected }, label = "Electronics")
            ShrineFilterChip(selected = false, onClick = {}, label = "Fashion")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShrineStatusChip(OrderStatusKind.InTransit)
            ShrineStatusChip(OrderStatusKind.Delivered)
        }

        GroupTitle("Selection")
        ShrineCheckboxRow(checked = checked, onCheckedChange = { checked = it }, label = "I agree to the Terms and Privacy Policy")
        ShrineRadioRow(selected = radioIndex == 0, onSelect = { radioIndex = 0 }, label = "Standard · Free", trailing = "3–5 days")
        ShrineRadioRow(selected = radioIndex == 1, onSelect = { radioIndex = 1 }, label = "Express", trailing = "$12")
        ShrineSwitchRow(checked = switchOn, onCheckedChange = { switchOn = it }, title = "Order updates", subtitle = "Shipping and delivery")
        ShrineSegmentedButtons(
            options = listOf(
                SegmentOption("System", Icons.Rounded.BrightnessAuto),
                SegmentOption("Light", Icons.Rounded.LightMode),
                SegmentOption("Dark", Icons.Rounded.DarkMode),
            ),
            selectedIndex = themeIndex,
            onSelect = { themeIndex = it },
        )
        ShrinePriceRangeSlider(value = range, onValueChange = { range = it }, valueRange = 0f..1500f)

        GroupTitle("Price, rating & stepper")
        PriceText(price = "$1,299", originalPrice = "$1,499")
        RatingBar(rating = 4f, reviewCount = 218)
        QuantityStepper(quantity = quantity, onDecrement = { quantity-- }, onIncrement = { quantity++ })

        GroupTitle("Product card & category")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductCard(
                name = "Aether Wireless",
                price = "$1,299",
                onClick = {},
                rating = 4f,
                wishlisted = wishlisted,
                onWishlistToggle = { wishlisted = it },
                isNew = true,
                modifier = Modifier.weight(1f),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryTile(label = "Audio", icon = Icons.Rounded.Headphones, onClick = {})
                QuickAddButton(onClick = {})
            }
        }

        GroupTitle("Section header, hero & tabs")
        SectionHeader(title = "New arrivals", subtitle = "Fresh this week", onSeeAll = {})
        HeroBanner(eyebrow = "SUMMER EDIT", title = "The quiet luxury edit", ctaLabel = "Shop now", onCtaClick = {})
        ShrineTabRow(titles = listOf("All", "Active", "Delivered"), selectedIndex = tabIndex, onSelect = { tabIndex = it })

        GroupTitle("List rows & avatar")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShrineAvatar(initials = "AM")
        }
        ShrineListRow(title = "My orders", onClick = {}, leadingIcon = Icons.AutoMirrored.Rounded.ReceiptLong)
        ShrineDivider()
        ShrineListRow(title = "Saved", onClick = {}, leadingIcon = Icons.Rounded.Favorite, supporting = "8 items")

        GroupTitle("Bars & date")
        ShrineTopBar(title = "Cart", onBack = {})
        ShrineDateField(label = "Date of birth", value = "14 March 1994", onClick = {})
        ShrineBottomBar(
            items = listOf(
                BottomNavItem("Home", Icons.Rounded.Home),
                BottomNavItem("Search", Icons.Rounded.Search),
                BottomNavItem("Cart", Icons.Rounded.ShoppingBag, badgeCount = 3),
                BottomNavItem("Saved", Icons.Rounded.Favorite),
                BottomNavItem("Profile", Icons.Rounded.Person),
            ),
            selectedIndex = navIndex,
            onSelect = { navIndex = it },
        )

        GroupTitle("States")
        EmptyState(icon = Icons.Rounded.ShoppingBag, title = "Your cart is empty", subtitle = "Discover something you love", actionLabel = "Browse", onAction = {})
        ErrorState(icon = Icons.Rounded.CloudOff, title = "Something went wrong", subtitle = "Check your connection", onRetry = {})
        LoadingState()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryTile(label = "Beauty", icon = Icons.Rounded.Spa, onClick = {})
            CategoryTile(label = "Home", icon = Icons.Rounded.Chair, onClick = {})
            CategoryTile(label = "Fashion", icon = Icons.Rounded.Checkroom, onClick = {})
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Preview(name = "Gallery · Light", showBackground = true, heightDp = 2400)
@Composable
private fun ComponentGalleryLightPreview() {
    ShrineTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = MaterialTheme.colorScheme.background) { ComponentGallery() }
    }
}

@Preview(name = "Gallery · Dark", showBackground = true, heightDp = 2400)
@Composable
private fun ComponentGalleryDarkPreview() {
    ShrineTheme(themeMode = ThemeMode.DARK) {
        Surface(color = MaterialTheme.colorScheme.background) { ComponentGallery() }
    }
}
