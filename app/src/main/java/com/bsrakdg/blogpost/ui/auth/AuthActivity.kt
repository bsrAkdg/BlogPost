package com.bsrakdg.blogpost.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.BaseActivity
import com.bsrakdg.blogpost.ui.ResponseType.*
import com.bsrakdg.blogpost.ui.main.MainActivity
import com.bsrakdg.blogpost.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        // listen nav graph changes
        findNavController(R.id.auth_fragments_container).addOnDestinationChangedListener(this)

        subscribeObservers()
    }

    private fun subscribeObservers() {

        // view model repository response subscribes
        viewModel.dataState.observe(this, Observer { dataState ->

            dataState.data?.let { data ->

                // data on Data class
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let { authViewState ->
                        authViewState.authToken?.let { authToken ->
                            Log.d(TAG, "AuthActivity, DataState : $authToken")
                            viewModel.setAuthToken(authToken)
                        }
                    }
                }

                // response on Data class
                data.response?.let { event ->
                    event.getContentIfNotHandled()?.let { response ->

                        when (response.responseType) {
                            is Dialog -> {
                                // show dialog
                                Log.d(TAG, "AuthActivity, Response : ${response.message} ");
                            }
                            is Toast -> {
                                // show toast
                                Log.d(TAG, "AuthActivity, Response : ${response.message} ");
                            }
                            is None -> {
                                Log.d(TAG, "AuthActivity, Response : ${response.message} ");
                            }
                        }
                    }
                }
            }
        })

        // view model subscribes
        viewModel.viewState.observe(this, Observer { authViewState ->
            authViewState.authToken?.let { authToken ->
                sessionManager.login(authToken)
            }
        })

        // session manager subscribes
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "AuthActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                navMainActivity()
                finish()
            }
        })
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

}
