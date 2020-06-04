package com.bsrakdg.blogpost.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent
import kotlinx.android.synthetic.main.fragment_update_account.*

class UpdateAccountFragment : BaseAccountFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_account, container, false)
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
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "UpdateAccountFragment: DataState $dataState")
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
    }
}