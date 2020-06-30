package com.bsrakdg.blogpost.ui

import com.bsrakdg.blogpost.utils.Response
import com.bsrakdg.blogpost.utils.StateMessageCallback

interface UICommunicationListener {

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

    fun displayProgressBar(isLoading: Boolean)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}