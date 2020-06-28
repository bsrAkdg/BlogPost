package com.bsrakdg.blogpost.ui.main

import com.bsrakdg.blogpost.viewmodels.AuthViewModelFactory
import com.bumptech.glide.RequestManager

interface MainDependencyProvider {
    // BaseBlogFragment dagger inject fields
    fun getViewModelProviderFactory(): AuthViewModelFactory
    fun getGlideRequestManager(): RequestManager
}