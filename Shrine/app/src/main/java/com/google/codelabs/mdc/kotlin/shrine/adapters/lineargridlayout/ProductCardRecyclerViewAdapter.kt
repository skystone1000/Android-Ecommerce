package com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.NetworkImageView
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.network.ImageRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter used to show a simple grid of products.
 */
class ProductCardRecyclerViewAdapter internal constructor(
    val context: Context, private var productList: MutableList<Product>
) : RecyclerView.Adapter<ProductCardRecyclerViewAdapter.ProductCardViewHolder>() {

    /** Replace the backing list and refresh the view. Call on the main thread. */
    @SuppressLint("NotifyDataSetChanged")
    fun submit(items: MutableList<Product>) {
        productList = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCardViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.shr_product_card, parent, false)
        return ProductCardViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ProductCardViewHolder, position: Int) {
        if (position < productList.size) {
            val product = productList[position]
            holder.productTitle.text = product.product_name
            holder.productPrice.text = product.product_price + " $"
            // Show a branded placeholder while loading and if the image URL fails to resolve,
            // so cards are never blank.
            holder.productImage.setDefaultImageResId(R.drawable.shr_logo)
            holder.productImage.setErrorImageResId(R.drawable.shr_logo)
            ImageRequester.setImageFromUrl(holder.productImage, product.product_url)
        }

        holder.itemView.setOnClickListener{
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener
            val product = productList[pos]
            Toast.makeText(context, "Item: " + product.product_name + " Added to cart", Toast.LENGTH_SHORT ).show()
            val database = ShrineDatabase.getDatabase(context)
            (context as AppCompatActivity).lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    database.cartItemDao().insertCartItem(
                        CartItem(0, product.product_id, product.product_name, product.product_price, product.product_url, "1")
                    )
                }
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
    }
}
