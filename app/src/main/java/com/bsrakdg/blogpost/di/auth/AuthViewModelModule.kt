package com.bsrakdg.blogpost.di.auth

import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.di.ViewModelKey
import com.bsrakdg.blogpost.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}