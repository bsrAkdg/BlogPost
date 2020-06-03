package com.bsrakdg.blogpost.di.main

import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.di.ViewModelKey
import com.bsrakdg.blogpost.ui.main.account.AccountViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

}