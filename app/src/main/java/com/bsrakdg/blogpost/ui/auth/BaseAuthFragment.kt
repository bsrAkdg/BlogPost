package com.bsrakdg.blogpost.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bsrakdg.blogpost.di.Injectable
import com.bsrakdg.blogpost.viewmodels.AuthViewModelFactory
import javax.inject.Inject

abstract class BaseAuthFragment : Fragment(), Injectable {

    val TAG: String = "BaseAuthFragment"

    @Inject
    lateinit var providerFactory: AuthViewModelFactory

    lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = activity?.run { // activity is not null
            ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        } ?: throw Exception("Invalid Activity")

        cancelActiveJobs()
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }
}