package com.bsrakdg.blogpost.ui.main.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.Injectable
import com.bsrakdg.blogpost.ui.DataStateChangeListener
import com.bsrakdg.blogpost.ui.main.MainDependencyProvider
import com.bsrakdg.blogpost.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState

abstract class BaseAccountFragment : Fragment(), Injectable {

    val TAG: String = "BaseAccountFragment"

    lateinit var viewModel: AccountViewModel

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var dependencyProvider: MainDependencyProvider

    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {
            outState.putParcelable(
                ACCOUNT_VIEW_STATE_BUNDLE_KEY,
                viewModel.viewState.value
            )
        }
        super.onSaveInstanceState(outState)
    }

    private fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getViewModelProviderFactory())
                .get(AccountViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        //restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { accountViewState ->
                viewModel.setViewState(accountViewState)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.accountFragment, activity as AppCompatActivity)
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }

        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement MainDependencyProvider")
        }
    }
}