package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout.CartRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import kotlinx.android.synthetic.main.shr_cart_fragment.*
import kotlinx.android.synthetic.main.shr_cart_fragment.view.*
import kotlinx.android.synthetic.main.shr_product_grid_fragment.view.*
import kotlinx.coroutines.*


/**
 * A simple [Fragment] subclass.
 * Use the [CartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment() {
    lateinit var database: ShrineDatabase
    var cartList: MutableList<CartItem> = mutableListOf(CartItem(0,0,"Test","123", "", "10"))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // Initializations
        CoroutineScope(Dispatchers.IO).launch {
            initialization()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment with the ProductGrid theme
        val view = inflater.inflate(R.layout.shr_cart_fragment, container, false)

        // Set up the RecyclerView
        view.cartRecyclerView.setHasFixedSize(true)
        view.cartRecyclerView.layoutManager = GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
        val adapter = CartRecyclerViewAdapter(requireActivity(), cartList)
        view.cartRecyclerView.adapter = adapter


        return view
    }

    suspend fun initialization() {
        database = ShrineDatabase.getDatabase(requireContext())
        val job = GlobalScope.launch {
            cartList = database.cartItemDao().getAll()

            // Regrouping the Cart Items
            var hashMap : HashMap<Long, CartItem> = HashMap<Long, CartItem> ()
            for(item in cartList){
                item.product_quantity = 0.toString()
                hashMap[item.product_id] = item
            }
            for(item in cartList){
                var currQuantity = hashMap[item.product_id]?.product_quantity?.toInt()
                currQuantity = currQuantity?.plus(1)
                hashMap[item.product_id]?.product_quantity = currQuantity.toString()
            }

            var flag = 0
            for(id in hashMap){
                if(flag == 0){
                    cartList = mutableListOf(id.component2())
                    flag = 1
                }else{
                    cartList.add(id.component2())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update Totals (At the bottom of the cart)
        var totalCost = 0
        for (cartItem in cartList) {
            totalCost += cartItem.product_price.toInt() * cartItem.product_quantity.toInt()
        }

        cart_items_total_value.text = cartList.size.toString()
        cart_items_price_value.text = totalCost.toString() + " $"


        // Item Listener - Clear Cart
        cart_clear_icon.setOnClickListener{
            val job = GlobalScope.launch {
                database.cartItemDao().clearCart()
                (activity as NavigationHost).navigateTo(CartFragment(), false)
            }
        }

        // Item Listener - Checkout
        cart_checkout.setOnClickListener{
            GlobalScope.launch {
                database.cartItemDao().clearCart()
                (activity as NavigationHost).navigateTo(OrderPlacedFragment(), false)
            }
        }
    }



}