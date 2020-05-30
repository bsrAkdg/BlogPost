package com.bsrakdg.blogpost.repository.auth

import android.util.Log
import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.api.auth.network_responses.LoginResponse
import com.bsrakdg.blogpost.api.auth.network_responses.RegistrationResponse
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.DataState
import com.bsrakdg.blogpost.ui.Response
import com.bsrakdg.blogpost.ui.ResponseType
import com.bsrakdg.blogpost.ui.auth.state.AuthViewState
import com.bsrakdg.blogpost.ui.auth.state.LoginFields
import com.bsrakdg.blogpost.ui.auth.state.RegistrationFields
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.bsrakdg.blogpost.utils.GenericApiResponse
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject // AuthModule.class : provideAuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager
) {

    private val TAG = "AuthRepository"
    private var authRepositoryJob: Job? = null

    fun cancelActiveJob() {
        Log.d(TAG, "cancelActiveJob: Cancelling on-going jobs...")
        authRepositoryJob?.cancel()
    }

    fun attemptLogin(
        email: String,
        password: String
    ): LiveData<DataState<AuthViewState>> {

        // Check validation of login request parameters
        val loginFieldError = LoginFields(email, password).isValidForLogin()

        if (loginFieldError != LoginFields.LoginError.none()) {
            return returnOnErrorResponse(loginFieldError, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConnectedToTheInternet()
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "login handleApiSuccessResponse: $response")

                // Incorrect login credentials counts as a 200 response from server,
                // so need to handle that
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        errorMessage = response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // update MediatorLiveData on NetworkBoundResource with success
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                account_pk = response.body.pk,
                                token = response.body.token
                            )
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return blogPostAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                authRepositoryJob?.cancel()
                authRepositoryJob = job
            }

        }.asLiveData()
    }

    private fun returnOnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        message = errorMessage,
                        responseType = responseType
                    )
                )
            }
        }
    }

    fun attemptRegister(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {

        // Check validation of register request parameters

        val registrationFieldError =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()

        if (registrationFieldError != RegistrationFields.RegistrationError.none()) {
            return returnOnErrorResponse(registrationFieldError, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<RegistrationResponse, AuthViewState>(
            sessionManager.isConnectedToTheInternet()
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "register handleApiSuccessResponse: $response")

                // Incorrect login credentials counts as a 200 response from server,
                // so need to handle that
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        errorMessage = response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // update MediatorLiveData on NetworkBoundResource with success
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                account_pk = response.body.pk,
                                token = response.body.token
                            )
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return blogPostAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                authRepositoryJob?.cancel()
                authRepositoryJob = job
            }

        }.asLiveData()
    }
}

