package com.google.codelabs.mdc.kotlin.shrine.fragments

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.codelabs.mdc.kotlin.shrine.NavigationHost
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.database.ShrineDatabase
import com.google.codelabs.mdc.kotlin.shrine.models.User
import kotlinx.android.synthetic.main.shr_register_fragment.*
import kotlinx.android.synthetic.main.shr_register_fragment.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    lateinit var database: ShrineDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.shr_register_fragment, container, false)


        view.signup_button.setOnClickListener{
            var noErrors = 0
            noErrors += check(name_text_input)
            noErrors += check(email_text_input)
            noErrors += check(phone_text_input)
            noErrors += check(organisation_text_input)
            noErrors += check(password_text_input)
            noErrors += check(confirm_password_text_input)

            if (password_edit_text?.text.toString() != confirm_password_edit_text?.text.toString()) {
                password_text_input.error = "Passwords Should Match"
                Toast.makeText(requireContext(), "Passwords Should Match", Toast.LENGTH_SHORT).show()
                noErrors += 1
            }

            if (!isPasswordValid(password_edit_text?.text)) {
                password_text_input.error = getString(R.string.shr_error_password)
                noErrors += 1
            }

            if (noErrors == 0) {
                // All fields are valid!
                // Toast.makeText(RegisterActivity.this,"Registered", Toast.LENGTH_SHORT).show();
                userRegister()
            }
        }



        // Inflate the layout for this fragment
        return view
    }

    private fun check(layout: TextInputLayout): Int {
        val editTextString = layout.editText!!.text.toString()
        var error : Int = 0
        if (editTextString.isEmpty()) {
            layout.error = resources.getString(R.string.error_string)
            error = 1
        } else {
            layout.error = null
            error = 0
        }
        return error
    }

    private fun isPasswordValid(text: Editable?): Boolean {
        return text != null && text.length >= 8
    }

    private fun userRegister() {
        val name = name_edit_text?.text.toString().trim()
        val email = email_edit_text?.text.toString().trim()
        val phone = phone_edit_text?.text.toString().trim()
        val organisation = organisation_edit_text?.text.toString().trim()
        val password = password_edit_text?.text.toString().trim()

        database = ShrineDatabase.getDatabase(requireContext())
        GlobalScope.launch {
            database.userDao().insertUser(User(0, name, email, phone, organisation, password))
        }

        Toast.makeText(requireContext(), "User Registered - Please Login", Toast.LENGTH_SHORT).show()

        (activity as NavigationHost).navigateTo(LoginFragment(), false)
    }


}