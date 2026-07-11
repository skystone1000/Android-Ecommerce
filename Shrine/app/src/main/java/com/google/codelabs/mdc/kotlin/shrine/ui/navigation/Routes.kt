package com.google.codelabs.mdc.kotlin.shrine.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations (kotlinx-serialization). Two nested graphs — [AuthGraph]
 * (pre-session) and [MainGraph] (post-session, bottom-bar scaffold) — mirror the navigation model
 * in plan_8. Args are compile-time checked (e.g. [ProductDetail.id]).
 */

@Serializable data object AuthGraph

@Serializable data object Splash
@Serializable data object Login
@Serializable data object Register
@Serializable data object ForgotPassword

@Serializable data object MainGraph

// Bottom-bar tab roots
@Serializable data object Home
@Serializable data object Search
@Serializable data object Cart
@Serializable data object Wishlist
@Serializable data object Profile

// Pushed destinations
@Serializable data class Category(val id: String)
@Serializable data class ProductDetail(val id: Long)
@Serializable data object Checkout
@Serializable data class OrderPlaced(val orderId: Long)
@Serializable data object OrderHistory
@Serializable data class OrderDetail(val orderId: Long)
@Serializable data object Addresses
@Serializable data object PaymentMethods
@Serializable data object EditProfile
@Serializable data object Settings
@Serializable data object HelpCenter
