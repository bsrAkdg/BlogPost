package com.bsrakdg.blogpost.di.auth

import com.bsrakdg.blogpost.ui.auth.AuthActivity
import dagger.Subcomponent

@Subcomponent(
    modules = [
        AuthModule::class,
        AuthViewModelModule::class,
        AuthFragmentsModule::class
    ]
)
interface AuthComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }

    fun inject(authActivity: AuthActivity)
}