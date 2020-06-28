package com.bsrakdg.blogpost.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.auth.AuthScope
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import com.bsrakdg.blogpost.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

@AuthScope
class LoginFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_login) {

    val viewModel: AuthViewModel by viewModels { // new way initialize viewModel
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObserver()

        // main activity navigate example
        login_button.setOnClickListener {
            login()
        }
    }

    fun login() {
        // Trigger state event on view model, view model has already subscribe state events changes
        // and then view model handles this event
        viewModel.setStateEvent(
            LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
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
