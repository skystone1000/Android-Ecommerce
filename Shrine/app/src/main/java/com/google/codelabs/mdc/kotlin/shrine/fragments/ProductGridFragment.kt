package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.NavigationIconClickListener
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout.ProductCardRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.database.ProductSeed
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductGridFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment with the ProductGrid theme
        val view = inflater.inflate(R.layout.shr_product_grid_fragment, container, false)

        // Set up the tool bar
        val appBar = view.findViewById<Toolbar>(R.id.app_bar)
        (activity as AppCompatActivity).setSupportActionBar(appBar)
        appBar.setNavigationOnClickListener(
            NavigationIconClickListener(
                requireActivity(),
                view.findViewById(R.id.product_grid),
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(requireContext(), R.drawable.shr_branded_menu), // Menu open icon
                ContextCompat.getDrawable(requireContext(), R.drawable.shr_close_menu))
        ) // Menu close icon

        // Set up the RecyclerView with an (initially empty) adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        val adapter = ProductCardRecyclerViewAdapter(requireActivity(), mutableListOf())
        recyclerView.adapter = adapter

        loadCatalog(adapter)

        return view
    }

    /**
     * Loads the catalog from the Room `products` table off the main thread, seeding it from the
     * bundled JSON on first run, then publishes it to the adapter on the main thread.
     */
    private fun loadCatalog(adapter: ProductCardRecyclerViewAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            val products = withContext(Dispatchers.IO) {
                val dao = ShrineDatabase.getDatabase(requireContext()).productDao()
                if (dao.count() == 0) {
                    dao.insertAll(ProductSeed.read(requireContext()))
                }
                dao.getAll()
            }
            adapter.submit(products.toMutableList())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onResume() {
        super.onResume()
        requireView().findViewById<View>(R.id.grid_profile_button).setOnClickListener{
            (activity as NavigationHost).navigateTo(ProfileFragment(), true)
        }
    }
}
