package com.bsrakdg.blogpost

import android.app.Application
import com.bsrakdg.blogpost.di.AppComponent
import com.bsrakdg.blogpost.di.DaggerAppComponent

class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        val mainComponent = appComponent.mainComponent().create()

        val authComponent = appComponent.authComponent().create()
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}