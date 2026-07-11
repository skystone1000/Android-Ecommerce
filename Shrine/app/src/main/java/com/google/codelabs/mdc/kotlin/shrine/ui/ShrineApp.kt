package com.google.codelabs.mdc.kotlin.shrine.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.BottomNavItem
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineBottomBar
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineButtonVariant
import com.google.codelabs.mdc.kotlin.shrine.designsystem.theme.ShrineTheme
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Addresses
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.AuthGraph
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Cart
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Category
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Checkout
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.EditProfile
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.ForgotPassword
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.HelpCenter
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Home
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Login
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.MainGraph
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.OrderDetail
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.OrderHistory
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.OrderPlaced
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.PaymentMethods
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.ProductDetail
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Profile
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Register
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Search
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Settings
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Splash
import com.google.codelabs.mdc.kotlin.shrine.ui.navigation.Wishlist
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.SplashScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.StubAction
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.StubScreen

/** Root of the Compose app: brand theme + a single NavHost (auth/main graphs) under a bottom-bar Scaffold. */
@Composable
fun ShrineApp() {
    ShrineTheme {
        val navController = rememberNavController()
        val appViewModel: AppViewModel = hiltViewModel()

        val backStackEntry by navController.currentBackStackEntryAsState()
        val current = backStackEntry?.destination
        // Type-safe routes are stored as the destination's qualified serial name (args after "/").
        val routeName = current?.route?.substringBefore("/")
        val selectedTab = when (routeName) {
            Home::class.qualifiedName -> 0
            Search::class.qualifiedName -> 1
            Cart::class.qualifiedName -> 2
            Wishlist::class.qualifiedName -> 3
            Profile::class.qualifiedName -> 4
            else -> -1
        }
        val tabRoutes = listOf<Any>(Home, Search, Cart, Wishlist, Profile)
        val bottomItems = listOf(
            BottomNavItem("Home", Icons.Rounded.Home),
            BottomNavItem("Search", Icons.Rounded.Search),
            BottomNavItem("Cart", Icons.Rounded.ShoppingBag),
            BottomNavItem("Saved", Icons.Rounded.FavoriteBorder),
            BottomNavItem("Profile", Icons.Rounded.Person),
        )

        Scaffold(
            bottomBar = {
                if (selectedTab >= 0) {
                    ShrineBottomBar(
                        items = bottomItems,
                        selectedIndex = selectedTab,
                        onSelect = { index ->
                            navController.navigate(tabRoutes[index]) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = AuthGraph,
                modifier = Modifier.padding(padding),
            ) {
                // ---------- Auth graph ----------
                navigation<AuthGraph>(startDestination = Splash) {
                    composable<Splash> {
                        SplashScreen(appViewModel = appViewModel) { session ->
                            if (session != null) {
                                navController.navigate(Home) { popUpTo<AuthGraph> { inclusive = true } }
                            } else {
                                navController.navigate(Login) { popUpTo<Splash> { inclusive = true } }
                            }
                        }
                    }
                    composable<Login> {
                        StubScreen(
                            title = "Login",
                            subtitle = "Welcome back — sign in to continue shopping",
                            actions = listOf(
                                StubAction("Sign in", ShrineButtonVariant.Filled) {
                                    appViewModel.demoSignIn()
                                    navController.navigate(Home) { popUpTo<AuthGraph> { inclusive = true } }
                                },
                                StubAction("Skip — browse as guest", ShrineButtonVariant.Text) {
                                    appViewModel.continueAsGuest()
                                    navController.navigate(Home) { popUpTo<AuthGraph> { inclusive = true } }
                                },
                                StubAction("Create account") { navController.navigate(Register) },
                                StubAction("Forgot password?", ShrineButtonVariant.Text) { navController.navigate(ForgotPassword) },
                            ),
                        )
                    }
                    composable<Register> {
                        StubScreen(
                            title = "Create account",
                            actions = listOf(
                                StubAction("Create account", ShrineButtonVariant.Filled) { navController.popBackStack() },
                                StubAction("Back to sign in", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<ForgotPassword> {
                        StubScreen(
                            title = "Forgot password",
                            subtitle = "Placeholder — no backend in this demo",
                            actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }),
                        )
                    }
                }

                // ---------- Main graph ----------
                navigation<MainGraph>(startDestination = Home) {
                    composable<Home> {
                        StubScreen(
                            title = "Home",
                            subtitle = "Catalog · promo · categories",
                            actions = listOf(
                                StubAction("Open category") { navController.navigate(Category("audio")) },
                                StubAction("Open product") { navController.navigate(ProductDetail(1)) },
                                StubAction("Search") { navController.navigate(Search) },
                            ),
                        )
                    }
                    composable<Search> {
                        StubScreen(
                            title = "Search",
                            actions = listOf(StubAction("Open product") { navController.navigate(ProductDetail(2)) }),
                        )
                    }
                    composable<Cart> {
                        StubScreen(
                            title = "Cart",
                            actions = listOf(StubAction("Checkout", ShrineButtonVariant.Filled) { navController.navigate(Checkout) }),
                        )
                    }
                    composable<Wishlist> {
                        StubScreen(
                            title = "Saved",
                            actions = listOf(StubAction("Open product") { navController.navigate(ProductDetail(3)) }),
                        )
                    }
                    composable<Profile> {
                        StubScreen(
                            title = "Profile",
                            actions = listOf(
                                StubAction("Order history") { navController.navigate(OrderHistory) },
                                StubAction("Addresses") { navController.navigate(Addresses) },
                                StubAction("Payment methods") { navController.navigate(PaymentMethods) },
                                StubAction("Edit profile") { navController.navigate(EditProfile) },
                                StubAction("Settings") { navController.navigate(Settings) },
                                StubAction("Help center") { navController.navigate(HelpCenter) },
                                StubAction("Sign out", ShrineButtonVariant.Text) {
                                    appViewModel.signOut()
                                    navController.navigate(Login) { popUpTo<MainGraph> { inclusive = true } }
                                },
                            ),
                        )
                    }
                    composable<Category> { entry ->
                        val category = entry.toRoute<Category>()
                        StubScreen(
                            title = "Category: ${category.id}",
                            actions = listOf(
                                StubAction("Open product") { navController.navigate(ProductDetail(1)) },
                                StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<ProductDetail> { entry ->
                        val product = entry.toRoute<ProductDetail>()
                        StubScreen(
                            title = "Product #${product.id}",
                            actions = listOf(
                                StubAction("Add to cart → Cart", ShrineButtonVariant.Filled) { navController.navigate(Cart) },
                                StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<Checkout> {
                        StubScreen(
                            title = "Checkout",
                            actions = listOf(
                                StubAction("Place order", ShrineButtonVariant.Filled) { navController.navigate(OrderPlaced(1)) },
                                StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<OrderPlaced> { entry ->
                        val placed = entry.toRoute<OrderPlaced>()
                        StubScreen(
                            title = "Order placed",
                            subtitle = "Order #${placed.orderId}",
                            actions = listOf(
                                StubAction("Continue shopping", ShrineButtonVariant.Filled) {
                                    navController.navigate(Home) { popUpTo<MainGraph> { inclusive = false }; launchSingleTop = true }
                                },
                                StubAction("View orders") { navController.navigate(OrderHistory) },
                            ),
                        )
                    }
                    composable<OrderHistory> {
                        StubScreen(
                            title = "Order history",
                            actions = listOf(
                                StubAction("Open order") { navController.navigate(OrderDetail(1)) },
                                StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<OrderDetail> { entry ->
                        val order = entry.toRoute<OrderDetail>()
                        StubScreen(
                            title = "Order #${order.orderId}",
                            actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }),
                        )
                    }
                    composable<Addresses> {
                        StubScreen("Addresses", actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }))
                    }
                    composable<PaymentMethods> {
                        StubScreen("Payment methods", actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }))
                    }
                    composable<EditProfile> {
                        StubScreen(
                            title = "Edit profile",
                            actions = listOf(
                                StubAction("Save", ShrineButtonVariant.Filled) { navController.popBackStack() },
                                StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() },
                            ),
                        )
                    }
                    composable<Settings> {
                        StubScreen("Settings", actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }))
                    }
                    composable<HelpCenter> {
                        StubScreen(
                            title = "Help center",
                            subtitle = "Placeholder — no backend in this demo",
                            actions = listOf(StubAction("Back", ShrineButtonVariant.Text) { navController.popBackStack() }),
                        )
                    }
                }
            }
        }
    }
}
