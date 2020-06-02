package com.bsrakdg.blogpost.di.main

import com.bsrakdg.blogpost.ui.main.account.AccountFragment
import com.bsrakdg.blogpost.ui.main.account.ChangePasswordFragment
import com.bsrakdg.blogpost.ui.main.account.UpdateAccountFragment
import com.bsrakdg.blogpost.ui.main.blog.BlogFragment
import com.bsrakdg.blogpost.ui.main.blog.UpdateBlogFragment
import com.bsrakdg.blogpost.ui.main.blog.ViewBlogFragment
import com.bsrakdg.blogpost.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}