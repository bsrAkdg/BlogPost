package com.bsrakdg.blogpost.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.AUTH_TOKEN_BUNDLE_KEY
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.ui.BaseActivity
import com.bsrakdg.blogpost.ui.auth.AuthActivity
import com.bsrakdg.blogpost.ui.main.account.BaseAccountFragment
import com.bsrakdg.blogpost.ui.main.account.ChangePasswordFragment
import com.bsrakdg.blogpost.ui.main.account.UpdateAccountFragment
import com.bsrakdg.blogpost.ui.main.blog.BaseBlogFragment
import com.bsrakdg.blogpost.ui.main.blog.UpdateBlogFragment
import com.bsrakdg.blogpost.ui.main.blog.ViewBlogFragment
import com.bsrakdg.blogpost.ui.main.create_blog.BaseCreateBlogFragment
import com.bsrakdg.blogpost.utils.BottomNavController
import com.bsrakdg.blogpost.utils.setUpNavigation
import com.bsrakdg.blogpost.viewmodels.ViewModelProviderFactory
import com.bumptech.glide.RequestManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener,
    MainDependencyProvider {

    private lateinit var bottomNavView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            context = this,
            containerId = R.id.main_nav_host_fragment,
            appStartDestinationId = R.id.nav_blog,
            graphChangeListener = this,
            navGraphProvider = this
        )
    }

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    override fun getViewModelProviderFactory() = providerFactory

    override fun getGlideRequestManager() = requestManager

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)
        super.onSaveInstanceState(outState)
    }

    private fun onRestoreSession(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            bundle[AUTH_TOKEN_BUNDLE_KEY]?.let { authToken ->
                sessionManager.setValue(authToken as AuthToken)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        bottomNavView = findViewById(R.id.bottom_navigation_view)
        bottomNavView.setUpNavigation(bottomNavController, this)

        if (savedInstanceState == null) {
            // parameters is null because, default selected item is navBackStack.last() on onNavItemSelected
            bottomNavController.onNavigationItemSelected()
        }

        subscribeObservers()

        onRestoreSession(savedInstanceState)
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun getNavGraphId(itemId: Int): Int = when (itemId) {
        R.id.nav_blog -> {
            R.navigation.nav_blog
        }
        R.id.nav_account -> {
            R.navigation.nav_account
        }
        R.id.nav_create_blog -> {
            R.navigation.nav_create_blog
        }
        else -> {
            R.navigation.nav_blog
        }
    }

    override fun onGraphChange() {
        // TODO("What needs to happen when the graph changes?")
        expandAppBar()
        cancelActiveJobs()
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)?.childFragmentManager?.fragments

        if (fragments != null) {
            for (fragment in fragments) {
                when (fragment) {
                    is BaseBlogFragment -> fragment.cancelActiveJobs()
                    is BaseCreateBlogFragment -> fragment.cancelActiveJobs()
                    is BaseAccountFragment -> fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) =
        when (fragment) {
            is ViewBlogFragment -> {
                navController.navigate(R.id.action_viewBlogFragment_to_blogFragment)
            }
            is UpdateBlogFragment -> {
                navController.navigate(R.id.action_updateBlogFragment_to_blogFragment)
            }
            is UpdateAccountFragment -> {
                navController.navigate(R.id.action_updateAccountFragment_to_accountFragment)
            }
            is ChangePasswordFragment -> {
                navController.navigate(R.id.action_changePasswordFragment_to_accountFragment)
            }
            else -> {
                // do nothing
            }
        }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }
}