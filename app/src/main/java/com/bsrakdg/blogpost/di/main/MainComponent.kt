package com.bsrakdg.blogpost.di.main

import com.bsrakdg.blogpost.ui.main.MainActivity
import dagger.Subcomponent

@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class
    ]
)
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)
}