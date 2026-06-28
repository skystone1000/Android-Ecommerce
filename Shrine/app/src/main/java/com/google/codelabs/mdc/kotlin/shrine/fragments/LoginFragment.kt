package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.User
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

        val userEditText = view.findViewById<TextInputEditText>(R.id.user_edit_text)
        val userTextInput = view.findViewById<TextInputLayout>(R.id.user_text_input)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val passwordTextInput = view.findViewById<TextInputLayout>(R.id.password_text_input)

        // Set an error if the password is less than 8 characters.
        view.findViewById<View>(R.id.login_button).setOnClickListener {
            if(userEditText.text.toString().isEmpty()) {
                userTextInput.error = "UserName should not be Empty"
            } else if (!isPasswordValid(passwordEditText.text.toString())) {
                userTextInput.error = null
                passwordTextInput.error = getString(R.string.shr_error_password)
            } else {
                passwordTextInput.error = null // Clear the error

                CoroutineScope(Dispatchers.IO).launch {
                    userLogin()
                }
            }
        }

        view.findViewById<View>(R.id.register_button).setOnClickListener {
            (activity as NavigationHost).navigateTo(RegisterFragment(), true)
        }

        return view
    }

    private fun isPasswordValid(text: String): Boolean {
        return text != null && text.length >= 8
    }

    private suspend fun userLogin(){
        // Get User data from Database
        val view = requireView()
        val username = view.findViewById<TextInputEditText>(R.id.user_edit_text).text.toString()
        val password = view.findViewById<TextInputEditText>(R.id.password_edit_text).text.toString()
        database = ShrineDatabase.getDatabase(requireContext())
        var user: User? = database.userDao().getLogin(username)

        // Authenticate User
        if(user?.user_pass == password){
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
