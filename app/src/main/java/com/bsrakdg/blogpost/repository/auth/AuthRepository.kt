package com.bsrakdg.blogpost.repository.auth

import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.api.auth.network_responses.LoginResponse
import com.bsrakdg.blogpost.api.auth.network_responses.RegistrationResponse
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.utils.GenericApiResponse
import javax.inject.Inject

class AuthRepository
@Inject // AuthModule.class : provideAuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager
) {

    fun testLogin(
        email: String,
        password: String
    ): LiveData<GenericApiResponse<LoginResponse>> {
        return blogPostAuthService.login(email, password)
    }

    fun testRegister(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>> {
        return blogPostAuthService.register(email, username, password, confirmPassword)
    }
}

