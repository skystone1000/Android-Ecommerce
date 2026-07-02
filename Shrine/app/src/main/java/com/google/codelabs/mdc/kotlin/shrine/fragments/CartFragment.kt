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
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

        // Set up the RecyclerView with an (initially empty) adapter
        val cartRecyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        cartRecyclerView.setHasFixedSize(true)
        cartRecyclerView.layoutManager = GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
        adapter = CartRecyclerViewAdapter(requireActivity(), mutableListOf())
        cartRecyclerView.adapter = adapter

        // Item Listener - Clear Cart
        view.findViewById<View>(R.id.cart_clear_icon).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ShrineDatabase.getDatabase(requireContext()).cartItemDao().clearCart()
                }
                (activity as NavigationHost).navigateTo(CartFragment(), false)
            }
        }

        // Item Listener - Checkout
        view.findViewById<View>(R.id.cart_checkout).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ShrineDatabase.getDatabase(requireContext()).cartItemDao().clearCart()
                }
                (activity as NavigationHost).navigateTo(OrderPlacedFragment(), false)
            }
        }

        loadCart(view)
        return view
    }

    /**
     * Loads the cart off the main thread, groups duplicate products into a per-product
     * quantity, then updates the adapter and totals back on the main thread.
     */
    private fun loadCart(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                val rows = ShrineDatabase.getDatabase(requireContext()).cartItemDao().getAll()
                regroupByProduct(rows)
            }
            adapter.submit(items)
            updateTotals(view, items)
        }
    }

    /** Collapse rows that share a product_id into one entry whose quantity is the row count. */
    private fun regroupByProduct(rows: List<CartItem>): MutableList<CartItem> {
        val grouped = LinkedHashMap<Long, CartItem>()
        for (item in rows) {
            val existing = grouped[item.product_id]
            if (existing == null) {
                item.product_quantity = "1"
                grouped[item.product_id] = item
            } else {
                val newQuantity = (existing.product_quantity.toIntOrNull() ?: 0) + 1
                existing.product_quantity = newQuantity.toString()
            }
        }
        return grouped.values.toMutableList()
    }

    /** Update the count + total price. Parsing is defensive so bad data can't crash the screen. */
    private fun updateTotals(view: View, items: List<CartItem>) {
        var totalCost = 0
        for (cartItem in items) {
            val price = cartItem.product_price.removePrefix("$").trim().toIntOrNull() ?: 0
            val quantity = cartItem.product_quantity.toIntOrNull() ?: 0
            totalCost += price * quantity
        }

        view.findViewById<TextView>(R.id.cart_items_total_value).text = items.size.toString()
        view.findViewById<TextView>(R.id.cart_items_price_value).text = "$totalCost $"
    }
}
