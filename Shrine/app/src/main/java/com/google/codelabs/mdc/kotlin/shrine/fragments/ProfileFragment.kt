package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.codelabs.mdc.kotlin.shrine.MainActivity
import com.google.codelabs.mdc.kotlin.shrine.R

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.shr_profile_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()

        // Get Data from Shared Prefrences
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
//        val defaultValue = resources.getInteger(R.integer.saved_high_score_default_key)
        val userName = sharedPref.getString("user_name", "Undefined")
        val userEmail = sharedPref.getString("user_email", "Undefined")
        val userPhone = sharedPref.getString("user_phone", "Undefined")

        val view = requireView()
        view.findViewById<TextView>(R.id.textView_username).text = userName
        view.findViewById<TextView>(R.id.textView_email).text = userEmail
        view.findViewById<TextView>(R.id.textView_phone).text = userPhone


        view.findViewById<View>(R.id.button_signOut).setOnClickListener{
            // Clearing Shared Preferences on Logout
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            // Clearing backstack
            val i = Intent(requireContext(), MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)

//            (activity as NavigationHost).navigateTo(LoginFragment(), false)
            // Check how to clear complete backstack
        }
    }
}