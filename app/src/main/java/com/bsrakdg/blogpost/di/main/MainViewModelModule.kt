package com.bsrakdg.blogpost.di.main

import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.di.ViewModelKey
import com.bsrakdg.blogpost.ui.main.account.AccountViewModel
import com.bsrakdg.blogpost.ui.main.blog.viewmodel.BlogViewModel
import com.bsrakdg.blogpost.ui.main.create_blog.viewmodel.CreateBlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(accountViewModel: BlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel

}