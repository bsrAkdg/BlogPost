package com.bsrakdg.blogpost.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import kotlinx.android.synthetic.main.fragment_account.*
import javax.inject.Inject

@MainScope
class AccountFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseAccountFragment(R.layout.fragment_account) {

    val viewModel: AccountViewModel by viewModels {
        viewModelFactory
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            ACCOUNT_VIEW_STATE_BUNDLE_KEY,
            viewModel.viewState.value
        )
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()

        //restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ACCOUNT_VIEW_STATE_BUNDLE_KEY] as AccountViewState?)?.let { accountViewState ->
                viewModel.setViewState(accountViewState)
            }
        }
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        subscribeObservers()

        change_password.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        logout_button.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(
            AccountStateEvent.GetAccountPropertiesEvent()
        )
    }

    private fun subscribeObservers() {

        // Update View Model
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.data?.let { event ->
                        event.getContentIfNotHandled()?.let { accountViewState ->
                            accountViewState.accountProperties?.let { accountProperties ->
                                Log.d(TAG, "AccountFragment, DataState: $accountProperties")
                                viewModel.setAccountPropertiesData(accountProperties)
                            }
                        }
                    }
                }
            }
        })

        // Update UI
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it?.let { accountViewState ->
                accountViewState.accountProperties?.let { accountProperties ->
                    Log.d(TAG, "AccountFragment, DataState: $accountProperties")
                    setAccountDataFields(accountProperties)
                }
            }

        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        email?.text = accountProperties.email
        username?.text = accountProperties.username
    }
}