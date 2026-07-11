package com.google.codelabs.mdc.kotlin.shrine.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.BottomNavItem
import com.google.codelabs.mdc.kotlin.shrine.designsystem.component.ShrineBottomBar
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
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.AddressesScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.CartScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.CategoryScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.CheckoutScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.EditProfileScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.ForgotPasswordScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.HelpCenterScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.HomeScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.LoginScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.OrderDetailScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.OrderHistoryScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.OrderPlacedScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.PaymentMethodsScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.ProductDetailScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.ProfileScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.RegisterScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.SearchScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.SettingsScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.SplashScreen
import com.google.codelabs.mdc.kotlin.shrine.ui.screens.WishlistScreen

/** Root of the Compose app: brand theme + a single NavHost (auth/main graphs) under a bottom-bar Scaffold. */
@Composable
fun ShrineApp() {
    val appViewModel: AppViewModel = hiltViewModel()
    val themeMode by appViewModel.themeMode.collectAsState()

    ShrineTheme(themeMode = themeMode) {
        val navController = rememberNavController()

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
            // Each screen owns its own top inset (via its TopAppBar / statusBarsPadding); this outer
            // Scaffold only reserves space for the bottom nav bar. Without zeroing insets here the
            // status-bar inset would be applied twice (gap above every screen title).
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (selectedTab >= 0) {
                    ShrineBottomBar(
                        items = bottomItems,
                        selectedIndex = selectedTab,
                        onSelect = { index -> navController.navigateToTab(tabRoutes[index]) },
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
                        LoginScreen(
                            onSignedIn = { navController.navigate(Home) { popUpTo<AuthGraph> { inclusive = true } } },
                            onCreateAccount = { navController.navigate(Register) },
                            onForgotPassword = { navController.navigate(ForgotPassword) },
                        )
                    }
                    composable<Register> {
                        RegisterScreen(
                            onRegistered = { navController.navigate(Home) { popUpTo<AuthGraph> { inclusive = true } } },
                            onBackToSignIn = { navController.popBackStack() },
                        )
                    }
                    composable<ForgotPassword> {
                        ForgotPasswordScreen(onBack = { navController.popBackStack() })
                    }
                }

                // ---------- Main graph ----------
                navigation<MainGraph>(startDestination = Home) {
                    composable<Home> {
                        HomeScreen(
                            onCategory = { id -> navController.navigate(Category(id)) },
                            onProduct = { id -> navController.navigate(ProductDetail(id)) },
                            onSearch = { navController.navigateToTab(Search) },
                            onCart = { navController.navigateToTab(Cart) },
                        )
                    }
                    composable<Search> {
                        SearchScreen(onProduct = { id -> navController.navigate(ProductDetail(id)) })
                    }
                    composable<Cart> {
                        CartScreen(
                            onCheckout = { navController.navigate(Checkout) },
                            onBrowse = { navController.navigateToTab(Home) },
                        )
                    }
                    composable<Wishlist> {
                        WishlistScreen(
                            onProduct = { id -> navController.navigate(ProductDetail(id)) },
                            onBrowse = { navController.navigateToTab(Home) },
                        )
                    }
                    composable<Profile> {
                        ProfileScreen(
                            onOrders = { navController.navigate(OrderHistory) },
                            onAddresses = { navController.navigate(Addresses) },
                            onPayments = { navController.navigate(PaymentMethods) },
                            onEditProfile = { navController.navigate(EditProfile) },
                            onSettings = { navController.navigate(Settings) },
                            onHelp = { navController.navigate(HelpCenter) },
                            onSignIn = { navController.navigate(Login) { popUpTo<MainGraph> { inclusive = true } } },
                            onRegister = { navController.navigate(Register) { popUpTo<MainGraph> { inclusive = true } } },
                            onSignOut = {
                                appViewModel.signOut()
                                navController.navigate(Login) { popUpTo<MainGraph> { inclusive = true } }
                            },
                        )
                    }
                    composable<Category> {
                        CategoryScreen(
                            onProduct = { id -> navController.navigate(ProductDetail(id)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<ProductDetail> {
                        ProductDetailScreen(
                            onBack = { navController.popBackStack() },
                            onProduct = { id -> navController.navigate(ProductDetail(id)) },
                            onAddedToCart = { navController.navigateToTab(Cart) },
                        )
                    }
                    composable<Checkout> {
                        CheckoutScreen(
                            onPlaced = { orderId ->
                                navController.navigate(OrderPlaced(orderId)) { popUpTo<Cart>() }
                            },
                            onBack = { navController.popBackStack() },
                            onChangeAddress = { navController.navigate(Addresses) },
                            onChangePayment = { navController.navigate(PaymentMethods) },
                        )
                    }
                    composable<OrderPlaced> {
                        OrderPlacedScreen(
                            onContinueShopping = { navController.navigateToTab(Home) },
                            onViewOrders = { navController.navigate(OrderHistory) },
                        )
                    }
                    composable<OrderHistory> {
                        OrderHistoryScreen(
                            onOrder = { id -> navController.navigate(OrderDetail(id)) },
                            onBack = { navController.popBackStack() },
                            onBrowse = { navController.navigateToTab(Home) },
                        )
                    }
                    composable<OrderDetail> {
                        OrderDetailScreen(onBack = { navController.popBackStack() })
                    }
                    composable<Addresses> {
                        AddressesScreen(onBack = { navController.popBackStack() })
                    }
                    composable<PaymentMethods> {
                        PaymentMethodsScreen(onBack = { navController.popBackStack() })
                    }
                    composable<EditProfile> {
                        EditProfileScreen(
                            onSaved = { navController.popBackStack() },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<Settings> {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable<HelpCenter> {
                        HelpCenterScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

/** Switches bottom-bar tabs with the standard single-top / save-restore behavior. */
private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
