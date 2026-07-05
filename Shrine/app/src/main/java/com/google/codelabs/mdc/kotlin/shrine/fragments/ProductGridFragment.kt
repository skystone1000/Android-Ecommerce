package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.NavigationIconClickListener
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.adapters.lineargridlayout.ProductCardRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.adapters.staggeredgridlayout.StaggeredProductCardRecyclerViewAdapter
import com.google.codelabs.mdc.kotlin.shrine.database.ProductSeed
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductGridFragment : Fragment() {

    // null = not yet rendered; otherwise tracks which layout is currently shown.
    private var staggeredMode: Boolean? = null

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

        // The RecyclerView is configured in renderGrid() (called from onResume) so that the
        // layout reflects the current "staggered grid" setting, including when returning from
        // the Settings screen.

        return view
    }

    /**
     * Configures the RecyclerView for the current "staggered grid" setting and loads the catalog.
     * No-op if the layout already matches the setting, so it is cheap to call on every resume.
     */
    private fun renderGrid() {
        val staggered = requireContext()
            .getSharedPreferences(SettingsFragment.PREFS_FILE, Context.MODE_PRIVATE)
            .getBoolean(SettingsFragment.KEY_STAGGERED_GRID, false)
        if (staggered == staggeredMode) return
        staggeredMode = staggered

        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)

        if (staggered) {
            // Horizontal 2-row staggered carousel. Needs a bounded height for the horizontal scroll.
            recyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
            recyclerView.layoutParams = recyclerView.layoutParams.apply {
                height = resources.getDimensionPixelSize(R.dimen.shr_staggered_recycler_height)
            }
            val adapter = StaggeredProductCardRecyclerViewAdapter(requireActivity(), mutableListOf())
            recyclerView.adapter = adapter
            loadCatalog { adapter.submit(it) }
        } else {
            recyclerView.layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            recyclerView.layoutParams = recyclerView.layoutParams.apply {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            val adapter = ProductCardRecyclerViewAdapter(requireActivity(), mutableListOf())
            recyclerView.adapter = adapter
            loadCatalog { adapter.submit(it) }
        }
    }

    /**
     * Loads the catalog from the Room `products` table off the main thread, seeding it from the
     * bundled JSON on first run, then delivers it to [onLoaded] on the main thread.
     */
    private fun loadCatalog(onLoaded: (MutableList<Product>) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            val products = withContext(Dispatchers.IO) {
                val dao = ShrineDatabase.getDatabase(requireContext()).productDao()
                if (dao.count() == 0) {
                    dao.insertAll(ProductSeed.read(requireContext()))
                }
                dao.getAll()
            }
            onLoaded(products.toMutableList())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onResume() {
        super.onResume()
        // Apply the current grid-layout setting (re-applies if it changed in Settings).
        renderGrid()
        requireView().findViewById<View>(R.id.grid_profile_button).setOnClickListener{
            (activity as NavigationHost).navigateTo(ProfileFragment(), true)
        }
    }
}
