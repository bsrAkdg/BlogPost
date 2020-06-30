package com.bsrakdg.blogpost.repository.auth

import android.content.SharedPreferences
import android.util.Log
import com.bsrakdg.blogpost.api.auth.BlogPostAuthService
import com.bsrakdg.blogpost.api.auth.network_responses.LoginResponse
import com.bsrakdg.blogpost.api.auth.network_responses.RegistrationResponse
import com.bsrakdg.blogpost.di.auth.AuthScope
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.persistence.AuthTokenDao
import com.bsrakdg.blogpost.repository.buildError
import com.bsrakdg.blogpost.repository.safeApiCall
import com.bsrakdg.blogpost.repository.safeCacheCall
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.auth.state.AuthViewState
import com.bsrakdg.blogpost.ui.auth.state.LoginFields
import com.bsrakdg.blogpost.ui.auth.state.RegistrationFields
import com.bsrakdg.blogpost.utils.*
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.bsrakdg.blogpost.utils.ErrorHandling.Companion.INVALID_CREDENTIALS
import com.bsrakdg.blogpost.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@FlowPreview
@AuthScope
class AuthRepositoryImpl
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    private val blogPostAuthService: BlogPostAuthService,
    val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : AuthRepository {

    private val tag: String = "AppDebug"

    override fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>> = flow {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors == LoginFields.LoginError.none()) {
            val apiResult = safeApiCall(IO) {
                blogPostAuthService.login(email, password)
            }
            emit(
                object : ApiResponseHandler<AuthViewState, LoginResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: LoginResponse): DataState<AuthViewState> {

                        Log.d(tag, "handleSuccess ")

                        // Incorrect login credentials counts as a 200 response from server, so need to handle that
                        if (resultObj.response == GENERIC_AUTH_ERROR) {
                            return DataState.error(
                                response = Response(
                                    INVALID_CREDENTIALS,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        accountPropertiesDao.insertOrIgnore(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                ""
                            )
                        )

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result = authTokenDao.insertAndReplace(authToken)
                        if (result < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)

                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }

                }.getResult()
            )
        } else {
            Log.d(tag, "emitting error: $loginFieldErrors")
            emit(
                buildError(
                    loginFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = flow {
        val registrationFieldErrors =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if (registrationFieldErrors == RegistrationFields.RegistrationError.none()) {

            val apiResult = safeApiCall(IO) {
                blogPostAuthService.register(
                    email,
                    username,
                    password,
                    confirmPassword
                )
            }
            emit(
                object : ApiResponseHandler<AuthViewState, RegistrationResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: RegistrationResponse): DataState<AuthViewState> {
                        if (resultObj.response == GENERIC_AUTH_ERROR) {
                            return DataState.error(
                                response = Response(
                                    resultObj.errorMessage,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        val result1 = accountPropertiesDao.insertAndReplace(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                resultObj.username
                            )
                        )
                        // will return -1 if failure
                        if (result1 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_ACCOUNT_PROPERTIES,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result2 = authTokenDao.insertAndReplace(authToken)
                        if (result2 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)
                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }
                }.getResult()
            )

        } else {
            emit(
                buildError(
                    registrationFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }

    }


    override fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>> = flow {
        Log.d(tag, "checkPreviousAuthUser: ")
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(tag, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        } else {
            val apiResult = safeCacheCall(IO) {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail)
            }
            emit(
                object : CacheResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {

                        if (resultObj.pk > -1) {
                            authTokenDao.searchByPk(resultObj.pk).let { authToken ->
                                if (authToken != null) {
                                    if (authToken.token != null) {
                                        return DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            ),
                                            response = null,
                                            stateEvent = stateEvent
                                        )
                                    }
                                }
                            }
                        }
                        Log.d(tag, "createCacheRequestAndReturn: AuthToken not found...")
                        return DataState.error(
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                UIComponentType.None(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }.getResult()
            )
        }
    }

    override fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    override fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState> {

        return DataState.error(
            response = Response(
                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                UIComponentType.None(),
                MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }

}


