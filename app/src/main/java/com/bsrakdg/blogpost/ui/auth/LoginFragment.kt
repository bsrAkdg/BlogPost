package com.bsrakdg.blogpost.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment : ${viewModel.hashCode()}")

        subscribeObserver()
    }

    private fun subscribeObserver() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { authViewState ->
            authViewState.loginFields?.let { loginFields ->

                // For configuration change
                loginFields.login_email?.let { email ->
                    input_email.setText(email)
                }

                loginFields.login_password?.let { password ->
                    input_password.setText(password)
                }

            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // for return back
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }
}
