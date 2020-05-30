package com.bsrakdg.blogpost.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.BaseActivity
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent
import com.bsrakdg.blogpost.ui.main.MainActivity
import com.bsrakdg.blogpost.viewmodels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        // listen nav graph changes, if nav graph change cancel all jobs on repository
        findNavController(R.id.auth_fragments_container).addOnDestinationChangedListener(this)

        subscribeObservers()

        // 1. Check shared pref has a email?
        // 2. Check database has a token?
        // 3. If has a token go main page directly
        // 4. If has not a token go login page directly
        checkPreviousAuthUser()
    }

    private fun subscribeObservers() {

        // view model repository response subscribes
        viewModel.dataState.observe(this, Observer { dataState ->

            // trigger base activity data state change listener
            onDataStateChange(dataState)

            // handle data on datastate (success)
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

    fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
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
