package com.bsrakdg.blogpost.ui.main

import com.bsrakdg.blogpost.viewmodels.ViewModelProviderFactory
import com.bumptech.glide.RequestManager

interface MainDependencyProvider {
    // BaseBlogFragment dagger inject fields
    fun getViewModelProviderFactory(): ViewModelProviderFactory
    fun getGlideRequestManager(): RequestManager
}