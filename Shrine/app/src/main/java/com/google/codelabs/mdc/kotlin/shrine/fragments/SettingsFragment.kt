package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.codelabs.mdc.kotlin.shrine.R

/**
 * Simple settings screen. Currently exposes one toggle: whether the product grid is shown in the
 * regular grid layout or the asymmetric staggered layout. The choice is persisted in a named
 * SharedPreferences file so it survives sign-out.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.shr_settings_fragment, container, false)

        val prefs = requireContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val staggeredSwitch = view.findViewById<SwitchMaterial>(R.id.settings_staggered_switch)
        staggeredSwitch.isChecked = prefs.getBoolean(KEY_STAGGERED_GRID, false)
        staggeredSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_STAGGERED_GRID, isChecked).apply()
        }

        return view
    }

    companion object {
        const val PREFS_FILE = "shrine_settings"
        const val KEY_STAGGERED_GRID = "staggered_grid"
    }
}
