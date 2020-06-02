package com.bsrakdg.blogpost.ui.main.account

import android.content.Context
import android.util.Log
import com.bsrakdg.blogpost.ui.DataStateChangeListener
import dagger.android.support.DaggerFragment

abstract class BaseAccountFragment : DaggerFragment() {

    val TAG: String = "BaseAccountFragment"

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }
    }
}