package com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.NetworkImageView
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.network.ImageRequester

import com.google.codelabs.mdc.kotlin.shrine.network.ProductEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Adapter used to show a simple grid of products.
 */
class ProductCardRecyclerViewAdapter internal constructor(
    val context: Context, private val productList: List<Product>
) : RecyclerView.Adapter<ProductCardRecyclerViewAdapter.ProductCardViewHolder>() {

    lateinit var database: ShrineDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCardViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.shr_product_card, parent, false)
        return ProductCardViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ProductCardViewHolder, position: Int) {
        if (position < productList.size) {
            val product = productList[position]
            holder.productTitle.text = product.product_name
            holder.productPrice.text = product.product_price + " $"
            ImageRequester.setImageFromUrl(holder.productImage, product.product_url)
        }

        holder.itemView.setOnClickListener{
            Toast.makeText(context, "Item: " + productList[position].product_name + " Added to cart", Toast.LENGTH_SHORT ).show()
            database = ShrineDatabase.getDatabase(context)
            GlobalScope.launch {
                database.cartItemDao().insertCartItem(CartItem(0,productList[position].product_id, productList[position].product_name, productList[position].product_price, productList[position].product_url, "1"))
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productImage: NetworkImageView = itemView.findViewById(R.id.product_image)
        var productTitle: TextView = itemView.findViewById(R.id.product_title)
        var productPrice: TextView = itemView.findViewById(R.id.product_price)
//        var productCount: CounterView = itemView.findViewById(R.id.counterView)
    }
}

