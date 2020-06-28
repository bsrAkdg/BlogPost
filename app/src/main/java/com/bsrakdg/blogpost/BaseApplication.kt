package com.bsrakdg.blogpost

import android.app.Application
import com.bsrakdg.blogpost.di.AppComponent
import com.bsrakdg.blogpost.di.DaggerAppComponent
import com.bsrakdg.blogpost.di.auth.AuthComponent

class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    private var authComponent: AuthComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        val mainComponent = appComponent.mainComponent().create()

        val authComponent = appComponent.authComponent().create()
    }

    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    fun reşeaseAuthComponent() {
        authComponent == null
    }

    private fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}