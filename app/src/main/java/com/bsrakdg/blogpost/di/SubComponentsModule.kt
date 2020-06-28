package com.bsrakdg.blogpost.di

import com.bsrakdg.blogpost.di.auth.AuthComponent
import com.bsrakdg.blogpost.di.main.MainComponent
import dagger.Module

@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule