package com.bsrakdg.blogpost.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.utils.ApiEmptyResponse
import com.bsrakdg.blogpost.utils.ApiErrorResponse
import com.bsrakdg.blogpost.utils.ApiSuccessResponse

class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment : ${viewModel.hashCode()}")


        viewModel.testLogin().observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ApiSuccessResponse -> {
                    Log.d(TAG, "LoginFragment response : ${response.body}")
                }

                is ApiErrorResponse -> {
                    Log.d(TAG, "LoginFragment error message : ${response.errorMessage}")
                }

                is ApiEmptyResponse -> {
                    Log.d(TAG, "LoginFragment empty response")
                }
            }
        })
    }
}
