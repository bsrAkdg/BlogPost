package com.bsrakdg.blogpost.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bsrakdg.blogpost.api.auth.network_responses.LoginResponse
import com.bsrakdg.blogpost.api.auth.network_responses.RegistrationResponse
import com.bsrakdg.blogpost.repository.auth.AuthRepository
import com.bsrakdg.blogpost.utils.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    fun testLogin(): LiveData<GenericApiResponse<LoginResponse>> {
        return authRepository.testLogin(
            "your_mail_address",
            "your_password"
        )
    }

    fun testRegister(): LiveData<GenericApiResponse<RegistrationResponse>> {
        return authRepository.testRegister(
            "your_mail_address",
            "your_username",
            "your_password",
            "your_password"
        )
    }
}