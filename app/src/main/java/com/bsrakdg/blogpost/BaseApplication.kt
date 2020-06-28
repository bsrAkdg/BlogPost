package com.bsrakdg.blogpost

import android.app.Application
import com.bsrakdg.blogpost.di.AppComponent
import com.bsrakdg.blogpost.di.DaggerAppComponent
import com.bsrakdg.blogpost.di.auth.AuthComponent
import com.bsrakdg.blogpost.di.main.MainComponent

class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    private var authComponent: AuthComponent? = null

    private var mainComponent: MainComponent? = null


    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    fun mainComponent(): MainComponent {
        if (mainComponent == null) {
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

    fun releaseAuthComponent() {
        authComponent == null
    }

    fun releaseMainComponent() {
        mainComponent == null
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}