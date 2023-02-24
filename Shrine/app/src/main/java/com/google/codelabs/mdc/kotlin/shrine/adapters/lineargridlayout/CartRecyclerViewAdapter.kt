package com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.NetworkImageView
import com.google.android.material.imageview.ShapeableImageView
import com.google.codelabs.mdc.kotlin.shrine.MainActivity
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.fragments.CartFragment
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.network.ImageRequester
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CartRecyclerViewAdapter internal constructor(
    val context: Context, private val productList: List<CartItem>
) : RecyclerView.Adapter<CartRecyclerViewAdapter.CartViewHolder>() {

    lateinit var database: ShrineDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.shr_cart_item, parent, false)
        return CartViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        if (position < productList.size) {
            val product = productList[position]
            holder.productTitle.text = "Item: " + product.product_name
            holder.productPrice.text = "Price: " + product.product_price + " $"
            holder.productCount.text = "Quantity: " + product.product_quantity
            ImageRequester.setImageFromUrl(holder.productImage, product.product_url)
        }

        holder.productRemove.setOnClickListener{
            Toast.makeText(context, "Item: " + productList[position].product_name + " Removed from the cart", Toast.LENGTH_SHORT ).show()
            database = ShrineDatabase.getDatabase(context)
            GlobalScope.launch {
                database.cartItemDao().deleteCartItem(productList[position].product_id)
                (context as MainActivity).navigateTo(CartFragment(), false)
            }
        }

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productImage: NetworkImageView = itemView.findViewById(R.id.cart_product_image)
        var productTitle: TextView = itemView.findViewById(R.id.cart_item_title)
        var productPrice: TextView = itemView.findViewById(R.id.cart_item_price)
        var productCount: TextView = itemView.findViewById(R.id.cart_item_quantity)
//        val totalQuantity: TextView = itemView.findViewById(R.id.cart_items_total_value)
//        val totalPrice: TextView = itemView.findViewById(R.id.cart_items_price_value)

        var productRemove: ShapeableImageView = itemView.findViewById(R.id.cart_delete_item)



    }

}