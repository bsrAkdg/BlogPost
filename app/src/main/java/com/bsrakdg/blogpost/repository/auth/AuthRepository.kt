package com.bsrakdg.blogpost.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.api.auth.network_responses.LoginResponse
import com.bsrakdg.blogpost.api.auth.network_responses.RegistrationResponse
import com.bsrakdg.blogpost.models.AccountProperties
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
import com.bsrakdg.blogpost.utils.AbsentLiveData
import com.bsrakdg.blogpost.utils.ApiSuccessResponse
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.bsrakdg.blogpost.utils.GenericApiResponse
import com.bsrakdg.blogpost.utils.PreferenceKeys
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject // AuthModule.class : provideAuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
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

        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldLoadFromCache = false
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

                // don't care about result, just insert if it doesn't exist b/c foreign key relationship
                // with AuthToken table
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        pk = response.body.pk,
                        email = response.body.email,
                        username = "" // it is not matter
                    )
                )

                // will return -1 if it failure
                val result = authTokenDao.insertAndReplace(
                    authToken = AuthToken(
                        account_pk = response.body.pk,
                        token = response.body.token
                    )
                )

                // AuthToken insert is important, if can't insert show error dialog
                // update MediatorLiveData on NetworkBoundResource with error
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(
                                message = ERROR_SAVE_AUTH_TOKEN,
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

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

            override suspend fun createCacheRequestAndReturn() {
                // not used in this case
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // ignore
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // ignore
            }

        }.asLiveData()
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

        return object : NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldLoadFromCache = false
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

                // don't care about result, just insert if it doesn't exist b/c foreign key relationship
                // with AuthToken table
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        pk = response.body.pk,
                        email = response.body.email,
                        username = "" // it is not matter
                    )
                )

                // will return -1 if it failure
                val result = authTokenDao.insertAndReplace(
                    authToken = AuthToken(
                        account_pk = response.body.pk,
                        token = response.body.token
                    )
                )

                // AuthToken insert is important, if can't insert show error dialog
                // update MediatorLiveData on NetworkBoundResource with error
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(
                                message = ERROR_SAVE_AUTH_TOKEN,
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

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

            override suspend fun createCacheRequestAndReturn() {
                // not used in this case
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // ignore
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // ignore
            }
        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        // if shared preferences has not email, continue login page
        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: NULL OR BLANK EMAIL")
            return returnNoTokenFound()
        }

        // if shared preferences has email, get token from cache (database)
        return object : NetworkBoundResource<Void, Any, AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = false,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Log.d(TAG, "createCacheRequestAndReturn: searching for token $accountProperties")

                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    // has token, you should continue main page directly
                                    onCompleteJob(
                                        dataState = DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            )
                                        )
                                    )
                                    return
                                }

                            }
                        }
                    }
                    Log.d(TAG, "checkPreviousAuthUser: AuthToken not found ...")
                    // has not token, you should continue login page
                    onCompleteJob(
                        dataState = DataState.data(
                            data = null,
                            response = Response(
                                message = RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                responseType = ResponseType.None()
                            )
                        )
                    )

                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                // not used in this case
            }

            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                // not used in this case
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                authRepositoryJob?.cancel()
                authRepositoryJob = job
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // ignore
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // ignore
            }

        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(
                        message = RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                        responseType = ResponseType.None()
                    )
                )
            }
        }
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


    private fun saveAuthenticatedUserToPrefs(email: String) {
        Log.d(TAG, "saveAuthenticatedUserToPrefs: $email")
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }
}

