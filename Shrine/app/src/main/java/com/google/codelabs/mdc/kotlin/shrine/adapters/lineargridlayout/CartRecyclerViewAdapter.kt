package com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.NetworkImageView
import com.google.android.material.imageview.ShapeableImageView
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.auth.Session
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.CartItem
import com.google.codelabs.mdc.kotlin.shrine.network.ImageRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartRecyclerViewAdapter internal constructor(
    val context: Context,
    private var productList: MutableList<CartItem>,
    /** Invoked (on the main thread) after a row is removed so the host can reload totals in place. */
    private val onChanged: () -> Unit = {}
) : RecyclerView.Adapter<CartRecyclerViewAdapter.CartViewHolder>() {

    /** Replace the backing list and refresh the view via DiffUtil. Call on the main thread. */
    fun submit(items: MutableList<CartItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = productList.size
            override fun getNewListSize() = items.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                productList[oldPos].cart_item_id == items[newPos].cart_item_id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                productList[oldPos] == items[newPos]
        })
        productList = items
        diff.dispatchUpdatesTo(this)
    }

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
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener
            val productId = productList[pos].product_id
            val productName = productList[pos].product_name
            Toast.makeText(context, context.getString(R.string.shr_removed_from_cart, productName), Toast.LENGTH_SHORT).show()
            val activity = context as AppCompatActivity
            val userId = Session.currentUserId(activity)
            // Delete this user's row off the main thread, then reload the cart in place on the main
            // thread (no full-fragment recreation).
            activity.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ShrineDatabase.getDatabase(context).cartItemDao().deleteCartItem(productId, userId)
                }
                onChanged()
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

        var productRemove: ShapeableImageView = itemView.findViewById(R.id.cart_delete_item)



    }

}
