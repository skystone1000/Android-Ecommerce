package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.User
import kotlinx.android.synthetic.main.shr_login_fragment.*
import kotlinx.android.synthetic.main.shr_login_fragment.view.*
import kotlinx.coroutines.*


/**
 * Fragment representing the login screen for Shrine.
 */
class LoginFragment : Fragment() {

    lateinit var database: ShrineDatabase
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.shr_login_fragment, container, false)

        // Set an error if the password is less than 8 characters.
        view.login_button.setOnClickListener {
            if(user_edit_text.text.toString().isEmpty()) {
                user_text_input.error = "UserName should not be Empty"
            } else if (!isPasswordValid(password_edit_text.text.toString())) {
                user_text_input.error = null
                password_text_input.error = getString(R.string.shr_error_password)
            } else {
                password_text_input.error = null // Clear the error

                CoroutineScope(Dispatchers.IO).launch {
                    userLogin()
                }
            }
        }

        view.register_button.setOnClickListener {
            (activity as NavigationHost).navigateTo(RegisterFragment(), true)
        }

        return view
    }

    private fun isPasswordValid(text: String): Boolean {
        return text != null && text.length >= 8
    }

    private suspend fun userLogin(){
        // Get User data from Database
        database = ShrineDatabase.getDatabase(requireContext())
        var user: User? = database.userDao().getLogin(user_edit_text.text.toString())

        // Authenticate User
        if(user?.user_pass == password_edit_text.text.toString()){
            // Add user details to shared pref
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString("user_id", user.user_id.toString())
                putString("user_name", user.user_name)
                putString("user_email", user.user_email)
                putString("user_phone", user.user_phone)
                apply()
            }

            // Navigate to ProductGrid
            (activity as NavigationHost).navigateTo(ProductGridFragment(), false)
        }else{
//            Toast.makeText(requireContext(), "Invalid Username or Password", Toast.LENGTH_SHORT).show()
        }

    }
}
