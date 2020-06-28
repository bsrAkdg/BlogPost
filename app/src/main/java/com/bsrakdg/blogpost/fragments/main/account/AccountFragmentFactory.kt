package com.bsrakdg.blogpost.fragments.main.account

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.ui.main.account.AccountFragment
import com.bsrakdg.blogpost.ui.main.account.ChangePasswordFragment
import com.bsrakdg.blogpost.ui.main.account.UpdateAccountFragment
import javax.inject.Inject

@MainScope
class AccountFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            AccountFragment::class.java.name -> {
                AccountFragment(viewModelFactory)
            }

            UpdateAccountFragment::class.java.name -> {
                UpdateAccountFragment(viewModelFactory)
            }

            ChangePasswordFragment::class.java.name -> {
                ChangePasswordFragment(viewModelFactory)
            }

            else -> {
                AccountFragment(viewModelFactory)
            }
        }


}