package com.bsrakdg.blogpost.ui

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.bsrakdg.blogpost.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener {

    protected val TAG: String = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onDataStateChange(dataState: DataState<*>?) {
        dataState?.let {
            GlobalScope.launch(Main) {
                // progress bar unique every activity
                displayProgressBar(it.loading.isLoading)

                it.error?.let { eventStateError ->
                    handleStateError(eventStateError)
                }

                it.data?.let { dataEvent ->
                    dataEvent.response?.let { responseEvent ->
                        handleStateResponse(responseEvent)
                    }
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>) {
        event.getContentIfNotHandled()?.let { response ->
            when (response.responseType) {
                is ResponseType.Toast -> {
                    response.message?.let { message -> displayToast(message) }
                }

                is ResponseType.Dialog -> {
                    response.message?.let { message -> displayErrorDialog(message) }
                }

                is ResponseType.None -> {
                    Log.d(TAG, "handleStateResponse: ${response.message}")
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let { stateError ->
            when (stateError.response.responseType) {
                is ResponseType.Toast -> {
                    stateError.response.message?.let { message -> displayToast(message) }
                }

                is ResponseType.Dialog -> {
                    stateError.response.message?.let { message -> displayErrorDialog(message) }
                }

                is ResponseType.None -> {
                    Log.d(TAG, "handleStateError: ${stateError.response.message}")
                }
            }
        }
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    abstract fun displayProgressBar(boolean: Boolean)
}