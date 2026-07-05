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
import com.google.codelabs.mdc.kotlin.shrine.auth.Session
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.database.addOrIncrement
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
            Toast.makeText(context, context.getString(R.string.shr_added_to_cart, product.product_name), Toast.LENGTH_SHORT).show()
            val activity = context as AppCompatActivity
            val userId = Session.currentUserId(activity)
            val database = ShrineDatabase.getDatabase(context)
            activity.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    database.cartItemDao().addOrIncrement(userId, product)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
