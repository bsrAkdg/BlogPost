package com.bsrakdg.blogpost.repository.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.Response
import com.bsrakdg.blogpost.ui.ResponseType
import com.bsrakdg.blogpost.ui.auth.state.AuthViewState
import com.bsrakdg.blogpost.utils.ApiEmptyResponse
import com.bsrakdg.blogpost.utils.ApiErrorResponse
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import javax.inject.Inject

class AuthRepository
@Inject // AuthModule.class : provideAuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager
) {

    fun attemptLogin(
        email: String,
        password: String
    ): LiveData<DataState<AuthViewState>> {
        return blogPostAuthService.login(email, password).switchMap { response ->
            object : LiveData<DataState<AuthViewState>>() {
                override fun onActive() {
                    super.onActive()
                    when (response) {
                        is ApiSuccessResponse -> {
                            value = DataState.data(
                                data = AuthViewState(
                                    authToken = AuthToken(
                                        response.body.pk,
                                        response.body.token
                                    )
                                ),
                                response = null
                            )
                        }
                        is ApiErrorResponse -> {
                            value = DataState.error(
                                response = Response(
                                    message = response.errorMessage,
                                    responseType = ResponseType.Dialog()
                                )
                            )
                        }
                        is ApiEmptyResponse -> {
                            value = DataState.error(
                                response = Response(
                                    message = ERROR_UNKNOWN,
                                    responseType = ResponseType.Dialog()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun attemptRegister(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        return blogPostAuthService.register(email, username, password, confirmPassword)
            .switchMap { response ->
                object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        when (response) {
                            is ApiSuccessResponse -> {
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthToken(
                                            response.body.pk,
                                            response.body.token
                                        )
                                    ),
                                    response = null
                                )
                            }
                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = response.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

//    fun attemptRegister(
//        email: String,
//        username: String,
//        password: String,
//        confirmPassword: String
//    ): LiveData<DataState<AuthViewState>> {
//        return blogPostAuthService.register(email, username, password, confirmPassword)
//    }
}

