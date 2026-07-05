package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout.CartRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.auth.Session
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * Use the [CartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment() {

    private lateinit var adapter: CartRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment with the ProductGrid theme
        val view = inflater.inflate(R.layout.shr_cart_fragment, container, false)

        // Set up the RecyclerView with an (initially empty) adapter. Removing a row reloads the
        // cart in place via the callback (no full-fragment recreation).
        val cartRecyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        cartRecyclerView.setHasFixedSize(true)
        cartRecyclerView.layoutManager = GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
        adapter = CartRecyclerViewAdapter(requireActivity(), mutableListOf()) { loadCart(view) }
        cartRecyclerView.adapter = adapter

        val userId = Session.currentUserId(requireActivity())

        // Item Listener - Clear Cart (this user's items only)
        view.findViewById<View>(R.id.cart_clear_icon).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ShrineDatabase.getDatabase(requireContext()).cartItemDao().clearCart(userId)
                }
                loadCart(view)
            }
        }

        // Item Listener - Checkout (clears this user's cart, then confirms)
        view.findViewById<View>(R.id.cart_checkout).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ShrineDatabase.getDatabase(requireContext()).cartItemDao().clearCart(userId)
                }
                (activity as NavigationHost).navigateTo(OrderPlacedFragment(), false)
            }
        }

        loadCart(view)
        return view
    }

    /**
     * Loads the current user's cart off the main thread (one row per product, with a real
     * quantity), then updates the adapter and totals back on the main thread.
     */
    private fun loadCart(view: View) {
        val userId = Session.currentUserId(requireActivity())
        viewLifecycleOwner.lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                ShrineDatabase.getDatabase(requireContext()).cartItemDao().getAll(userId)
            }
            adapter.submit(items)
            updateTotals(view, items)
        }
    }

    /** Update the count + total price. Parsing is defensive so bad data can't crash the screen. */
    private fun updateTotals(view: View, items: List<CartItem>) {
        var totalCost = 0.0
        for (cartItem in items) {
            // Currency-safe: decimals/`$`-prefixes/garbage no longer silently become 0 via toInt.
            val price = cartItem.product_price.removePrefix("$").trim().toDoubleOrNull() ?: 0.0
            val quantity = cartItem.product_quantity.toIntOrNull() ?: 0
            totalCost += price * quantity
        }

        view.findViewById<TextView>(R.id.cart_items_total_value).text = items.size.toString()
        view.findViewById<TextView>(R.id.cart_items_price_value).text =
            getString(R.string.shr_price_format, formatAmount(totalCost))

        // Empty-cart guard (B23): clearing or checking out an empty cart is a no-op order.
        val hasItems = items.isNotEmpty()
        view.findViewById<View>(R.id.cart_checkout).isEnabled = hasItems
        view.findViewById<View>(R.id.cart_clear_icon).isEnabled = hasItems
    }

    /** Render whole amounts without a trailing ".0"; show two decimals otherwise. */
    private fun formatAmount(amount: Double): String =
        if (amount % 1.0 == 0.0) amount.toLong().toString()
        else String.format(Locale.US, "%.2f", amount)
}
