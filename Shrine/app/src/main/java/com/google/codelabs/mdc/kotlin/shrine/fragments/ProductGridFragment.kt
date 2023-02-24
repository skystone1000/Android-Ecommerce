package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.NavigationIconClickListener
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout.ProductCardRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import kotlinx.android.synthetic.main.shr_backdrop.*
import kotlinx.android.synthetic.main.shr_product_grid_fragment.view.*

class ProductGridFragment : Fragment() {
    lateinit var database: ShrineDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment with the ProductGrid theme
        val view = inflater.inflate(R.layout.shr_product_grid_fragment, container, false)

        // Set up the tool bar
        (activity as AppCompatActivity).setSupportActionBar(view.app_bar)
        view.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                requireActivity(),
                view.product_grid,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(requireContext(), R.drawable.shr_branded_menu), // Menu open icon
                ContextCompat.getDrawable(requireContext(), R.drawable.shr_close_menu))
        ) // Menu close icon

        // Items
//        var products = listOf(
//            ProductEntry("iPhone 9", "https://i.dummyjson.com/data/products/1/thumbnail.jpg", "https://i.dummyjson.com/data/products/1/thumbnail.jpg", "549", "An apple mobile which is nothing like apple"),
//            ProductEntry("iPhone X", "https://i.dummyjson.com/data/products/2/2.jpg", "https://i.dummyjson.com/data/products/2/thumbnail.jpg", "654", "An apple mobile which is nothing like apple"),
//            ProductEntry("Samsung Universe 9", "https://i.dummyjson.com/data/products/3/2.jpg", "https://i.dummyjson.com/data/products/3/thumbnail.jpg", "4863", "An apple mobile which is nothing like apple"),
//            ProductEntry("OPPOF19", "https://i.dummyjson.com/data/products/4/1.jpg", "https://i.dummyjson.com/data/products/4/thumbnail.jpg", "3453", "An apple mobile which is nothing like apple"),
//            ProductEntry("Huawei P30", "https://i.dummyjson.com/data/products/5/1.jpg", "https://i.dummyjson.com/data/products/5/thumbnail.jpg", "766", "An apple mobile which is nothing like apple"),
//            ProductEntry("MacBook Pro", "https://i.dummyjson.com/data/products/6/1.jpg", "https://i.dummyjson.com/data/products/6/2.jpg", "6512", "An apple mobile which is nothing like apple"),
//            ProductEntry("Samsung Galaxy Book", "https://i.dummyjson.com/data/products/7/1.jpg", "https://i.dummyjson.com/data/products/7/thumbnail.jpg", "7863", "An apple mobile which is nothing like apple"),
//            ProductEntry("Microsoft Surface Laptop 4", "https://i.dummyjson.com/data/products/8/1.jpg", "https://i.dummyjson.com/data/products/8/thumbnail.jpg", "132", "An apple mobile which is nothing like apple"),
//            ProductEntry("Infinix INBOOK", "https://i.dummyjson.com/data/products/9/1.jpg", "https://i.dummyjson.com/data/products/9/thumbnail.jpg", "453", "An apple mobile which is nothing like apple"),
//            ProductEntry("HP Pavilion 15-DK1056WM", "https://i.dummyjson.com/data/products/10/1.jpg", "https://i.dummyjson.com/data/products/10/1.jpg", "549", "An apple mobile which is nothing like apple"),
//            ProductEntry("perfume Oil", "https://i.dummyjson.com/data/products/11/1.jpg", "https://i.dummyjson.com/data/products/11/thumbnail.jpg", "1746", "An apple mobile which is nothing like apple"),
//            ProductEntry("Brown Perfume", "https://i.dummyjson.com/data/products/12/1.jpg", "https://i.dummyjson.com/data/products/12/thumbnail.jpg", "6547", "An apple mobile which is nothing like apple"),
//            ProductEntry("Fog Scent Xpressio Perfume", "https://i.dummyjson.com/data/products/13/1.jpg", "https://i.dummyjson.com/data/products/13/1.jpg", "4567", "An apple mobile which is nothing like apple"),
//            ProductEntry("Non-Alcoholic Concentrated Perfume Oil", "https://i.dummyjson.com/data/products/14/1.jpg", "https://i.dummyjson.com/data/products/14/thumbnail.jpg", "4568", "An apple mobile which is nothing like apple"),
//            ProductEntry("Eau De Perfume Spray", "https://i.dummyjson.com/data/products/15/1.jpg", "https://i.dummyjson.com/data/products/15/thumbnail.jpg", "4105", "An apple mobile which is nothing like apple"),
//            ProductEntry("Hyaluronic Acid Serum", "https://i.dummyjson.com/data/products/16/1.jpg", "https://i.dummyjson.com/data/products/16/thumbnail.jpg", "85305", "An apple mobile which is nothing like apple"),
//            ProductEntry("Tree Oil 30ml", "https://i.dummyjson.com/data/products/17/1.jpg", "https://i.dummyjson.com/data/products/17/thumbnail.jpg", "7820", "An apple mobile which is nothing like apple")
//        )

        var products = listOf(
            Product(0,"iPhone 9", "234", "https://i.dummyjson.com/data/products/1/thumbnail.jpg", "549"),
            Product(1,"iPhone X", "56", "https://i.dummyjson.com/data/products/2/thumbnail.jpg", "654"),
            Product(2,"Samsung Universe 9", "123", "https://i.dummyjson.com/data/products/3/thumbnail.jpg", "4863"),
            Product(3,"OPPOF19", "135", "https://i.dummyjson.com/data/products/4/thumbnail.jpg", "3453"),
            Product(4,"Huawei P30", "786", "https://i.dummyjson.com/data/products/5/thumbnail.jpg", "766"),
            Product(5,"MacBook Pro", "1385", "https://i.dummyjson.com/data/products/6/2.jpg", "6512"),
            Product(6,"Samsung Galaxy Book", "788", "https://i.dummyjson.com/data/products/7/thumbnail.jpg", "7863"),
            Product(7,"Microsoft Surface Laptop 4", "586", "https://i.dummyjson.com/data/products/8/thumbnail.jpg", "132"),
            Product(8,"Infinix INBOOK", "241", "https://i.dummyjson.com/data/products/9/thumbnail.jpg", "453"),
            Product(9,"HP Pavilion 15-DK1056WM", "198", "https://i.dummyjson.com/data/products/10/1.jpg", "549"),
            Product(10,"perfume Oil", "954", "https://i.dummyjson.com/data/products/11/thumbnail.jpg", "1746"),
            Product(11,"Brown Perfume", "357", "https://i.dummyjson.com/data/products/12/thumbnail.jpg", "6547"),
            Product(12,"Fog Scent Xpressio Perfume", "849", "https://i.dummyjson.com/data/products/13/1.jpg", "4567"),
            Product(13,"Non-Alcoholic Concentrated Perfume Oil", "126", "https://i.dummyjson.com/data/products/14/thumbnail.jpg", "4568"),
            Product(14,"Eau De Perfume Spray", "417", "https://i.dummyjson.com/data/products/15/thumbnail.jpg", "4105"),
            Product(15,"Hyaluronic Acid Serum", "258", "https://i.dummyjson.com/data/products/16/thumbnail.jpg", "85305"),
            Product(16,"Tree Oil 30ml", "371", "https://i.dummyjson.com/data/products/17/thumbnail.jpg", "7820")
        )


        // Set up the RecyclerView
        view.recycler_view.setHasFixedSize(true)
        view.recycler_view.layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        val adapter = ProductCardRecyclerViewAdapter(requireActivity(), products)
        view.recycler_view.adapter = adapter




        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onResume() {
        super.onResume()
        grid_profile_button.setOnClickListener{
            (activity as NavigationHost).navigateTo(ProfileFragment(), true)
        }
    }
}
