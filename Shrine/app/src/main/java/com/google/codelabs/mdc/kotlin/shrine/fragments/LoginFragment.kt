package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.auth.PasswordHasher
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Fragment representing the login screen for Shrine.
 */
class LoginFragment : Fragment() {

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
            val email = userEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isEmpty()) {
                userTextInput.error = getString(R.string.shr_error_email_empty)
            } else if (!isValidEmail(email)) {
                userTextInput.error = getString(R.string.shr_error_invalid_email)
            } else if (!isPasswordValid(password)) {
                userTextInput.error = null
                passwordTextInput.error = getString(R.string.shr_error_password)
            } else {
                userTextInput.error = null
                passwordTextInput.error = null // Clear the error
                login(email, password, passwordTextInput)
            }
        }

        view.findViewById<View>(R.id.register_button).setOnClickListener {
            (activity as NavigationHost).navigateTo(RegisterFragment(), true)
        }

        return view
    }

    private fun isPasswordValid(text: String): Boolean {
        return text.length >= 8
    }

    private fun isValidEmail(text: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }

    /**
     * Authenticates against the local database off the main thread, then updates the UI
     * (navigation / error) back on the main thread. Scoped to the view lifecycle so it is
     * cancelled if the view goes away.
     */
    private fun login(email: String, password: String, passwordTextInput: TextInputLayout) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Look up the user and verify the password hash off the main thread.
            val result = withContext(Dispatchers.IO) {
                val user = ShrineDatabase.getDatabase(requireContext()).userDao().getLogin(email)
                val authenticated = user != null &&
                        PasswordHasher.hash(password, user.user_pass_salt) == user.user_pass_hash
                Pair(user, authenticated)
            }

            val user: User? = result.first
            if (result.second && user != null) {
                saveSession(user)
                // Navigate to ProductGrid (on the main thread)
                (activity as NavigationHost).navigateTo(ProductGridFragment(), false)
            } else {
                passwordTextInput.error = getString(R.string.shr_error_invalid_credentials)
            }
        }
    }

    /** Cache the logged-in user's details for the session. */
    private fun saveSession(user: User) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("user_id", user.user_id.toString())
            putString("user_name", user.user_name)
            putString("user_email", user.user_email)
            putString("user_phone", user.user_phone)
            apply()
        }
    }
}
