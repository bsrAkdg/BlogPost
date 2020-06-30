package com.bsrakdg.blogpost.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bsrakdg.blogpost.BaseApplication
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.fragments.auth.AuthNavHostFragment
import com.bsrakdg.blogpost.ui.BaseActivity
import com.bsrakdg.blogpost.ui.auth.state.AuthStateEvent
import com.bsrakdg.blogpost.ui.main.MainActivity
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    override fun inject() {
        (application as BaseApplication).authComponent()
            .inject(this)
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // 1. Check shared pref has a email?
        // 2. Check database has a token?
        // 3. If has a token go main page directly
        // 4. If has not a token go login page directly
        checkPreviousAuthUser()
    }

    private fun onRestoreInstanceState() {
        val host = supportFragmentManager.findFragmentById(R.id.auth_fragments_container)
        host?.let {
            // do nothing
        } ?: createNavHost()
    }

    private fun createNavHost() {
        val navHost = AuthNavHostFragment.create(
            R.navigation.auth_nav_graph
        )
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.auth_fragments_container,
                navHost,
                getString(R.string.AuthNavHost)
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        subscribeObservers()
        onRestoreInstanceState()
    }

    override fun expandAppBar() {
        // ignore
    }

    private fun subscribeObservers() {

        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let { authViewState ->
                        authViewState.authToken?.let { authToken ->
                            Log.d(tag, "AuthActivity, DataState: $authToken")
                            viewModel.setAuthToken(authToken)
                        }
                    }
                }
                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message == RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE) {
                                onFinishCheckPreviousAuthUser()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer {
            Log.d(tag, "AuthActivity, subscribeObservers: AuthViewState: ${it}")
            it.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer { dataState ->
            Log.d(tag, "AuthActivity, subscribeObservers: AuthDataState: ${dataState}")
            dataState.let { authToken ->
                if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                    navMainActivity()
                }
            }
        })
    }

    private fun onFinishCheckPreviousAuthUser() {
        fragment_container.visibility = View.VISIBLE
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseAuthComponent()
    }
}
