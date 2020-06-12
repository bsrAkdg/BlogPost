package com.bsrakdg.blogpost.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.UIMessageType.*
import com.bsrakdg.blogpost.utils.Constants.Companion.PERMISSIONS_REQUEST_READ_STORAGE
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(),
    DataStateChangeListener,
    UICommunicationListener {

    protected val TAG: String = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when (uiMessage.uiMessageType) {
            is AreYouSureDialog -> {
                areYouSureDialog(
                    message = uiMessage.message,
                    callBack = uiMessage.uiMessageType.callback
                )
            }

            is Toast -> {
                displayToast(message = uiMessage.message)
            }

            is Dialog -> {
                displayInfoDialog(message = uiMessage.message)
            }

            is None -> {
                Log.d(TAG, "onUIMessageReceived: ${uiMessage.message}")
            }
        }
    }

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
                    response.message?.let { message -> displaySuccessDialog(message) }
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

    override fun isStoragePermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_READ_STORAGE
            )
            return false
        } else {
            // Permisson has already been granted
            return true
        }
    }

    abstract fun displayProgressBar(boolean: Boolean)
}