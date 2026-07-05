package com.google.codelabs.mdc.kotlin.shrine.adapters.staggeredgridlayout

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView

import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.codelabs.mdc.kotlin.shrine.network.ImageRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter used to show an asymmetric grid of products, with 2 items in the first column, and 1
 * item in the second column, and so on. Used by the product grid when the "Staggered product grid"
 * setting is enabled.
 */
class StaggeredProductCardRecyclerViewAdapter internal constructor(
    val context: Context, private var productList: MutableList<Product>
) : RecyclerView.Adapter<StaggeredProductCardViewHolder>() {

    /** Replace the backing list and refresh the view. Call on the main thread. */
    @SuppressLint("NotifyDataSetChanged")
    fun submit(items: MutableList<Product>) {
        productList = items
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position % 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaggeredProductCardViewHolder {
        var layoutId = R.layout.shr_staggered_product_card_first
        if (viewType == 1) {
            layoutId = R.layout.shr_staggered_product_card_second
        } else if (viewType == 2) {
            layoutId = R.layout.shr_staggered_product_card_third
        }

        val layoutView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return StaggeredProductCardViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: StaggeredProductCardViewHolder, position: Int) {
        if (position < productList.size) {
            val product = productList[position]
            holder.productTitle.text = product.product_name
            holder.productPrice.text = product.product_price + " $"
            holder.productImage.setDefaultImageResId(R.drawable.shr_logo)
            holder.productImage.setErrorImageResId(R.drawable.shr_logo)
            ImageRequester.setImageFromUrl(holder.productImage, product.product_url)
        }

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener
            val product = productList[pos]
            Toast.makeText(context, "Item: " + product.product_name + " Added to cart", Toast.LENGTH_SHORT).show()
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
}
