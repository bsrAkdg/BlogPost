package com.bsrakdg.blogpost.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent.RegisterAttemptEvent
import com.bsrakdg.blogpost.ui.auth.state.RegistrationFields
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RegisterFragment : ${viewModel.hashCode()}")

        subscribeObserver()

        register_button.setOnClickListener {
            register()
        }
    }

    private fun subscribeObserver() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { authViewState ->
            authViewState.registrationFields?.let { registrationFields ->

                // For configuration change
                registrationFields.registration_email?.let { email ->
                    input_email.setText(email)
                }

                registrationFields.registration_username?.let { username ->
                    input_username.setText(username)
                }

                registrationFields.registration_password?.let { password ->
                    input_password.setText(password)
                }

                registrationFields.registration_confirm_password?.let { confirm_password ->
                    input_password_confirm.setText(confirm_password)
                }
            }
        })
    }

    private fun register() {
        // Trigger state event on view model, view model has already subscribe state events changes
        // and then view model handles this event
        viewModel.setStateEvent(
            RegisterAttemptEvent(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // for return back
        viewModel.setRegistrationFields(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }
}
