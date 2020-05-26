package com.bsrakdg.blogpost.di

import com.bsrakdg.blogpost.di.auth.AuthFragmentBuildersModule
import com.bsrakdg.blogpost.di.auth.AuthModule
import com.bsrakdg.blogpost.di.auth.AuthScope
import com.bsrakdg.blogpost.di.auth.AuthViewModelModule
import com.bsrakdg.blogpost.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [
            AuthModule::class,
            AuthFragmentBuildersModule::class,
            AuthViewModelModule::class
        ]
    )
    abstract fun contributeAuthActivity(): AuthActivity
}