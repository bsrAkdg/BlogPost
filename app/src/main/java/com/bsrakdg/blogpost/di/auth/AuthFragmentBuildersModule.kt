package com.bsrakdg.blogpost.di.auth

import com.bsrakdg.blogpost.ui.auth.ForgotPasswordFragment
import com.bsrakdg.blogpost.ui.auth.LauncherFragment
import com.bsrakdg.blogpost.ui.auth.LoginFragment
import com.bsrakdg.blogpost.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment
}