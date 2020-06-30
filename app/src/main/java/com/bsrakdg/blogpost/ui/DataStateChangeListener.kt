package com.bsrakdg.blogpost.ui

import com.bsrakdg.blogpost.utils.DataState

interface DataStateChangeListener {
    fun onDataStateChange(dataState: DataState<*>?)
    fun expandAppBar()
    fun hideSoftKeyboard()
    fun isStoragePermissionGranted() : Boolean
}