package com.bsrakdg.blogpost.di

import androidx.lifecycle.ViewModelProvider
import com.bsrakdg.blogpost.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory

}