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
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import kotlinx.android.synthetic.main.fragment_update_account.*
import javax.inject.Inject

class UpdateAccountFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseAccountFragment(R.layout.fragment_update_account) {

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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            Log.d(TAG, "UpdateAccountFragment: DataState $dataState")
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it?.let { accountViewState ->
                accountViewState.accountProperties?.let { accountProperties ->
                    Log.d(TAG, "UpdateAccountFragment: ViewState $accountProperties")
                    setAccountDataFields(accountProperties)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        input_email?.let {
            input_email.setText(accountProperties.email)
        }

        input_username?.let {
            input_username.setText(accountProperties.username)
        }
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                email = input_email.text.toString(),
                username = input_username.text.toString()
            )
        )

        stateChangeListener.hideSoftKeyboard()
    }
}