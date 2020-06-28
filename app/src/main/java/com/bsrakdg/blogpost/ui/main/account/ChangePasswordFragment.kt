package com.bsrakdg.blogpost.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.ui.main.account.state.ACCOUNT_VIEW_STATE_BUNDLE_KEY
import com.bsrakdg.blogpost.ui.main.account.state.AccountStateEvent
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import kotlinx.android.synthetic.main.fragment_change_password.*
import javax.inject.Inject

class ChangePasswordFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseAccountFragment(R.layout.fragment_change_password) {

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

        update_password_button.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    input_current_password.text.toString(),
                    input_new_password.text.toString(),
                    input_confirm_new_password.text.toString()
                )
            )
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            Log.d(TAG, "ChangePasswordFragment, DataState: $dataState")
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.response?.let { event ->
                        if (event.peekContent()
                                .message
                                .equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)
                        ) { // base activity already handled it
                            stateChangeListener.hideSoftKeyboard()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })
    }
}