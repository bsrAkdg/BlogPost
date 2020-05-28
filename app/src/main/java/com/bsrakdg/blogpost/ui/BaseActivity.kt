package com.bsrakdg.blogpost.ui

import com.bsrakdg.blogpost.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    protected val TAG: String = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager


}