package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R


/**
 * A simple [Fragment] subclass.
 * Use the [OrderPlacedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderPlacedFragment : Fragment() {

    lateinit var avd : AnimatedVectorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.shr_order_placed_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        val drawable:Drawable = requireView().findViewById<ImageView>(R.id.done).drawable

        // AnimatedVectorDrawable (framework) only exists on API 21+, so this instanceof check
        // is the guard; no @RequiresApi annotation is needed.
        if (drawable is AnimatedVectorDrawable) {
            avd = drawable
            avd.start()
        }

        requireView().findViewById<View>(R.id.continue_shopping).setOnClickListener{
            (activity as NavigationHost).navigateTo(ProductGridFragment(), false)
        }
    }

}