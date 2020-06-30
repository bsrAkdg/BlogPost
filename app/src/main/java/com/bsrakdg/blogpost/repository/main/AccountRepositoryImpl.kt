package com.bsrakdg.blogpost.repository.main

import android.util.Log
import com.bsrakdg.blogpost.api.GenericResponse
import com.bsrakdg.blogpost.api.main.BlogPostMainService
import com.bsrakdg.blogpost.di.main.MainScope
import com.bsrakdg.blogpost.models.AccountProperties
import com.bsrakdg.blogpost.models.AuthToken
import com.bsrakdg.blogpost.persistence.AccountPropertiesDao
import com.bsrakdg.blogpost.repository.NetworkBoundResource
import com.bsrakdg.blogpost.repository.safeApiCall
import com.bsrakdg.blogpost.session.SessionManager
import com.bsrakdg.blogpost.ui.main.account.state.AccountViewState
import com.bsrakdg.blogpost.utils.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@FlowPreview
@MainScope
class AccountRepositoryImpl
@Inject
constructor(
    val blogPostMainService: BlogPostMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : AccountRepository {

    private val tag: String = "AppDebug"

    override fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    blogPostMainService
                        .getAccountProperties("Token ${authToken.token!!}")
                },
                cacheCall = {
                    accountPropertiesDao.searchByPk(authToken.account_pk!!)
                }

            ) {
            override suspend fun updateCache(networkObject: AccountProperties) {
                Log.d(tag, "updateCache: $networkObject ")
                accountPropertiesDao.updateAccountProperties(
                    networkObject.pk,
                    networkObject.email,
                    networkObject.username
                )
            }

            override fun handleCacheSuccess(
                resultObj: AccountProperties
            ): DataState<AccountViewState> {
                return DataState.data(
                    response = null,
                    data = AccountViewState(
                        accountProperties = resultObj
                    ),
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            blogPostMainService.saveAccountProperties(
                "Token ${authToken.token!!}",
                email,
                username
            )
        }
        emit(
            object : ApiResponseHandler<AccountViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: GenericResponse
                ): DataState<AccountViewState> {

                    val updatedAccountProperties = blogPostMainService
                        .getAccountProperties("Token ${authToken.token!!}")

                    accountPropertiesDao.updateAccountProperties(
                        pk = updatedAccountProperties.pk,
                        email = updatedAccountProperties.email,
                        username = updatedAccountProperties.username
                    )

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }

    override fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            blogPostMainService.updatePassword(
                "Token ${authToken.token!!}",
                currentPassword,
                newPassword,
                confirmNewPassword
            )
        }
        emit(
            object : ApiResponseHandler<AccountViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: GenericResponse
                ): DataState<AccountViewState> {

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}





